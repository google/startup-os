package(default_visibility = ["//visibility:public"])

load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_library")

java_proto_library(
    name = "service_lib",
    proto_deps = ["//common:text_difference_java_proto"],
    protos = ["code_review.proto"],
    verbose = 1,
    with_grpc = True,
)

java_library(
    name = "service",
    srcs = [
        "CodeReviewService.java",
    ],
    deps = [
        ":service_lib",
        "//common:text_differencer",
        "//common/firestore",
        "//common/flags",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "//tools/local_server/service:auth_service",
        "@org_pubref_rules_protobuf//java:netty_runtime_deps",
    ],
)

java_binary(
    name = "test_tool",
    srcs = [
        "TestTool.java",
    ],
    main_class = "com.google.startupos.tools.reviewer.service.TestTool",
    deps = [
        ":service",
        ":service_lib",
        "//common",
        "//external:jar/com/google/protobuf/protobuf_java",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "//third_party/maven/org/slf4j:slf4j_simple",
        "@org_pubref_rules_protobuf//java:grpc_compiletime_deps",
    ],
)
