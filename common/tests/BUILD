# Tests for the common library.

package(default_visibility = ["//visibility:private"])

java_test(
    name = "FileUtilsTest",
    srcs = ["FileUtilsTest.java"],
    test_class = "com.google.startupos.common.FileUtilsTest",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit:junit",
        "//third_party/maven/org/mockito:mockito_core",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/com/google/jimfs",
        "//third_party/maven/org/objenesis:objenesis",
        "//third_party/maven/com/google/dagger:dagger",
    ],
)

java_test(
    name = "TextDifferencerTest",
    srcs = ["TextDifferencerTest.java"],
    test_class = "com.google.startupos.common.TextDifferencerTest",
    deps = [
        "//common:text_differencer_java_proto",
        "//common:text_differencer",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/junit",
    ],
)
