# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_test(
    name = "init_command_test",
    srcs = ["InitCommandTest.java"],
    test_class = "com.google.startupos.tools.reviewer.aa.commands.tests.InitCommandTest",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/jimfs",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit",
        "//tools/reviewer/aa:aa_module",
        "//tools/reviewer/aa/commands",
    ],
)

checkstyle_test(
    name = "init_command_test-checkstyle",
    target = ":init_command_test",
)

java_test(
    name = "workspace_command_test",
    srcs = ["WorkspaceCommandTest.java"],
    test_class = "com.google.startupos.tools.reviewer.aa.commands.tests.WorkspaceCommandTest",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/jimfs",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit",
        "//tools/reviewer/aa:aa_module",
        "//tools/reviewer/aa/commands",
    ],
)

checkstyle_test(
    name = "workspace_command_test-checkstyle",
    target = ":workspace_command_test",
)
