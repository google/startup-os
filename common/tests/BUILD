# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
    name = "file_utils_test_proto",
    srcs = ["file_utils_test.proto"],
)

java_proto_library(
    name = "file_utils_test_java_proto",
    deps = [":file_utils_test_proto"],
)

java_binary(
    name = "text_differencer_tool",
    srcs = ["TextDifferencerTool.java"],
    main_class = "com.google.startupos.common.tests.TextDifferencerTool",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common:text_difference_java_proto",
        "//common:text_differencer",
        "//common/flags",
        "//common/repo",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "text_differencer_tool-checkstyle",
    target = ":text_differencer_tool",
)

java_test(
    name = "file_utils_test",
    srcs = ["FileUtilsTest.java"],
    resources = [
        "resources/BUILD_after.txt",
        "resources/BUILD_before.txt",
        "resources/BUILD_diff.txt",
        "resources/BUILD_diff_prototxt.txt",
        "resources/License_after.txt",
        "resources/License_before.txt",
        "resources/License_diff.txt",
        "resources/License_diff_prototxt.txt",
        "resources/MixedChangesAtTheBeginning_diff_prototxt.txt",
        "resources/MixedChangesAtTheEnd_diff_prototxt.txt",
        "resources/MixedChangesAtTheMiddle_diff_prototxt.txt",
    ],
    test_class = "com.google.startupos.common.tests.FileUtilsTest",
    deps = [
        ":file_utils_test_java_proto",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/jimfs",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit",
    ],
)

checkstyle_test(
    name = "file_utils_test-checkstyle",
    target = ":file_utils_test",
)

java_test(
    name = "lists_test",
    srcs = ["ListsTest.java"],
    resources = [
        "resources/BUILD_after.txt",
        "resources/BUILD_before.txt",
        "resources/BUILD_diff.txt",
        "resources/BUILD_diff_prototxt.txt",
        "resources/License_after.txt",
        "resources/License_before.txt",
        "resources/License_diff.txt",
        "resources/License_diff_prototxt.txt",
        "resources/MixedChangesAtTheBeginning_diff_prototxt.txt",
        "resources/MixedChangesAtTheEnd_diff_prototxt.txt",
        "resources/MixedChangesAtTheMiddle_diff_prototxt.txt",
    ],
    test_class = "com.google.startupos.common.tests.ListsTest",
    deps = [
        "//common:lists",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/junit",
    ],
)

checkstyle_test(
    name = "lists_test-checkstyle",
    target = ":lists_test",
)

java_test(
    name = "text_differencer_test",
    srcs = ["TextDifferencerTest.java"],
    resources = [
        "resources/BUILD_after.txt",
        "resources/BUILD_before.txt",
        "resources/BUILD_diff.txt",
        "resources/BUILD_diff_prototxt.txt",
        "resources/License_after.txt",
        "resources/License_before.txt",
        "resources/License_diff.txt",
        "resources/License_diff_prototxt.txt",
        "resources/MixedChangesAtTheBeginning_diff_prototxt.txt",
        "resources/MixedChangesAtTheEnd_diff_prototxt.txt",
        "resources/MixedChangesAtTheMiddle_diff_prototxt.txt",
    ],
    test_class = "com.google.startupos.common.tests.TextDifferencerTest",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common:text_difference_java_proto",
        "//common:text_differencer",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit",
    ],
)

checkstyle_test(
    name = "text_differencer_test-checkstyle",
    target = ":text_differencer_test",
)
