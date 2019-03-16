# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
    name = "flags_proto",
    srcs = ["flags.proto"],
)

java_proto_library(
    name = "flags_java_proto",
    deps = [":flags_proto"],
)

java_library(
    name = "class_scanner",
    srcs = ["ClassScanner.java"],
    deps = [
        ":flag",
        ":flag_desc",
        ":flags_java_proto",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/org/javassist",
    ],
)

checkstyle_test(
    name = "class_scanner-checkstyle",
    target = ":class_scanner",
)

java_library(
    name = "flag",
    srcs = ["Flag.java"],
    deps = [
        ":flags",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/flogger",
    ],
)

checkstyle_test(
    name = "flag-checkstyle",
    target = ":flag",
)

java_library(
    name = "flag_desc",
    srcs = ["FlagDesc.java"],
)

checkstyle_test(
    name = "flag_desc-checkstyle",
    target = ":flag_desc",
)

java_library(
    name = "flags",
    srcs = ["Flags.java"],
    deps = [
        ":class_scanner",
        ":flags_java_proto",
        ":gflags_parser",
        ":usage_printer",
        "//third_party/maven/com/google/guava",
    ],
)

checkstyle_test(
    name = "flags-checkstyle",
    target = ":flags",
)

java_library(
    name = "gflags_parser",
    srcs = ["GflagsParser.java"],
    deps = [
        ":flags",
        ":flags_java_proto",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/guava",
    ],
)

checkstyle_test(
    name = "gflags_parser-checkstyle",
    target = ":gflags_parser",
)

java_library(
    name = "usage_printer",
    srcs = ["UsagePrinter.java"],
    deps = [":flags_java_proto"],
)

checkstyle_test(
    name = "usage_printer-checkstyle",
    target = ":usage_printer",
)
