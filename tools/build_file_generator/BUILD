# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
    name = "build_file_generator_proto",
    srcs = ["build_file_generator.proto"],
)

java_proto_library(
    name = "build_file_generator_java_proto",
    deps = [":build_file_generator_proto"],
)

java_library(
    name = "build_file_generator",
    srcs = ["BuildFileGenerator.java"],
    deps = [
        ":build_file_generator_java_proto",
        ":build_file_generator_utils",
        ":build_file_parser",
        ":java_class_analyzer",
        ":proto_file_analyzer",
        "//common",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "build_file_generator-checkstyle",
    target = ":build_file_generator",
)

java_library(
    name = "build_file_generator_utils",
    srcs = ["BuildFileGeneratorUtils.java"],
    deps = [
        "//common",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "build_file_generator_utils-checkstyle",
    target = ":build_file_generator_utils",
)

java_library(
    name = "build_file_parser",
    srcs = ["BuildFileParser.java"],
    deps = [
        ":build_file_generator_java_proto",
        ":build_file_generator_utils",
        "//common",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "build_file_parser-checkstyle",
    target = ":build_file_parser",
)

java_library(
    name = "build_file_writer",
    srcs = ["BuildFileWriter.java"],
    deps = [
        ":build_file_generator_java_proto",
        "//common",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "build_file_writer-checkstyle",
    target = ":build_file_writer",
)

java_library(
    name = "java_class_analyzer",
    srcs = ["JavaClassAnalyzer.java"],
    deps = [
        ":build_file_generator_java_proto",
        "//common",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "java_class_analyzer-checkstyle",
    target = ":java_class_analyzer",
)

java_library(
    name = "proto_file_analyzer",
    srcs = ["ProtoFileAnalyzer.java"],
    deps = [
        ":build_file_generator_java_proto",
        "//common",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "proto_file_analyzer-checkstyle",
    target = ":proto_file_analyzer",
)

java_binary(
    name = "build_file_generator_tool",
    srcs = ["BuildFileGeneratorTool.java"],
    main_class = "com.google.startupos.tools.build_file_generator.BuildFileGeneratorTool",
    deps = [
        ":build_file_generator",
        ":build_file_writer",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "build_file_generator_tool-checkstyle",
    target = ":build_file_generator_tool",
)
