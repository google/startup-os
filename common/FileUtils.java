package il.org.hasadna.common;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Arrays;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import com.google.protobuf.TextFormat;

/** File utils */
public class FileUtils {

  /** Replace the ~ in e.g ~/path with the home directory. */
  public static String expandHomeDirectory(String path) {
    if (path.startsWith("~" + File.separator)) {
      path = System.getProperty("user.home") + path.substring(1);
    }
    return path;
  }

  /** Reads a prototxt file into a proto. */
  public static Message readPrototxt(String path, Message.Builder builder)
      throws IOException, ParseException {
    String protoText = Files.toString(new File(expandHomeDirectory(path)), UTF_8);
    TextFormat.merge(protoText, builder);
    return builder.build();
  }

  /** Reads a prototxt file into a proto, rethrows exceptions as unchecked. */
  public static Message readPrototxtUnchecked(String path, Message.Builder builder) {
    try {
      return readPrototxt(path, builder);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Writes a proto to file. */
  public static void writePrototxt(Message proto, String path) throws IOException {
    mkdirs(path);
    String text = TextFormat.printToUnicodeString(proto);
    java.nio.file.Files.write(Paths.get(expandHomeDirectory(path)), text.getBytes());
  }

  /** Writes a proto to file, rethrows exceptions as unchecked. */
  public static void writePrototxtUnchecked(Message proto, String path) {
    try {
      writePrototxt(proto, path);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Checks if file exists. Returns false for folders. */
  public static boolean fileExists(String path) {
    File file = new File(expandHomeDirectory(path));
    return file.exists() && file.isFile();
  }

  /** Checks if folder exists. Returns false for files. */
  public static boolean folderExists(String path) {
    File file = new File(expandHomeDirectory(path));
    return file.exists() && file.isDirectory();
  }

  /** Checks if folder or folder exists. */
  public static boolean fileOrFolderExists(String path) {
    File file = new File(expandHomeDirectory(path));
    return file.exists();
  }

  /** Creates directories in path if none exist. */
  public static void mkdirs(String path) {
    try {
      Files.createParentDirs(new File(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Gets filenames in path. */
  public static ImmutableList<String> getFiles(String path) {
    String[] files = Paths.get(path).toFile().list();
    if (files.length == 0) {
      return ImmutableList.of();
    }
    return ImmutableList.sortedCopyOf(Arrays.asList(files));
  }

  /** Reads a text file. */
  public static String readFile(String path) throws IOException {
    try {
      return Files.toString(new File(expandHomeDirectory(path)), UTF_8);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Reads a text file, rethrows exceptions as unchecked. */
  public static String readFileUnchecked(String path) {
    try {
      return readFile(path);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
