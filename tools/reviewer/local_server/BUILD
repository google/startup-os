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
    name = "grpc_inprocess_server",
    srcs = [
        "CodeReviewInProcessServer.java",
    ],
    deps = [
        ":code_review_lib",
        ":grpc_service",
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

java_binary(
    name = "grpc_server",
    srcs = [
        "CodeReviewServer.java",
    ],
    main_class = "com.appstory.review_server.CodeReviewServer",
    deps = [
        ":code_review_lib",
        ":grpc_service",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "@org_pubref_rules_protobuf//java:netty_runtime_deps",
    ],
)

java_binary(
    name = "grpc_gateway",
    srcs = [
        "CodeReviewGateway.java",
    ],
    main_class = "com.appstory.review_server.CodeReviewGateway",
    deps = [
        ":code_review_lib",
        ":grpc_client",
        ":grpc_inprocess_server",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "@org_pubref_rules_protobuf//java:netty_runtime_deps",
    ],
)
