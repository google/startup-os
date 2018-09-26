package(default_visibility = ["//visibility:public"])

java_library(
    name = "github_reader_and_writer",
    srcs = [
        "GitHubReader.java",
        "ReviewerBot.java",
    ],
    deps = [
        "//common",
        "//common/repo:repo_java_proto",
        "//common/repo:repo_proto",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/org/json",
        "//tools/reviewer/local_server/service:code_review_java_proto",
        "//tools/reviewer/local_server/service:code_review_proto",
    ],
)

java_binary(
    name = "github_sync_tool",
    srcs = [
        "GitHubSync.java",
    ],
    main_class = "com.google.startupos.tools.reviewer.job.sync.GitHubSync",
    deps = [
        ":github_reader_and_writer",
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//tools/reviewer/local_server/service:code_review_java_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
        "//tools/reviewer/local_server/service:code_review_proto",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)
