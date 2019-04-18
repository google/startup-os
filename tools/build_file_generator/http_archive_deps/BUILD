# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_library(
    name = "http_archive_deps_generator",
    srcs = ["HttpArchiveDepsGenerator.java"],
    deps = [
        "//common",
        "//common/repo",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/build_file_generator:build_file_generator_java_proto",
        "//tools/build_file_generator:build_file_parser",
        "//tools/build_file_generator:java_class_analyzer",
        "//tools/build_file_generator:proto_file_analyzer",
    ],
)

checkstyle_test(
    name = "http_archive_deps_generator-checkstyle",
    target = ":http_archive_deps_generator",
)

java_library(
    name = "workspace_parser",
    srcs = ["WorkspaceParser.java"],
    deps = [
        "//common",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/build_file_generator:build_file_generator_java_proto",
    ],
)

checkstyle_test(
    name = "workspace_parser-checkstyle",
    target = ":workspace_parser",
)

java_binary(
    name = "http_archive_deps_tool",
    srcs = ["HttpArchiveDepsTool.java"],
    main_class = "com.google.startupos.tools.build_file_generator.http_archive_deps.HttpArchiveDepsTool",
    deps = [
        ":http_archive_deps_generator",
        ":workspace_parser",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/build_file_generator:build_file_generator_java_proto",
    ],
)

checkstyle_test(
    name = "http_archive_deps_tool-checkstyle",
    target = ":http_archive_deps_tool",
)
