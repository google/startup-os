# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
    name = "reviewer_proto",
    srcs = ["reviewer.proto"],
)

proto_library(
    name = "reviewer_registry_proto",
    srcs = ["reviewer_registry.proto"],
    deps = ["//tools/reviewer:reviewer_proto"],
)

java_proto_library(
    name = "reviewer_java_proto",
    deps = [":reviewer_proto"],
)

java_proto_library(
    name = "reviewer_registry_java_proto",
    deps = [":reviewer_registry_proto"],
)

java_library(
    name = "reviewer_constants",
    srcs = ["ReviewerConstants.java"],
)

checkstyle_test(
    name = "reviewer_constants-checkstyle",
    target = ":reviewer_constants",
)

java_test(
    name = "global_registry_test",
    srcs = ["GlobalRegistryTest.java"],
    test_class = "com.google.startupos.tools.reviewer.GlobalRegistryTest",
    deps = [
        ":reviewer_registry_java_proto",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit",
    ],
)

checkstyle_test(
    name = "global_registry_test-checkstyle",
    target = ":global_registry_test",
)

java_test(
    name = "reviewer_config_test",
    srcs = ["ReviewerConfigTest.java"],
    test_class = "com.google.startupos.tools.reviewer.ReviewerConfigTest",
    deps = [
        ":reviewer_java_proto",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit",
    ],
)

checkstyle_test(
    name = "reviewer_config_test-checkstyle",
    target = ":reviewer_config_test",
)
