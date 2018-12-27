package(default_visibility = ["//visibility:public"])

java_test(
    name = "line_number_converter_test",
    srcs = [
        "LineNumberConverterTest.java",
    ],
    test_class = "com.google.startupos.tools.reviewer.job.sync.tests.LineNumberConverterTest",
    deps = [
        "//common",
        "//third_party/maven/junit",
        "//tools/reviewer/job/sync:github_sync",
    ],
)

java_test(
    name = "diff_converter_test",
    srcs = ["DiffConverterTest.java"],
    test_class = "com.google.startupos.tools.reviewer.job.sync.tests.DiffConverterTest",
    deps = [
        "//common",
        "//common/repo",
        "//common/repo:repo_java_proto",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/junit",
        "//third_party/maven/org/mockito:mockito_core",
        "//tools/reviewer/job/sync:full_github_pr_java_proto",
        "//tools/reviewer/job/sync:github_sync",
        "//tools/reviewer/local_server/service:code_review_java_proto",
    ],
)
