# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
    name = "full_github_pr_proto",
    srcs = ["full_github_pr.proto"],
)

proto_library(
    name = "github_api_proto",
    srcs = ["github_api.proto"],
    deps = ["//tools/reviewer/job/sync:full_github_pr_proto"],
)

java_proto_library(
    name = "full_github_pr_java_proto",
    deps = [":full_github_pr_proto"],
)

java_proto_library(
    name = "github_api_java_proto",
    deps = [":github_api_proto"],
)

java_library(
    name = "diff_converter",
    srcs = ["DiffConverter.java"],
    deps = [
        ":full_github_pr_java_proto",
        ":line_number_converter",
        ":reviewer_client",
        "//common/repo",
        "//third_party/maven/com/google/guava",
        "//tools/reviewer/local_server/service:code_review_java_proto",
    ],
)

checkstyle_test(
    name = "diff_converter-checkstyle",
    target = ":diff_converter",
)

java_library(
    name = "github_client",
    srcs = ["GithubClient.java"],
    deps = [
        ":github_api_java_proto",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
    ],
)

checkstyle_test(
    name = "github_client-checkstyle",
    target = ":github_client",
)

java_library(
    name = "github_patch",
    srcs = ["GithubPatch.java"],
)

checkstyle_test(
    name = "github_patch-checkstyle",
    target = ":github_patch",
)

java_library(
    name = "github_reader",
    srcs = ["GithubReader.java"],
    deps = [
        ":full_github_pr_java_proto",
        ":github_api_java_proto",
        ":github_client",
    ],
)

checkstyle_test(
    name = "github_reader-checkstyle",
    target = ":github_reader",
)

java_library(
    name = "github_sync",
    srcs = ["GithubSync.java"],
    deps = [
        ":diff_converter",
        ":full_github_pr_java_proto",
        ":github_client",
        ":github_reader",
        ":github_writer",
        ":reviewer_client",
        "//common/repo",
        "//third_party/maven/com/google/guava",
        "//tools/reviewer/local_server/service:code_review_java_proto",
    ],
)

checkstyle_test(
    name = "github_sync-checkstyle",
    target = ":github_sync",
)

java_library(
    name = "github_writer",
    srcs = ["GithubWriter.java"],
    deps = [
        ":full_github_pr_java_proto",
        ":github_api_java_proto",
        ":github_client",
        "//third_party/maven/com/google/flogger",
    ],
)

checkstyle_test(
    name = "github_writer-checkstyle",
    target = ":github_writer",
)

java_library(
    name = "line_number_converter",
    srcs = ["LineNumberConverter.java"],
    deps = [":github_patch"],
)

checkstyle_test(
    name = "line_number_converter-checkstyle",
    target = ":line_number_converter",
)

java_library(
    name = "reviewer_client",
    srcs = ["ReviewerClient.java"],
    deps = [
        ":full_github_pr_java_proto",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/io/grpc:grpc_core",
        "//tools/reviewer/local_server/service:code_review_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
    ],
)

checkstyle_test(
    name = "reviewer_client-checkstyle",
    target = ":reviewer_client",
)

java_binary(
    name = "github_sync_tool",
    srcs = ["GithubSyncTool.java"],
    main_class = "com.google.startupos.tools.reviewer.job.sync.GithubSyncTool",
    deps = [
        ":github_client",
        ":github_sync",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/flags",
        "//common/repo",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "github_sync_tool-checkstyle",
    target = ":github_sync_tool",
)
