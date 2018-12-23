java_library(
    name = "protobuf_java",
    exports = [
        "//external:jar/mvncom/google/protobuf/protobuf_java"
    ],
    licenses = [
        "permissive"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "protobuf_java_util",
    exports = [
        "//external:jar/mvncom/google/protobuf/protobuf_java_util"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/code/gson:gson",
        "//third_party/maven/com/google/guava:guava",
        ":protobuf_java"
    ],
    visibility = [
        "//visibility:public"
    ]
)


