package(default_visibility = ["//visibility:public"])

java_test(
    name = "java_class_analyzer_test",
    srcs = [
        "JavaClassAnalyzerTest.java",
    ],
    test_class = "com.google.startupos.tools.buildfilegenerator.tests.JavaClassAnalyzerTest",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/junit",
        "//tools/build_file_generator",
        "//tools/build_file_generator:build_file_generator_java_proto",
    ],
)

java_test(
    name = "proto_file_analyzer_test",
    srcs = [
        "ProtoFileAnalyzerTest.java",
    ],
    resources = ["resources/test_proto.proto"],
    test_class = "com.google.startupos.tools.buildfilegenerator.tests.ProtoFileAnalyzerTest",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/junit",
        "//tools/build_file_generator",
        "//tools/build_file_generator:build_file_generator_java_proto",
    ],
)
