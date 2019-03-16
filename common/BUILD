# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
    name = "text_difference_proto",
    srcs = ["text_difference.proto"],
)

java_proto_library(
    name = "text_difference_java_proto",
    deps = [":text_difference_proto"],
)

java_library(
    name = "command_line",
    srcs = ["CommandLine.java"],
)

checkstyle_test(
    name = "command_line-checkstyle",
    target = ":command_line",
)

java_library(
    name = "common_component",
    srcs = ["CommonComponent.java"],
    deps = [
        ":common_module",
        ":file_utils",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "common_component-checkstyle",
    target = ":common_component",
)

java_library(
    name = "common_module",
    srcs = ["CommonModule.java"],
    deps = [
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "common_module-checkstyle",
    target = ":common_module",
)

java_library(
    name = "diff_match_patch",
    srcs = ["DiffMatchPatch.java"],
    deps = [":arrays"],
)

checkstyle_test(
    name = "diff_match_patch-checkstyle",
    target = ":diff_match_patch",
)

java_library(
    name = "file_utils",
    srcs = ["FileUtils.java"],
    deps = [
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "file_utils-checkstyle",
    target = ":file_utils",
)

java_library(
    name = "http_utils",
    srcs = ["HttpUtils.java"],
    deps = ["//third_party/maven/javax/inject:javax_inject"],
)

checkstyle_test(
    name = "http_utils-checkstyle",
    target = ":http_utils",
)

java_library(
    name = "lists",
    srcs = ["Lists.java"],
    deps = [
        "//third_party/maven/com/google/auto/value:auto_value_annotations",
        "//third_party/maven/com/google/guava",
    ],
)

checkstyle_test(
    name = "lists-checkstyle",
    target = ":lists",
)

java_library(
    name = "message_differencer",
    srcs = ["MessageDifferencer.java"],
    deps = [
        "//third_party/maven/com/google/auto/value:auto_value_annotations",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
    ],
)

checkstyle_test(
    name = "message_differencer-checkstyle",
    target = ":message_differencer",
)

java_library(
    name = "string_builder",
    srcs = ["StringBuilder.java"],
)

checkstyle_test(
    name = "string_builder-checkstyle",
    target = ":string_builder",
)

java_library(
    name = "strings",
    srcs = ["Strings.java"],
    deps = ["//third_party/maven/org/apache/commons:commons_lang3"],
)

checkstyle_test(
    name = "strings-checkstyle",
    target = ":strings",
)

java_library(
    name = "text_differencer",
    srcs = ["TextDifferencer.java"],
    deps = [
        ":lists",
        ":text_difference_java_proto",
        "//common:lists",
        "//name/fraser/neil/plaintext:diff_match_patch",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "text_differencer-checkstyle",
    target = ":text_differencer",
)

java_library(
    name = "time",
    srcs = ["Time.java"],
)

checkstyle_test(
    name = "time-checkstyle",
    target = ":time",
)

java_binary(
    name = "base64_tool",
    srcs = ["Base64Tool.java"],
    main_class = "com.google.startupos.common.Base64Tool",
    deps = [
        ":common_module",
        ":file_utils",
        "//common:dagger_with_annotation_processor",
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "base64_tool-checkstyle",
    target = ":base64_tool",
)
