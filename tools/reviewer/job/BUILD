# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_binary(
    name = "reviewer_job",
    srcs = ["ReviewerJob.java"],
    main_class = "com.google.startupos.tools.reviewer.job.ReviewerJob",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/firestore",
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer/aa:aa_module",
        "//tools/reviewer/aa/commands",
        "//tools/reviewer/job/tasks:ci_task",
        "//tools/reviewer/job/tasks:reviewer_metadata_updater_task",
        "//tools/reviewer/job/tasks:submitter_task",
        "//tools/reviewer/job/tasks:task_executor",
        "//tools/reviewer/local_server",
        "//tools/reviewer/local_server:local_http_gateway",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)

checkstyle_test(
    name = "reviewer_job-checkstyle",
    target = ":reviewer_job",
)
