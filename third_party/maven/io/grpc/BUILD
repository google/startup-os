java_library(
    name = "grpc_auth",
    exports = [
        "//external:jar/mvnio/grpc/grpc_auth"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/auth:google_auth_library_credentials",
        ":grpc_core"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "grpc_context",
    exports = [
        "//external:jar/mvnio/grpc/grpc_context"
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
        "//external:jar/mvnio/grpc/grpc_core"
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
        "//third_party/maven/org/codehaus/mojo:animal_sniffer_annotations",
        ":grpc_context"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "grpc_netty",
    exports = [
        "//external:jar/mvnio/grpc/grpc_netty"
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
    name = "grpc_netty_shaded",
    exports = [
        "//external:jar/mvnio/grpc/grpc_netty_shaded"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        ":grpc_core"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "grpc_protobuf",
    exports = [
        "//external:jar/mvnio/grpc/grpc_protobuf"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/api/grpc:proto_google_common_protos",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
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
        "//external:jar/mvnio/grpc/grpc_protobuf_lite"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/guava:guava",
        ":grpc_core"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "grpc_services",
    exports = [
        "//external:jar/mvnio/grpc/grpc_services"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
        "//third_party/maven/com/google/re2j:re2j",
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
        "//external:jar/mvnio/grpc/grpc_stub"
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


