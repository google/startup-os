# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_library(
    name = "third_party_deps_analyzer",
    srcs = ["ThirdPartyDepsAnalyzer.java"],
    deps = [
        "//common",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/build_file_generator:build_file_generator_java_proto",
    ],
)

checkstyle_test(
    name = "third_party_deps_analyzer-checkstyle",
    target = ":third_party_deps_analyzer",
)

java_binary(
    name = "third_party_deps_tool",
    srcs = ["ThirdPartyDepsTool.java"],
    main_class = "com.google.startupos.tools.build_file_generator.third_party_deps.ThirdPartyDepsTool",
    deps = [
        ":third_party_deps_analyzer",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "third_party_deps_tool-checkstyle",
    target = ":third_party_deps_tool",
)
