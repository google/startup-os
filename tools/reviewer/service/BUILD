package(default_visibility = ["//visibility:public"])
load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_library")

java_proto_library(
    name = "code_review_lib",
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
        ":code_review_lib",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "@org_pubref_rules_protobuf//java:netty_runtime_deps",
    ],
)

java_library(
    name = "grpc_client",
    srcs = [
        "CodeReviewClient.java",
    ],
    deps = [
        ":code_review_lib",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "@org_pubref_rules_protobuf//java:grpc_compiletime_deps",
    ],
)
