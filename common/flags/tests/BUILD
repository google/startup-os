# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_library(
    name = "flag_desc_test_class",
    srcs = ["FlagDescTestClass.java"],
    deps = ["//common/flags"],
)

checkstyle_test(
    name = "flag_desc_test_class-checkstyle",
    target = ":flag_desc_test_class",
)

java_library(
    name = "flag_test_class",
    srcs = ["FlagTestClass.java"],
    deps = ["//common/flags"],
)

checkstyle_test(
    name = "flag_test_class-checkstyle",
    target = ":flag_test_class",
)

java_test(
    name = "flags_test",
    srcs = ["FlagsTest.java"],
    test_class = "com.google.startupos.common.flags.tests.FlagsTest",
    deps = [
        ":flag_desc_test_class",
        ":flag_test_class",
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/junit",
    ],
)

checkstyle_test(
    name = "flags_test-checkstyle",
    target = ":flags_test",
)
