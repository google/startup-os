java_library(
    name = "grpc_alts",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvnio/grpc/grpc_alts",
    ],
    runtime_deps = [
        ":grpc_auth",
        ":grpc_core",
        ":grpc_grpclb",
        ":grpc_netty_shaded",
        ":grpc_protobuf",
        ":grpc_stub",
        "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/org/apache/commons:commons_lang3",
    ],
)

java_library(
    name = "grpc_auth",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/mvnio/grpc/grpc_auth",
    ],
    runtime_deps = [
        ":grpc_core",
        "//third_party/maven/com/google/auth:google_auth_library_credentials",
    ],
)

java_library(
    name = "grpc_context",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvnio/grpc/grpc_context",
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
        "//external:jar/mvnio/grpc/grpc_core",
    ],
    runtime_deps = [
        ":grpc_context",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/code/gson",
        "//third_party/maven/com/google/errorprone:error_prone_annotations",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/io/opencensus:opencensus_api",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "//third_party/maven/org/checkerframework:checker_compat_qual",
        "//third_party/maven/org/codehaus/mojo:animal_sniffer_annotations",
    ],
)

java_library(
    name = "grpc_grpclb",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvnio/grpc/grpc_grpclb",
    ],
    runtime_deps = [
        ":grpc_core",
        ":grpc_protobuf",
        ":grpc_stub",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
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
        "//external:jar/mvnio/grpc/grpc_netty",
    ],
    runtime_deps = [
        ":grpc_core",
        "//third_party/maven/io/netty:netty_codec_http2",
        "//third_party/maven/io/netty:netty_handler_proxy",
    ],
)

java_library(
    name = "grpc_netty_shaded",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvnio/grpc/grpc_netty_shaded",
    ],
    runtime_deps = [
        ":grpc_core",
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
        "//external:jar/mvnio/grpc/grpc_protobuf",
    ],
    runtime_deps = [
        ":grpc_core",
        ":grpc_protobuf_lite",
        "//third_party/maven/com/google/api/grpc:proto_google_common_protos",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/org/checkerframework:checker_compat_qual",
    ],
)

java_library(
    name = "grpc_protobuf_lite",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvnio/grpc/grpc_protobuf_lite",
    ],
    runtime_deps = [
        ":grpc_core",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/org/checkerframework:checker_compat_qual",
    ],
)

java_library(
    name = "grpc_services",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/mvnio/grpc/grpc_services",
    ],
    runtime_deps = [
        ":grpc_protobuf",
        ":grpc_stub",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
        "//third_party/maven/com/google/re2j",
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
        "//external:jar/mvnio/grpc/grpc_stub",
    ],
    runtime_deps = [
        ":grpc_core",
    ],
)
