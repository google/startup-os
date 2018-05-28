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


import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.FileVisitResult;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.inject.Inject;

/** File utils */
@Singleton
public class FileUtils {
  private final String userHome;
  private FileSystem fileSystem;

  @Inject
  FileUtils(FileSystem fileSystem, @Named("Home folder") String userHome) {
    this.fileSystem = fileSystem;
    this.userHome = userHome;
  }

  /** Replace the ~ in e.g ~/path with the home directory. */
  public String expandHomeDirectory(String path) {
    if (path.startsWith("~" + fileSystem.getSeparator())) {
      path = userHome + path.substring(1);
    }
    return path;
  }

  /** Reads a prototxt file into a proto. */
  public Message readPrototxt(String path, Message.Builder builder) throws IOException {
    String protoText = readFile(path);
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
    File file = new File(path);
    if (file.getParent() != null) {
      mkdirs(file.getParent());
    }
    Files.write(fileSystem.getPath(expandHomeDirectory(path)), text.getBytes());
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
    return Files.isRegularFile(fileSystem.getPath(expandHomeDirectory(path)));
  }

  /** Checks if folder exists. Returns false for files. */
  public boolean folderExists(String path) {
    return Files.isDirectory(fileSystem.getPath(expandHomeDirectory(path)));
  }

  /** Checks if folder or folder exists. */
  public boolean fileOrFolderExists(String path) {
    return Files.isRegularFile(fileSystem.getPath(expandHomeDirectory(path)))
            || Files.isDirectory(fileSystem.getPath(expandHomeDirectory(path)));
  }

  /** Checks if folder is empty or doesn't exist. Returns false for files. */
  public boolean folderEmptyOrNotExists(String path) throws IOException {
    if (fileExists(path)) {
      return false;
    }
    if (!folderExists(path)) {
      return true;
    }
    return listContents(path).isEmpty();
  }

  /** Creates directories in path if none exist. */
  public void mkdirs(String path) {
    try {
      Files.createDirectories(fileSystem.getPath(expandHomeDirectory(path)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Joins paths */
  public String joinPaths(String first, String... more) {
    return fileSystem.getPath(first, more).toAbsolutePath().toString();
  }

  /** Get the current working directory */
  public String getCurrentWorkingDirectory() {
    if (System.getenv("BUILD_WORKSPACE_DIRECTORY") != null) {
      return System.getenv("BUILD_WORKSPACE_DIRECTORY");
    } else {
      return fileSystem.getPath("").toAbsolutePath().toString();
    }
  }

  /** Get the current working directory */
  public String getCurrentWorkingDirectoryName() {
    return fileSystem.getPath("").toAbsolutePath().getFileName().toString();
  }

  /** Gets file and folder names in path. */
  public ImmutableList<String> listContents(String path) throws IOException {
    try (Stream<Path> paths = Files.list(fileSystem.getPath(expandHomeDirectory(path)))) {
      return ImmutableList.sortedCopyOf(
          paths.map(
              absolutePath -> absolutePath.getFileName().toString())
              .collect(Collectors.toList()));
    }
  }

  /** 
   * Gets file and folder absolute paths recursively.
   * Throws NoSuchFileException if directory doesn't exist
   */
  public ImmutableList<String> listContentsRecursively(String path) throws IOException {
    try (Stream<Path> paths = Files.find(
        fileSystem.getPath(expandHomeDirectory(path)),
        100000, // Folder depth
        (unused, unused2) -> true)) {
      return ImmutableList.sortedCopyOf(
          paths.map(
              absolutePath -> absolutePath.toString())
              .collect(Collectors.toList()));
    }
} 

  /** Reads a text file. */
  public String readFile(String path) throws IOException {
    return String.join("\n", Files.readAllLines(fileSystem.getPath(expandHomeDirectory(path))));
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
    File file = new File(path);
    if (file.getParent() != null) {
      mkdirs(file.getParent());
    }
    proto.writeTo(Files.newOutputStream(fileSystem.getPath(expandHomeDirectory(path))));
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
    InputStream input = Files.newInputStream(fileSystem.getPath(expandHomeDirectory(path)));
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

  public void copyDirectoryToDirectory(String source, String destination, String ignored)
      throws IOException {
    final Path sourcePath = Paths.get(source);
    final Path targetPath = Paths.get(destination);
    Files.walkFileTree(
        sourcePath,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
              throws IOException {
            if (ignored != null) {
              if (Pattern.matches(ignored, dir.getFileName().toString())) {
                return FileVisitResult.CONTINUE;
              }
            }
            Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
              throws IOException {
            if (ignored != null) {
              if (Pattern.matches(ignored, file.getFileName().toString())) {
                return FileVisitResult.CONTINUE;
              }
            }
            Files.copy(file, targetPath.resolve(sourcePath.relativize(file)));
            return FileVisitResult.CONTINUE;
          }
        });
  }

  public void copyDirectoryToDirectory(String source, String destination) throws IOException {
    copyDirectoryToDirectory(source, destination, null);
  }
}
