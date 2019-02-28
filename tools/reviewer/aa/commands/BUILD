package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_library(
    name = "commands",
    srcs = glob([
        "*.java",
        "checks/*.java",
    ]),
    runtime_deps = [
        "//tools:buildifier",
        "//tools/formatter:clang_format",
    ],
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
        "//tools/reviewer:reviewer_registry_java_proto",
        "//tools/reviewer/aa:config_java_proto",
        "//tools/reviewer/local_server/service:auth_service_grpc",
        "//tools/reviewer/local_server/service:auth_service_java_proto",
        "//tools/reviewer/local_server/service:code_review_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)

checkstyle_test(
    name = "commands-checkstyle",
    target = ":commands",
)
