# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//third_party:java_grpc_library.bzl", "java_grpc_library")
load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
  name = "auth_service_proto",
  srcs = ["auth_service.proto"],
)

proto_library(
  name = "code_review_proto",
  srcs = ["code_review.proto"],
  deps = [
    "//common/repo:repo_proto",
    "//common:text_difference_proto",
    "//tools/reviewer:reviewer_proto",
  ],
)

java_proto_library(
  name = "auth_service_java_proto",
  deps = [":auth_service_proto"],
)

java_proto_library(
  name = "code_review_java_proto",
  deps = [":code_review_proto"],
)

java_grpc_library(
  name = "auth_service_java_grpc",
  srcs = [":auth_service_proto"],
  tags = ["checkstyle_ignore"],
  deps = [":auth_service_java_proto"],
)

java_grpc_library(
  name = "code_review_java_grpc",
  srcs = [":code_review_proto"],
  tags = ["checkstyle_ignore"],
  deps = [":code_review_java_proto"],
)

java_library(
  name = "auth_service",
  srcs = ["AuthService.java"],
  deps = [
    "//common/flags:flags",
    "//common:common",
    "//third_party/maven/com/google/flogger:flogger",
    "//third_party/maven/io/grpc:grpc_core",
    "//third_party/maven/io/grpc:grpc_stub",
    "//third_party/maven/javax/inject:javax_inject",
    "//third_party/maven/org/json:json",
    ":auth_service_java_grpc",
    ":auth_service_java_proto",
  ],
)

checkstyle_test(
  name = "auth_service-checkstyle",
  target = ":auth_service",
)

java_library(
  name = "code_review_service",
  srcs = ["CodeReviewService.java"],
  deps = [
    "//common/firestore:firestore",
    "//common/repo:repo",
    "//common/repo:repo_java_proto",
    "//common:common",
    "//common:text_difference_java_proto",
    "//common:text_differencer",
    "//third_party/maven/com/google/flogger:flogger",
    "//third_party/maven/com/google/guava:guava",
    "//third_party/maven/io/grpc:grpc_core",
    "//third_party/maven/io/grpc:grpc_stub",
    "//third_party/maven/javax/inject:javax_inject",
    "//tools/reviewer:reviewer_constants",
    ":auth_service",
    ":code_review_java_grpc",
    ":code_review_java_proto",
  ],
)

checkstyle_test(
  name = "code_review_service-checkstyle",
  target = ":code_review_service",
)

