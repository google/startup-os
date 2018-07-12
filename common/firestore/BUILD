package(default_visibility = ["//visibility:public"])

proto_library(
    name = "data_proto",
    srcs = ["data.proto"],
)

java_proto_library(
    name = "data_java_proto",
    deps = [":data_proto"],
)

java_library(
    name = "firestore",
    srcs = [
        "FirestoreClient.java",
        "FirestoreJsonFormat.java",
    ],
    deps = [
        ":data_java_proto",
        "//third_party/maven/com/google/code/gson",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
    ],
)
