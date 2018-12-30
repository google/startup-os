package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
    name = "github_api_proto",
    srcs = [
        "github_api.proto",
    ],
    deps = [":full_github_pr_proto"],
)

java_proto_library(
    name = "github_api_java_proto",
    deps = [
        "github_api_proto",
    ],
)

proto_library(
    name = "full_github_pr_proto",
    srcs = ["full_github_pr.proto"],
)

java_proto_library(
    name = "full_github_pr_java_proto",
    deps = ["full_github_pr_proto"],
)

java_library(
    name = "github_sync",
    srcs = [
        "DiffConverter.java",
        "GithubClient.java",
        "GithubPatch.java",
        "GithubReader.java",
        "GithubSync.java",
        "GithubWriter.java",
        "LineNumberConverter.java",
        "ReviewerClient.java",
    ],
    deps = [
        ":full_github_pr_java_proto",
        ":github_api_java_proto",
        "//common",
        "//common/repo",
        "//common/repo:repo_java_proto",
        "//common/repo:repo_proto",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_netty",
        "//third_party/maven/org/slf4j:slf4j_simple",
        "//tools/reviewer/local_server/service:code_review_java_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
        "//tools/reviewer/local_server/service:code_review_proto",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)

checkstyle_test(
    name = "github_sync-checkstyle",
    target = ":github_sync",
)

java_binary(
    name = "github_sync_tool",
    srcs = [
        "GithubSyncTool.java",
    ],
    main_class = "com.google.startupos.tools.reviewer.job.sync.GithubSyncTool",
    deps = [
        ":full_github_pr_java_proto",
        ":github_sync",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/flags",
        "//common/repo",
        "//common/repo:repo_java_proto",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer/local_server/service:code_review_java_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
        "//tools/reviewer/local_server/service:code_review_proto",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)

checkstyle_test(
    name = "github_sync_tool-checkstyle",
    allow_failure = 1,
    target = ":github_sync_tool",
)
