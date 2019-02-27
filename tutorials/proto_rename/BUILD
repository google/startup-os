# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
  name = "person_proto",
  srcs = ["person.proto"],
)

java_proto_library(
  name = "person_java_proto",
  deps = [":person_proto"],
)

java_binary(
  name = "person_tool",
  srcs = ["PersonTool.java"],
  main_class = "com.google.startupos.tutorials.proto_rename.PersonTool",
  deps = [
    "//common:common",
    "//common:dagger_with_annotation_processor",
    "//third_party/maven/javax/inject:javax_inject",
    ":person_java_proto",
    "//third_party/maven/com/google/flogger:flogger_system_backend",
  ],
)

checkstyle_test(
  name = "person_tool-checkstyle",
  target = ":person_tool",
)

