package(default_visibility = ["//visibility:public"])
load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_library")

java_proto_library(
    name = "code_review_service_lib",
    protos = ["code_review.proto"],
    verbose = 1,
    with_grpc = True,
)

java_library(
    name = "grpc_service",
    srcs = [
        "CodeReviewService.java",
    ],
    deps = [
        ":code_review_service_lib",
        "//tools/local_server/service:auth_service",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "@org_pubref_rules_protobuf//java:netty_runtime_deps",
    ],
)
