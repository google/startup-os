# Tests for the common library.

package(default_visibility = ["//visibility:private"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
    name = "test_proto",
    srcs = ["file_utils_test.proto"],
)

java_proto_library(
    name = "test_java_proto",
    deps = [":test_proto"],
)

java_test(
    name = "FileUtilsTest",
    srcs = ["FileUtilsTest.java"],
    test_class = "com.google.startupos.common.tests.FileUtilsTest",
    deps = [
        ":test_java_proto",
        ":test_proto",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/dagger",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/jimfs",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit",
        "//third_party/maven/org/mockito:mockito_core",
        "//third_party/maven/org/objenesis",
    ],
)

checkstyle_test(
    name = "FileUtilsTest-checkstyle",
    target = ":FileUtilsTest",
)

java_test(
    name = "ListsTest",
    srcs = ["ListsTest.java"],
    test_class = "com.google.startupos.common.tests.ListsTest",
    deps = [
        "//common:lists",
        "//third_party/maven/com/google/dagger",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/junit",
        "//third_party/maven/org/mockito:mockito_core",
        "//third_party/maven/org/objenesis",
    ],
)

checkstyle_test(
    name = "ListsTest-checkstyle",
    target = ":ListsTest",
)

java_test(
    name = "TextDifferencerTest",
    srcs = ["TextDifferencerTest.java"],
    resources = glob(["resources/*.txt"]),
    test_class = "com.google.startupos.common.tests.TextDifferencerTest",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common:text_difference_java_proto",
        "//common:text_differencer",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit",
    ],
)

checkstyle_test(
    name = "TextDifferencerTest-checkstyle",
    target = ":TextDifferencerTest",
)

java_binary(
    name = "text_differencer_tool",
    srcs = [
        "TextDifferencerTool.java",
    ],
    main_class = "com.google.startupos.common.tests.TextDifferencerTool",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common:diff_match_patch",
        "//common:text_difference_java_proto",
        "//common:text_differencer",
        "//common/flags",
        "//common/repo",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/org/slf4j:slf4j_simple",
    ],
)

checkstyle_test(
    name = "text_differencer_tool-checkstyle",
    target = ":text_differencer_tool",
)
