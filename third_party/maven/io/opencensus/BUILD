java_library(
    name = "opencensus_api",
    exports = [
        "//external:jar/io/opencensus/opencensus_api"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/io/grpc:grpc_context"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "opencensus_contrib_grpc_metrics",
    exports = [
        "//external:jar/io/opencensus/opencensus_contrib_grpc_metrics"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        ":opencensus_api"
    ],
    visibility = [
        "//visibility:public"
    ]
)


