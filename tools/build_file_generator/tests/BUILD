# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_test(
    name = "java_class_analyzer_test",
    srcs = ["JavaClassAnalyzerTest.java"],
    resources = [
        "resources/another_proto.proto",
        "resources/test_proto.proto",
    ],
    test_class = "com.google.startupos.tools.build_file_generator.tests.JavaClassAnalyzerTest",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit",
        "//tools/build_file_generator:build_file_generator_java_proto",
        "//tools/build_file_generator:java_class_analyzer",
    ],
)

checkstyle_test(
    name = "java_class_analyzer_test-checkstyle",
    target = ":java_class_analyzer_test",
)

java_test(
    name = "proto_file_analyzer_test",
    srcs = ["ProtoFileAnalyzerTest.java"],
    resources = [
        "resources/another_proto.proto",
        "resources/test_proto.proto",
    ],
    test_class = "com.google.startupos.tools.build_file_generator.tests.ProtoFileAnalyzerTest",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit",
        "//tools/build_file_generator:build_file_generator_java_proto",
        "//tools/build_file_generator:proto_file_analyzer",
    ],
)

checkstyle_test(
    name = "proto_file_analyzer_test-checkstyle",
    target = ":proto_file_analyzer_test",
)
