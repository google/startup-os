package(default_visibility = ["//visibility:private"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
    name = "person_proto",
    srcs = ["person.proto"],
)

java_proto_library(
    name = "person_java_proto",
    deps = [":person_proto"],
)

java_binary(
    name = "person_tool",
    srcs = [
        "PersonTool.java",
    ],
    main_class = "com.google.startupos.tutorials.protorename.PersonTool",
    deps = [
        ":person_java_proto",
        ":person_proto",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "person_tool-checkstyle",
    target = ":person_tool",
)
