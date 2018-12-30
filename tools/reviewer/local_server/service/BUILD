package(default_visibility = ["//visibility:public"])

load("//third_party:java_grpc_library.bzl", "java_grpc_library")
load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

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
    tags = ["checkstyle_ignore"],
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

checkstyle_test(
    name = "auth_service-checkstyle",
    allow_failure = 1,
    target = ":auth_service",
)

proto_library(
    name = "code_review_proto",
    srcs = ["code_review.proto"],
    deps = [
        "//common:text_difference_proto",
        "//common/repo:repo_proto",
        "//tools/reviewer:reviewer_proto",
    ],
)

java_proto_library(
    name = "code_review_java_proto",
    deps = [":code_review_proto"],
)

java_grpc_library(
    name = "code_review_java_grpc",
    srcs = [":code_review_proto"],
    tags = ["checkstyle_ignore"],
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
        "//common:text_difference_java_proto",
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
        "//tools/reviewer:reviewer_constants",
        "//tools/reviewer/local_server/service:auth_service",
    ],
)

checkstyle_test(
    name = "code_review_service-checkstyle",
    allow_failure = 1,
    target = ":code_review_service",
)
