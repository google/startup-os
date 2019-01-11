package(default_visibility = ["//visibility:private"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_test(
    name = "FlagsTest",
    srcs = [
        "FlagDescTestClass.java",
        "FlagTestClass.java",
        "FlagsTest.java",
    ],
    test_class = "com.google.startupos.common.flags.testpackage.FlagsTest",
    deps = [
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/junit",
        "//third_party/maven/org/mockito:mockito_core",
        "//third_party/maven/org/objenesis",
    ],
)

checkstyle_test(
    name = "FlagsTest-checkstyle",
    target = ":FlagsTest",
)
