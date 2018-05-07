package(default_visibility = ["//visibility:public"])

load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")

proto_library(
    name = "code_review_proto",
    srcs = ["code_review.proto"],
    deps = ["//common:text_difference_proto"],
)

java_proto_library(
    name = "code_review_java_proto",
    deps = [":code_review_proto"],
)

java_grpc_library(
    name = "code_review_java_grpc",
    srcs = [":code_review_proto"],
    deps = [
        ":code_review_java_proto",
    ],
)

java_library(
    name = "service",
    srcs = [
        "CodeReviewService.java",
    ],
    deps = [
        ":code_review_java_grpc",
        ":code_review_java_proto",
        "//common/firestore",
        "//common/flags",
        "//common:auto_factory",
        "//common:text_differencer",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "//third_party/maven/javax/inject:javax_inject",
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
        ":code_review_java_grpc",
        ":code_review_java_proto",
        "//common",
        "//external:jar/com/google/protobuf/protobuf_java",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "//third_party/maven/org/slf4j:slf4j_simple",
        "@org_pubref_rules_protobuf//java:grpc_compiletime_deps",
    ],
)
