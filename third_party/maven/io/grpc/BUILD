java_library(
    name = "grpc_auth",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/io/grpc/grpc_auth",
    ],
)

java_library(
    name = "grpc_context",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/io/grpc/grpc_context",
    ],
)

java_library(
    name = "grpc_core",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/io/grpc/grpc_core",
    ],
    runtime_deps = [
        ":grpc_context",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/code/gson",
        "//third_party/maven/com/google/errorprone:error_prone_annotations",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/io/opencensus:opencensus_api",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
    ],
)

java_library(
    name = "grpc_netty",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/io/grpc/grpc_netty",
    ],
    runtime_deps = [
        ":grpc_core",
        "//third_party/maven/io/netty:netty_codec_http2",
        "//third_party/maven/io/netty:netty_handler_proxy",
    ],
)

java_library(
    name = "grpc_protobuf",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/io/grpc/grpc_protobuf",
    ],
    runtime_deps = [
        ":grpc_core",
        ":grpc_protobuf_lite",
        "//third_party/maven/com/google/api/grpc:proto_google_common_protos",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
    ],
)

java_library(
    name = "grpc_protobuf_lite",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/io/grpc/grpc_protobuf_lite",
    ],
)

java_library(
    name = "grpc_stub",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/io/grpc/grpc_stub",
    ],
    runtime_deps = [
        ":grpc_core",
    ],
)
