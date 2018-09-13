package(default_visibility = ["//visibility:private"])

proto_library(
    name = "persons_proto",
    srcs = ["persons.proto"],
)

java_proto_library(
    name = "persons_java_proto",
    deps = [":persons_proto"],
)

java_binary(
    name = "person_tool",
    srcs = [
        "PersonTool.java",
    ],
    main_class = "com.google.startupos.tutorials.protorename.PersonTool",
    deps = [
        ":persons_java_proto",
        ":persons_proto",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)
