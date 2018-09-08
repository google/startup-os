package(default_visibility = ["//visibility:public"])

load("//third_party:java_grpc_library.bzl", "java_grpc_library")

proto_library(
    name = "auth_service_proto",
    srcs = ["auth_service.proto"],
)

java_proto_library(
    name = "auth_service_java_proto",
    deps = [":auth_service_proto"],
)

java_grpc_library(
    name = "auth_service_java_grpc",
    srcs = [":auth_service_proto"],
    deps = [
        ":auth_service_java_proto",
    ],
)

java_library(
    name = "auth_service",
    srcs = [
        "AuthService.java",
    ],
    deps = [
        ":auth_service_java_grpc",
        ":auth_service_java_proto",
        "//common",
        "//common/flags",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_stub",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/org/json",
    ],
)

proto_library(
    name = "code_review_proto",
    srcs = ["code_review.proto"],
    deps = [
        "//common:text_difference_proto",
        "//common/repo:repo_proto",
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
    name = "code_review_service",
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
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_stub",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer/local_server/service:auth_service",
    ],
)
