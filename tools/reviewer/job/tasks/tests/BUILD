# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_binary(
  name = "ci_task_tool",
  srcs = ["CiTaskTool.java"],
  main_class = "com.google.startupos.tools.reviewer.job.tasks.tests.CiTaskTool",
  deps = [
    "//common/firestore:firestore",
    "//common/flags:flags",
    "//common:common",
    "//common:dagger_with_annotation_processor",
    "//third_party/maven/javax/inject:javax_inject",
    "//tools/reviewer/local_server/service:auth_service",
    "//tools/reviewer:reviewer_constants",
    "//tools/reviewer:reviewer_java_proto",
    "//third_party/maven/com/google/flogger:flogger_system_backend",
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
    "//common/firestore:firestore",
    "//common/flags:flags",
    "//common:common",
    "//common:dagger_with_annotation_processor",
    "//third_party/maven/javax/inject:javax_inject",
    "//tools/reviewer/local_server/service:auth_service",
    "//tools/reviewer/local_server/service:code_review_java_proto",
    "//tools/reviewer:reviewer_constants",
    "//tools/reviewer:reviewer_java_proto",
    "//third_party/maven/com/google/flogger:flogger_system_backend",
  ],
)

checkstyle_test(
  name = "submitter_task_tool-checkstyle",
  target = ":submitter_task_tool",
)

