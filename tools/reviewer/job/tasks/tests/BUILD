# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_binary(
    name = "ci_task_tool",
    srcs = ["CiTaskTool.java"],
    main_class = "com.google.startupos.tools.reviewer.job.tasks.tests.CiTaskTool",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/firestore",
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer:reviewer_constants",
        "//tools/reviewer:reviewer_java_proto",
        "//tools/reviewer/local_server/service:auth_service",
    ],
)

checkstyle_test(
    name = "ci_task_tool-checkstyle",
    target = ":ci_task_tool",
)

java_binary(
    name = "submitter_task_tool",
    srcs = ["SubmitterTaskTool.java"],
    main_class = "com.google.startupos.tools.reviewer.job.tasks.tests.SubmitterTaskTool",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/firestore",
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer:reviewer_constants",
        "//tools/reviewer:reviewer_java_proto",
        "//tools/reviewer/local_server/service:auth_service",
        "//tools/reviewer/local_server/service:code_review_java_proto",
    ],
)

checkstyle_test(
    name = "submitter_task_tool-checkstyle",
    target = ":submitter_task_tool",
)
