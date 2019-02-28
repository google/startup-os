# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_binary(
    name = "test_tool",
    srcs = ["TestTool.java"],
    main_class = "com.google.startupos.common.repo.tests.TestTool",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/repo",
        "//common/repo:repo_java_proto",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "test_tool-checkstyle",
    target = ":test_tool",
)

java_test(
    name = "git_repo_test",
    srcs = ["GitRepoTest.java"],
    test_class = "com.google.startupos.common.repo.tests.GitRepoTest",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/repo",
        "//common/repo:repo_java_proto",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit",
    ],
)

checkstyle_test(
    name = "git_repo_test-checkstyle",
    target = ":git_repo_test",
)
