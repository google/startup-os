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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scans for {@code Flag} fields using reflection and saves their data.
 *
 * <p>This class also enforces the following: - No two flags have the same flag name. - Flag
 * variable names are camelCase. - Flag names are underscore_case.
 */
public class ClassScanner {
  private static final Logger log = LoggerFactory.getLogger(ClassScanner.class);

  public void scanPackage(String packagePrefix, Map<String, FlagData> flags) {
    // TODO - figure out configuration builder.
    Reflections reflections =
        new Reflections(
            new ConfigurationBuilder()
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(packagePrefix)))
                .setUrls(ClasspathHelper.forPackage(packagePrefix))
                .setScanners(new FieldAnnotationsScanner()));

    Set<Field> fields = reflections.getFieldsAnnotatedWith(FlagDesc.class);
    for (Field field : fields) {
      Class<?> clazz = field.getType();
      if (!Flag.class.isAssignableFrom(clazz)) {
        throw new IllegalArgumentException("Annotation '" + field + "' does not annotate a flag.");
      }
      if ((field.getModifiers() & Modifier.STATIC) == 0) {
        throw new IllegalArgumentException("Flag '" + field + "' should be static but is not.");
      }

      Class<?> declaringClass = field.getDeclaringClass();
      Flag<?> flag = getFlagMember(declaringClass, field);
      FlagData flagData = createFlagData(declaringClass, field, flag);
      if (flags.containsKey(flagData.getName())) {
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
    // TOOD: Get nicer string for field
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
            .setDescription(desc.description())
            .setRequired(desc.required());
    if (flag.getDefault() != null) {
      result.setDefault(flag.getDefault().toString());
    }
    return result.build();
  }

  private boolean isBooleanFlag(Field field) {
    if (field.getGenericType() instanceof ParameterizedType) {
      ParameterizedType flagType = (ParameterizedType) field.getGenericType();
      Type[] innerTypes = flagType.getActualTypeArguments();
      if (innerTypes.length != 1) {
        log.warn(
            "Cannot check if flag '"
                + field
                + "' is of boolean type. It"
                + " has "
                + innerTypes.length
                + " inner types instead of 1.");
      } else if (innerTypes[0].getTypeName().equals("java.lang.Boolean")) {
        return true;
      }
    } else {
      log.warn(
          "Cannot check if flag '"
              + field
              + "' is of boolean type. It's"
              + " not a ParameterizedType");
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
