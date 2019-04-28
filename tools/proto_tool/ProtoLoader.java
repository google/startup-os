/*
 * Copyright 2018 The StartupOS Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.startupos.tools.proto_tool;

import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class ProtoLoader {
  public static void toProtobin(
      List<Path> protos, Path protoTxt, Path protoBinOutput, String protoClassName)
      throws Exception {

    final Path tempDirectory = Files.createTempDirectory("protoLoader");

    ArrayList<String> args = new ArrayList<>(protos.size() + 3);
    args.add("/usr/bin/env");
    args.add("bash");
    args.add(Paths.get("./tools/protoc.sh").toAbsolutePath().toString());

    for (Path proto : protos) {
      Path resutingFile =
          Paths.get(tempDirectory.toAbsolutePath().toString(), proto.getFileName().toString());

      String updatedProto =
          Files.readAllLines(proto)
              .stream()
              .map(
                  s -> {
                    if (s.contains("import")) {
                      if (s.lastIndexOf('/') != -1) {
                        // proto import contains directories
                        s =
                            String.format(
                                "import \"%s\";",
                                s.substring(s.lastIndexOf('/') + 1, s.lastIndexOf('"')));
                      }
                    }
                    return s;
                  })
              .collect(Collectors.joining("\n"));

      Files.write(resutingFile, updatedProto.getBytes());
      args.add(resutingFile.toString());
    }

    args.add("-I");
    args.add(tempDirectory.toString());
    args.add("--java_out");
    args.add(tempDirectory.toString());

    Process protoCompileProcess = new ProcessBuilder(args).inheritIO().start();
    int res = protoCompileProcess.waitFor();

    if (res != 0) {
      throw new Exception("Could not compile proto files");
    }

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

    ArrayList<File> files = new ArrayList<>();

    Files.walkFileTree(
        tempDirectory,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (file.getFileName().toString().endsWith(".java")) {
              files.add(file.toFile());
            }
            return FileVisitResult.CONTINUE;
          }
        });

    CompilationTask task =
        compiler.getTask(
            null, null, null, null, null, fileManager.getJavaFileObjectsFromFiles(files));

    if (!task.call()) {
      throw new Exception("Compilation of generated files was not successful");
    }

    ClassLoader loader =
        new URLClassLoader(
            new URL[] {tempDirectory.toUri().toURL()}, ClassLoader.getSystemClassLoader());

    Class protoMessageClass = loader.loadClass(protoClassName);

    //noinspection unchecked
    Method protoMessageNewBuilderMethod = protoMessageClass.getDeclaredMethod("newBuilder");

    Message.Builder protoMessageBuilder =
        (com.google.protobuf.Message.Builder) protoMessageNewBuilderMethod.invoke(null);

    TextFormat.Parser parser = TextFormat.getParser();
    parser.merge(new String(Files.readAllBytes(protoTxt)), protoMessageBuilder);

    Message message = protoMessageBuilder.build();

    try (FileOutputStream outputStream = new FileOutputStream(protoBinOutput.toFile())) {
      message.writeTo(outputStream);
    }
  }
}

