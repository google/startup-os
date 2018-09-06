package(default_visibility = ["//visibility:private"])

proto_library(
    name = "person_proto",
    srcs = ["person.proto"],
)

java_proto_library(
    name = "person_java_proto",
    deps = [":person_proto"],
)

java_binary(
    name = "person_reader",
    srcs = [
        "PersonReader.java",
    ],
    data = ["person.pb"],
    main_class = "tutorials.proto_rename.PersonReader",
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

java_binary(
    name = "person_writer",
    srcs = [
        "PersonWriter.java",
    ],
    data = ["person.pb"],
    main_class = "tutorials.proto_rename.PersonWriter",
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
