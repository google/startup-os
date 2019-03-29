java_library(
    name = "opencensus_api",
    exports = [
        "//external:jar/mvnio/opencensus/opencensus_api"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/io/grpc:grpc_context"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "opencensus_contrib_grpc_metrics",
    exports = [
        "//external:jar/mvnio/opencensus/opencensus_contrib_grpc_metrics"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/guava:guava",
        ":opencensus_api"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "opencensus_contrib_grpc_util",
    exports = [
        "//external:jar/mvnio/opencensus/opencensus_contrib_grpc_util"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/io/grpc:grpc_core",
        ":opencensus_api"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "opencensus_contrib_http_util",
    exports = [
        "//external:jar/mvnio/opencensus/opencensus_contrib_http_util"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/guava:guava",
        ":opencensus_api"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)


