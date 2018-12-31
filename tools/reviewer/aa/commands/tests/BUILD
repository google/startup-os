package(default_visibility = ["//visibility:private"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_test(
    name = "InitCommandTest",
    srcs = [
        "InitCommandTest.java",
    ],
    test_class = "com.google.startupos.tools.reviewer.aa.commands.tests.InitCommandTest",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/jimfs",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit",
        "//third_party/maven/org/mockito:mockito_core",
        "//third_party/maven/org/objenesis",
        "//tools/reviewer/aa:aa_module",
        "//tools/reviewer/aa/commands",
    ],
)

checkstyle_test(
    name = "InitCommandTest-checkstyle",
    target = ":InitCommandTest",
)

java_test(
    name = "WorkspaceCommandTest",
    srcs = [
        "WorkspaceCommandTest.java",
    ],
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
        "//third_party/maven/org/mockito:mockito_core",
        "//third_party/maven/org/objenesis",
        "//tools/reviewer/aa:aa_module",
        "//tools/reviewer/aa/commands",
    ],
)

checkstyle_test(
    name = "WorkspaceCommandTest-checkstyle",
    allow_failure = 1,
    target = ":WorkspaceCommandTest",
)
