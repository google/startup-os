# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_binary(
  name = "rules_updater",
  srcs = ["RulesUpdater.java"],
  main_class = "com.google.startupos.tools.reviewer.rules_updater.RulesUpdater",
  deps = [
    "//common/flags:flags",
    "//common:common",
    "//common:dagger_with_annotation_processor",
    "//tools/reviewer:reviewer_java_proto",
    "//third_party/maven/com/google/flogger:flogger_system_backend",
  ],
)

checkstyle_test(
  name = "rules_updater-checkstyle",
  target = ":rules_updater",
)

