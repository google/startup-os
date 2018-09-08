package(default_visibility = ["//visibility:public"])

java_library(
    name = "commands",
    srcs = glob([
        "*.java",
        "checks/*.java",
    ]),
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/firestore",
        "//common/flags",
        "//common/repo",
        "//common/repo:repo_java_proto",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_netty",
        "//third_party/maven/io/grpc:grpc_stub",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/formatter",
        "//tools/reviewer/aa:config_java_proto",
        "//tools/reviewer/local_server/service:auth_service_java_grpc",
        "//tools/reviewer/local_server/service:auth_service_java_proto",
        "//tools/reviewer/local_server/service:code_review_java_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)
