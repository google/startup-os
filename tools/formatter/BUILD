# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_binary(
    name = "formatter_tool",
    srcs = ["FormatterTool.java"],
    main_class = "com.google.startupos.tools.formatter.FormatterTool",
    deps = [
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/googlejavaformat:google_java_format",
        "//third_party/maven/com/google/guava",
    ],
)

checkstyle_test(
    name = "formatter_tool-checkstyle",
    target = ":formatter_tool",
)
