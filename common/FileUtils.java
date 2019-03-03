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
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/** File utils. */
// TODO Disallow `java.nio.file.Paths` using error_prone, since it bypasses the injected FileSystem.
@Singleton
public class FileUtils {
  private final String userHome;
  private final FileSystem fileSystem;

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

  /** Reads a prototxt from a string into a proto. */
  public Message readPrototxtFromString(String prototxt, Message.Builder builder)
      throws IOException {
    TextFormat.merge(prototxt, builder);
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
    String fileContent = TextFormat.printToUnicodeString(proto);
    writeString(fileContent, path);
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
    Files.write(fileSystem.getPath(expandHomeDirectory(path)), text.getBytes(UTF_8));
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

  /** Joins to an absolute paths. */
  public String joinToAbsolutePath(String first, String... more) {
    return fileSystem.getPath(first, more).toAbsolutePath().toString();
  }

  /** Joins paths. */
  public String joinPaths(String first, String... more) {
    return fileSystem.getPath(first, more).toString();
  }

  /** Get the current working directory. */
  public String getCurrentWorkingDirectory() {
    if (System.getenv("BUILD_WORKSPACE_DIRECTORY") != null) {
      return System.getenv("BUILD_WORKSPACE_DIRECTORY");
    } else {
      return fileSystem.getPath("").toAbsolutePath().toString();
    }
  }

  /** Get the current working directory. */
  public String getCurrentWorkingDirectoryName() {
    return fileSystem.getPath("").toAbsolutePath().getFileName().toString();
  }

  /** Gets file and folder names in path. */
  public ImmutableList<String> listContents(String path) throws IOException {
    try (Stream<Path> paths = Files.list(fileSystem.getPath(expandHomeDirectory(path)))) {
      return ImmutableList.sortedCopyOf(
          paths
              .map(absolutePath -> absolutePath.getFileName().toString())
              .collect(Collectors.toList()));
    }
  }

  /** Gets subfolder names in path. Throws IllegalStateException if path is not a folder. */
  public ImmutableList<String> listSubfolders(String path) throws IOException {
    if (!folderExists(path)) {
      throw new IllegalStateException("Folder does not exist");
    }
    return ImmutableList.sortedCopyOf(
        listContents(path)
            .stream()
            .filter(x -> folderExists(joinPaths(path, x)))
            .collect(Collectors.toList()));
  }

  /**
   * Gets file and folder absolute paths recursively. Throws NoSuchFileException if directory
   * doesn't exist.
   */
  public ImmutableList<String> listContentsRecursively(String path) throws IOException {
    try (Stream<Path> paths =
        Files.find(
            fileSystem.getPath(expandHomeDirectory(path)),
            100000, // Folder depth
            (unused, unused2) -> true)) {
      return ImmutableList.sortedCopyOf(paths.map(Path::toString).collect(Collectors.toList()));
    }
  }

  /** Reads a text file. */
  public String readFile(String path) throws IOException {
    return new String(Files.readAllBytes(fileSystem.getPath(expandHomeDirectory(path))), UTF_8);
  }

  /** Reads a text file, rethrows exceptions as unchecked. */
  public String readFileUnchecked(String path) {
    try {
      return readFile(path);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Reads a text file, rethrows exceptions as unchecked. */
  public String readFileFromResourcesUnchecked(String path) {
    try {
      return Resources.toString(Resources.getResource(path), UTF_8);
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

  /**
   * Copies a directory to another directory. Creates target directory if needed and doesn't copy
   * symlinks.
   */
  public void copyDirectoryToDirectory(String source, String destination, String... ignored)
      throws IOException {
    final Path sourcePath = fileSystem.getPath(source);
    final Path targetPath = fileSystem.getPath(destination);

    Files.walkFileTree(
        sourcePath,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
              throws IOException {
            final String dirPath = dir.toAbsolutePath().toString();
            if (ignored.length != 0) {
              for (String item : ignored) {
                if (dirPath.contains(item)) {
                  return FileVisitResult.SKIP_SUBTREE;
                }
                if (Pattern.matches(item, dir.getFileName().toString())) {
                  return FileVisitResult.SKIP_SUBTREE;
                }
              }
            }
            Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
              throws IOException {
            if (ignored.length != 0) {
              for (String itemForIgnore : ignored) {
                if (Pattern.matches(itemForIgnore, file.getFileName().toString())) {
                  return FileVisitResult.CONTINUE;
                }
              }
            }
            if (Files.isSymbolicLink(file)) {
              return FileVisitResult.CONTINUE;
            }
            Files.copy(file, targetPath.resolve(sourcePath.relativize(file)));
            return FileVisitResult.CONTINUE;
          }
        });
  }

  public void copyDirectoryToDirectory(String source, String destination) throws IOException {
    copyDirectoryToDirectory(source, destination, new String[0]);
  }

  /** Deletes all files and folders in directory. Target directory is deleted. */
  public void deleteDirectory(String path) throws IOException {
    deleteDirectoryContents(path, true);
  }

  /**
   * Deletes all files and folders in directory. Target directory is deleted. Rethrows exceptions as
   * unchecked.
   */
  public void deleteDirectoryUnchecked(String path) {
    try {
      deleteDirectory(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Deletes file or folder. */
  public void deleteFileOrDirectoryIfExists(String path) throws IOException {
    Files.deleteIfExists(fileSystem.getPath(expandHomeDirectory(path)));
  }

  /** Deletes file or folder, rethrows exceptions as unchecked. */
  public void deleteFileOrDirectoryIfExistsUnchecked(String path) {
    try {
      deleteFileOrDirectoryIfExists(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Deletes all files and folders inside directory. Target directory is not deleted. */
  public void clearDirectory(String path) throws IOException {
    deleteDirectoryContents(path, false);
  }

  /**
   * Deletes all files and folders inside directory. Target directory is not deleted. Rethrows
   * exceptions as unchecked.
   */
  public void clearDirectoryUnchecked(String path) {
    try {
      clearDirectory(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void deleteDirectoryContents(String path, boolean deleteTargetDirectory)
      throws IOException {
    final Path targetDirectory = fileSystem.getPath(expandHomeDirectory(path));
    Files.walkFileTree(
        targetDirectory,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exception)
              throws IOException {
            if (exception == null) {
              if (!deleteTargetDirectory && (targetDirectory == dir)) {
                // Do nothing
              } else {
                Files.delete(dir);
              }
              return FileVisitResult.CONTINUE;
            } else {
              throw exception;
            }
          }
        });
  }

  public static String streamToString(InputStream inputStream) throws IOException {
    return CharStreams.toString(new InputStreamReader(inputStream, UTF_8));
  }

  public String downloadUrl(String url) throws IOException {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
      return CharStreams.toString(in);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /** Writes a proto to zip archive. */
  // TODO: Think over how to instead of writing the file to disk and then copying, and then
  // deleting, write it once, directly to the Zip filesystem
  public void writePrototxtToZip(Message proto, String zipFilePath, String pathInsideZip)
      throws IOException {
    String fileContent = TextFormat.printToUnicodeString(proto);
    File tempPrototxt = File.createTempFile("temp_prototxt", ".prototxt");
    BufferedWriter writer = new BufferedWriter(new FileWriter(tempPrototxt));
    writer.write(fileContent);
    writer.close();

    Map<String, String> env = new HashMap<>();
    env.put("create", "true");

    URI uri = URI.create(String.format("jar:file:%s", joinPaths(zipFilePath)));
    try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
      Files.copy(
          fileSystem.getPath(expandHomeDirectory(tempPrototxt.getPath())),
          zipfs.getPath(pathInsideZip),
          StandardCopyOption.REPLACE_EXISTING);
    } finally {
      tempPrototxt.deleteOnExit();
    }
  }

  /** Reads a prototxt file into a proto from zip archive. */
  public Message readPrototxtFromZip(
      String zipFilePath, String pathInsideZip, Message.Builder builder) throws IOException {
    ZipFile zipFile =
        new ZipFile(expandHomeDirectory(joinPaths(getCurrentWorkingDirectory(), zipFilePath)));
    String content = streamToString(zipFile.getInputStream(zipFile.getEntry(pathInsideZip)));
    TextFormat.merge(content, builder);
    return builder.build();
  }

  /** Reads a prototxt file into a proto from zip archive, rethrows exceptions as unchecked. */
  public Message readPrototxtFromZipUnchecked(
      String zipFilePath, String pathInsideZip, Message.Builder builder) {
    try {
      return readPrototxtFromZip(zipFilePath, pathInsideZip, builder);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public File getFile(String path) {
    return new File(expandHomeDirectory(path));
  }
}

