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

package com.google.startupos.common.flags;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.FluentLogger;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

/**
 * Scans for {@code Flag} fields using reflection and saves their data.
 *
 * <p>This class also enforces the following: - No two flags have the same flag name. - Flag
 * variable names are camelCase. - Flag names are underscore_case.
 */
class ClassScanner {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  private String resourceName(String packageName) {
    String resourceName = packageName.replaceAll("[.\\\\]", "/");
    if (resourceName.startsWith("/")) {
      resourceName = resourceName.substring(1);
    }
    return resourceName;
  }

  private ClassFile getClassFile(JarFile jarFile, ZipEntry zipEntry) {
    if (!zipEntry.isDirectory()) {
      try (InputStream inputStream = jarFile.getInputStream(zipEntry)) {
        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(inputStream));
        return new ClassFile(dataInputStream);
      } catch (IOException e) {
        // Some zip entries are not class files
        return null;
      }
    }
    return null;
  }

  private JarFile getJarFile(URL url) throws IOException {
    JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
    urlConnection.setUseCaches(false);
    return urlConnection.getJarFile();
  }

  private ImmutableList<Field> getClassFields(Class clazz) {
    ImmutableList.Builder<Field> result = ImmutableList.builder();
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      FlagDesc flagDesc = field.getAnnotation(FlagDesc.class);
      if (flagDesc != null) {
        if (Flag.class.isAssignableFrom(field.getType())) {
          result.add(field);
        } else {
          throw new IllegalArgumentException(
              "Field annotated with FlagDesc does not inherit from Flag " + field);
        }
      }
    }
    return result.build();
  }

  private ImmutableList<Field> getPackageFields(String packageName) throws IOException {
    ImmutableList.Builder<Field> result = ImmutableList.builder();
    Set<String> classes = new HashSet<>();
    String resourceName = resourceName(packageName);
    Enumeration<URL> urls = ClassLoader.getSystemClassLoader().getResources(resourceName);
    while (urls.hasMoreElements()) {
      URL url = urls.nextElement();
      JarFile jarFile = getJarFile(url);
      Enumeration<? extends ZipEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        ClassFile classFile = getClassFile(jarFile, entries.nextElement());
        if (classFile == null) {
          continue;
        }
        if (!classes.add(classFile.getName())) {
          // We've already gone through this class - skip
          continue;
        }

        for (Object fieldInfoObject : classFile.getFields()) {
          FieldInfo fieldInfo = (FieldInfo) fieldInfoObject;
          AnnotationsAttribute annotationsAttribute =
              (AnnotationsAttribute) fieldInfo.getAttribute(AnnotationsAttribute.visibleTag);
          if (annotationsAttribute != null) {
            for (Annotation annotation : annotationsAttribute.getAnnotations()) {
              try {
                if (FlagDesc.class.getName().equals(annotation.getTypeName())) {
                  Class clazz = ClassLoader.getSystemClassLoader().loadClass(classFile.getName());
                  Field field = clazz.getDeclaredField(fieldInfo.getName());
                  if (Flag.class.isAssignableFrom(field.getType())) {
                    result.add(field);
                  } else {
                    throw new IllegalArgumentException(
                        "Field annotated with FlagDesc does not inherit from Flag " + field);
                  }
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        }
      }
    }
    return result.build();
  }

  void scanClass(Class clazz, Map<String, FlagData> flags) {
    scan(getClassFields(clazz), flags);
  }

  void scanPackage(String packagePrefix, Map<String, FlagData> flags) throws IOException {
    scan(getPackageFields(packagePrefix), flags);
  }

  private void scan(List<Field> fields, Map<String, FlagData> flags) {
    for (Field field : fields) {
      if ((field.getModifiers() & Modifier.STATIC) == 0) {
        throw new IllegalArgumentException("Flag '" + field + "' should be static but is not.");
      }
      Class<?> declaringClass = field.getDeclaringClass();
      Flag<?> flag = getFlagMember(declaringClass, field);
      FlagData flagData = createFlagData(declaringClass, field, flag);
      if (flags.containsKey(flagData.getName())
          && !declaringClass.getName().equals(flagData.getClassName())) {
        throw new IllegalArgumentException(
            String.format(
                "Flag '%s' is already defined here:\n%s", field, flags.get(flagData.getName())));
      }
      flags.put(flagData.getName(), flagData);
      flag.setName(flagData.getName());
      flag.setRequired(flagData.getRequired());
    }
  }

  private FlagData createFlagData(Class<?> declaringClass, Field field, Flag<?> flag) {
    FlagDesc[] flagDescs = field.getAnnotationsByType(FlagDesc.class);
    // TODO: Get nicer string for field
    if (flagDescs.length == 0) {
      throw new IllegalArgumentException("Flag '" + field + "' should be annotated with @FlagDesc");
    }
    if (flagDescs.length > 1) {
      throw new IllegalArgumentException(
          String.format(
              "Flag '%s' has %d @FlagDesc annotations instead of 1.", field, flagDescs.length));
    }
    FlagDesc desc = flagDescs[0];
    if (desc.name().isEmpty()) {
      throw new IllegalArgumentException("Flag '" + field + "' name should be specified.");
    }
    if (!isLowerCamel(field.getName())) {
      throw new IllegalArgumentException(
          String.format(
              "Flag '%s' variable name '%s' should be lowerCamelCase.", field, field.getName()));
    }
    String expectedUnderscoreName =
        CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
    if (!desc.name().equals(expectedUnderscoreName)) {
      throw new IllegalArgumentException(
          String.format(
              "Flag '%s' name '%s' should be %s.", field, desc.name(), expectedUnderscoreName));
    }

    FlagData.Builder result =
        FlagData.newBuilder()
            .setName(desc.name())
            .setClassName(declaringClass.getCanonicalName())
            .setIsBooleanFlag(isBooleanFlag(field))
            .setIsListFlag(isListFlag(field))
            .setDescription(desc.description())
            .setRequired(desc.required());
    if (flag.getDefault() != null) {
      if (result.getIsListFlag()) {
        result.setDefault(
            flag.getDefault().toString().replace("[", "").replace("]", "").replaceAll(", ", ","));
      } else {
        result.setDefault(flag.getDefault().toString());
      }
    }
    return result.build();
  }

  private boolean isBooleanFlag(Field field) {
    if (field.getGenericType() instanceof ParameterizedType) {
      ParameterizedType flagType = (ParameterizedType) field.getGenericType();
      Type[] innerTypes = flagType.getActualTypeArguments();
      if (innerTypes.length != 1) {
        log.atWarning()
            .log(
                "Cannot check if flag '%s' is of boolean type. It has %s inner types instead of 1.",
                field, innerTypes.length);
      } else if (innerTypes[0].getTypeName().equals("java.lang.Boolean")) {
        return true;
      }
    } else {
      log.atWarning()
          .log("Cannot check if flag '%s' is of boolean type. It's not a ParameterizedType", field);
    }
    return false;
  }

  private boolean isListFlag(Field field) {
    if (field.getGenericType() instanceof ParameterizedType) {
      ParameterizedType flagType = (ParameterizedType) field.getGenericType();
      Type[] innerTypes = flagType.getActualTypeArguments();
      if (innerTypes.length != 1) {
        log.atWarning()
            .log(
                "Cannot check if flag '%s' is of list type. It has %s inner types instead of 1.",
                field, innerTypes.length);
      } else if (innerTypes[0].getTypeName().startsWith("java.util.List")) {
        return true;
      }
    } else {
      log.atWarning()
          .log("Cannot check if flag '%s' is of list type. It's not a ParameterizedType", field);
    }
    return false;
  }

  private boolean isLowerCamel(String name) {
    return !name.contains("_")
        && !name.contains("-")
        && name.equals(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_CAMEL, name));
  }

  private Flag<?> getFlagMember(Class<?> declaringClass, Field field) {
    try {
      boolean accessible = field.isAccessible();
      field.setAccessible(true);
      Object value = field.get(declaringClass);
      field.setAccessible(accessible);
      if (value == null) {
        throw new IllegalArgumentException("Flag '" + field + "' value is not set");
      }
      return (Flag<?>) value;
    } catch (IllegalAccessException e) {
      // Should not happen, as we make the field accessible.
      throw new IllegalArgumentException("Flag '" + field + "' is not accessible", e);
    }
  }
}

