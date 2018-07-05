package(default_visibility = ["//visibility:public"])

load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")

proto_library(
    name = "code_review_proto",
    srcs = ["code_review.proto"],
    deps = [
        "//common:text_difference_proto",
        "//common/repo:repo_proto",
        "@com_google_protobuf//:empty_proto",
    ],
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
        "//common",
        "//common:auto_factory",
        "//common:text_differencer",
        "//common/firestore",
        "//common/flags",
        "//common/repo",
        "//common/repo:repo_java_proto",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_stub",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/local_server/service:auth_service",
        "@com_google_protobuf//:protobuf_java",
    ],
)

java_binary(
    name = "test_tool",
    srcs = [
        "TestTool.java",
    ],
    main_class = "com.google.startupos.tools.reviewer.service.TestTool",
    deps = [
        ":code_review_java_grpc",
        ":code_review_java_proto",
        ":service",
        "//common",
        "//common/repo:repo_java_proto",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_netty",
        "//third_party/maven/org/slf4j:slf4j_simple",
    ],
)
