package(default_visibility = ["//visibility:public"])

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

java_binary(
    name = "github_sync_tool",
    srcs = [
        "GithubSync.java",
    ],
    main_class = "com.google.startupos.tools.reviewer.job.sync.GithubSync",
    deps = [
        ":full_github_pr_java_proto",
        ":github_sync",
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//tools/reviewer/local_server/service:code_review_java_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
        "//tools/reviewer/local_server/service:code_review_proto",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)
