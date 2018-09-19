package(default_visibility = ["//visibility:public"])

java_library(
    name = "github_reader",
    srcs = [
        "GitHubReader.java",
    ],
    deps = [
        "//common",
        "//common/repo:repo_java_proto",
        "//common/repo:repo_proto",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/org/kohsuke:github_api",
        "//tools/reviewer/local_server/service:code_review_java_proto",
        "//tools/reviewer/local_server/service:code_review_proto",
    ],
)

java_binary(
    name = "sync_tool",
    srcs = [
        "SyncTool.java",
    ],
    main_class = "com.google.startupos.tools.reviewer.job.sync.SyncTool",
    deps = [
        ":github_reader",
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/org/kohsuke:github_api",
        "//tools/reviewer/local_server/service:code_review_java_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
        "//tools/reviewer/local_server/service:code_review_proto",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)
