# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
  name = "submitter_proto",
  srcs = ["submitter.proto"],
)

java_proto_library(
  name = "submitter_java_proto",
  deps = [":submitter_proto"],
)

java_library(
  name = "ci_task",
  srcs = ["CiTask.java"],
  deps = [
    "//common/firestore:firestore",
    "//common/repo:repo",
    "//common:common",
    "//third_party/maven/com/google/code/findbugs:jsr305",
    "//third_party/maven/com/google/flogger:flogger",
    "//third_party/maven/com/google/guava:guava",
    "//third_party/maven/javax/inject:javax_inject",
    "//tools/reviewer/local_server/service:code_review_java_proto",
    "//tools/reviewer:reviewer_constants",
    "//tools/reviewer:reviewer_java_proto",
    ":task",
  ],
)

checkstyle_test(
  name = "ci_task-checkstyle",
  target = ":ci_task",
)

java_library(
  name = "reviewer_metadata_updater_task",
  srcs = ["ReviewerMetadataUpdaterTask.java"],
  deps = [
    "//common/firestore:firestore",
    "//common/flags:flags",
    "//common/repo:repo",
    "//common:common",
    "//third_party/maven/com/google/flogger:flogger",
    "//third_party/maven/javax/inject:javax_inject",
    "//tools/reviewer:reviewer_registry_java_proto",
    ":task",
  ],
)

checkstyle_test(
  name = "reviewer_metadata_updater_task-checkstyle",
  target = ":reviewer_metadata_updater_task",
)

java_library(
  name = "submitter_task",
  srcs = ["SubmitterTask.java"],
  deps = [
    "//common/firestore:firestore",
    "//common/repo:repo",
    "//common:common",
    "//third_party/maven/com/google/code/findbugs:jsr305",
    "//third_party/maven/com/google/flogger:flogger",
    "//third_party/maven/com/google/guava:guava",
    "//third_party/maven/javax/inject:javax_inject",
    "//tools/reviewer/local_server/service:code_review_java_proto",
    "//tools/reviewer:reviewer_constants",
    "//tools/reviewer:reviewer_java_proto",
    ":task",
  ],
)

checkstyle_test(
  name = "submitter_task-checkstyle",
  target = ":submitter_task",
)

java_library(
  name = "task",
  srcs = ["Task.java"],
)

checkstyle_test(
  name = "task-checkstyle",
  target = ":task",
)

java_library(
  name = "task_executor",
  srcs = ["TaskExecutor.java"],
  deps = [
    "//third_party/maven/com/google/guava:guava",
    ":task",
  ],
)

checkstyle_test(
  name = "task_executor-checkstyle",
  target = ":task_executor",
)

