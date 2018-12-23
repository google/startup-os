java_library(
    name = "protobuf_java",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/mvncom/google/protobuf/protobuf_java",
    ],
)

java_library(
    name = "protobuf_java_util",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/mvncom/google/protobuf/protobuf_java_util",
    ],
    runtime_deps = [
        ":protobuf_java",
        "//third_party/maven/com/google/code/gson",
        "//third_party/maven/com/google/guava",
    ],
)
