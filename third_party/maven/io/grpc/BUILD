java_library(
    name = "grpc_auth",
    exports = [
        "//external:jar/io/grpc/grpc_auth"
    ],
    licenses = [
        "permissive"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "grpc_context",
    exports = [
        "//external:jar/io/grpc/grpc_context"
    ],
    licenses = [
        "permissive"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "grpc_core",
    exports = [
        "//external:jar/io/grpc/grpc_core"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/code/gson:gson",
        "//third_party/maven/com/google/errorprone:error_prone_annotations",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/io/opencensus:opencensus_api",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        ":grpc_context"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "grpc_netty",
    exports = [
        "//external:jar/io/grpc/grpc_netty"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/io/netty:netty_codec_http2",
        "//third_party/maven/io/netty:netty_handler_proxy",
        ":grpc_core"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "grpc_protobuf",
    exports = [
        "//external:jar/io/grpc/grpc_protobuf"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/api/grpc:proto_google_common_protos",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
        ":grpc_core",
        ":grpc_protobuf_lite"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "grpc_protobuf_lite",
    exports = [
        "//external:jar/io/grpc/grpc_protobuf_lite"
    ],
    licenses = [
        "permissive"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "grpc_services",
    exports = [
        "//external:jar/io/grpc/grpc_services"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/instrumentation:instrumentation_api",
        ":grpc_protobuf",
        ":grpc_stub"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "grpc_stub",
    exports = [
        "//external:jar/io/grpc/grpc_stub"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        ":grpc_core"
    ],
    visibility = [
        "//visibility:public"
    ]
)


