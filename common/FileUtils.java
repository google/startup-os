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

 package com.google.startupos.common;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileSystem;
import java.text.ParseException;
import java.util.Arrays;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import com.google.protobuf.TextFormat;
import javax.inject.Singleton;
import javax.inject.Inject;

/** File utils */
@Singleton
public class FileUtils {
  private FileSystem fileSystem;

  @Inject
  FileUtils(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  /** Replace the ~ in e.g ~/path with the home directory. */
  public String expandHomeDirectory(String path) {
    if (path.startsWith("~" + File.separator)) {
      path = System.getProperty("user.home") + path.substring(1);
    }
    return path;
  }

  /** Reads a prototxt file into a proto. */
  public Message readPrototxt(String path, Message.Builder builder)
      throws IOException, ParseException {
    String protoText = Files.toString(new File(expandHomeDirectory(path)), UTF_8);
    TextFormat.merge(protoText, builder);
    return builder.build();
  }

  /** Reads a prototxt file into a proto, rethrows exceptions as unchecked. */
  public Message readPrototxtUnchecked(String path, Message.Builder builder) {
    try {
      return readPrototxt(path, builder);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Writes a proto to file. */
  public void writePrototxt(Message proto, String path) throws IOException {
    writeString(TextFormat.printToUnicodeString(proto), path);
  }

  /** Writes a proto to file, rethrows exceptions as unchecked. */
  public void writePrototxtUnchecked(Message proto, String path) {
    try {
      writePrototxt(proto, path);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Writes a string to file. */
  public void writeString(String text, String path) throws IOException {
    mkdirs(path);
    java.nio.file.Files.write(Paths.get(expandHomeDirectory(path)), text.getBytes());
  }

  /** Writes a string to file, rethrows exceptions as unchecked. */
  public void writeStringUnchecked(String text, String path) {
    try {
      writeString(text, path);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Checks if file exists. Returns false for folders. */
  public boolean fileExists(String path) {
    File file = new File(expandHomeDirectory(path));
    return file.exists() && file.isFile();
  }

  /** Checks if folder exists. Returns false for files. */
  public boolean folderExists(String path) {
    return java.nio.file.Files.isDirectory(fileSystem.getPath(expandHomeDirectory(path)));
  }

  /** Checks if folder or folder exists. */
  public boolean fileOrFolderExists(String path) {
    File file = new File(expandHomeDirectory(path));
    return file.exists();
  }

  /** Creates directories in path if none exist. */
  public void mkdirs(String path) {
    try {
      Files.createParentDirs(new File(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Gets filenames in path. */
  public ImmutableList<String> getFiles(String path) {
    String[] files = Paths.get(path).toFile().list();
    if (files.length == 0) {
      return ImmutableList.of();
    }
    return ImmutableList.sortedCopyOf(Arrays.asList(files));
  }

  /** Reads a text file. */
  public String readFile(String path) throws IOException {
    try {
      return Files.toString(new File(expandHomeDirectory(path)), UTF_8);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Reads a text file, rethrows exceptions as unchecked. */
  public String readFileUnchecked(String path) {
    try {
      return readFile(path);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Writes a proto to binary file. */
  public void writeProtoBinary(Message proto, String path) throws IOException {
    mkdirs(path);
    proto.writeTo(new FileOutputStream(path));
  }

  /** Writes a proto to binary file, rethrows exceptions as unchecked. */
  public void writeProtoBinaryUnchecked(Message proto, String path) {
    try {
      writeProtoBinary(proto, path);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Reads a proto binary file into a proto. */
  public Message readProtoBinary(String path, Message.Builder builder) throws IOException {
    InputStream input = new FileInputStream(path);
    return builder.build().getParserForType().parseFrom(input);
  }

  /** Reads a proto binary file into a proto, rethrows exceptions as unchecked. */
  public Message readProtoBinaryUnchecked(String path, Message.Builder builder) {
    try {
      return readProtoBinary(path, builder);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void copyDirectoryToDirectory(String source, String destination) throws IOException {
    final Path sourcePath = Paths.get(source);
    final Path targetPath = Paths.get(destination);
    java.nio.file.Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(final Path dir,
                                               final BasicFileAttributes attrs) throws IOException {
        java.nio.file.Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(final Path file,
                                       final BasicFileAttributes attrs) throws IOException {
        java.nio.file.Files.copy(file,
                targetPath.resolve(sourcePath.relativize(file)));
        return FileVisitResult.CONTINUE;
      }
    });
  }
}
