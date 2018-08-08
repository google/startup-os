java_library(
    name = "opencensus_api",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/io/opencensus/opencensus_api",
    ],
    runtime_deps = [
        "//third_party/maven/com/google/guava",
        "//third_party/maven/io/grpc:grpc_context",
    ],
)

java_library(
    name = "opencensus_contrib_grpc_metrics",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/io/opencensus/opencensus_contrib_grpc_metrics",
    ],
    runtime_deps = [
        ":opencensus_api",
    ],
)
