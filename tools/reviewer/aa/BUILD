package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
    name = "config_proto",
    srcs = ["config.proto"],
)

java_proto_library(
    name = "config_java_proto",
    deps = [":config_proto"],
)

java_library(
    name = "aa_module",
    srcs = ["AaModule.java"],
    deps = [
        ":config_java_proto",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/flags",
        "//common/repo",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer/aa/commands",
    ],
)

checkstyle_test(
    name = "aa_module-checkstyle",
    target = ":aa_module",
)

java_binary(
    name = "aa_tool",
    srcs = ["AaTool.java"],
    main_class = "com.google.startupos.tools.reviewer.aa.AaTool",
    deps = [
        ":aa_module",
        ":config_java_proto",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/org/slf4j:slf4j_simple",
        "//tools/reviewer/aa/commands",
    ],
)

checkstyle_test(
    name = "aa_tool-checkstyle",
    allow_failure = 1,
    target = ":aa_tool",
)

java_binary(
    name = "aa_script_helper",
    srcs = ["AaScriptHelper.java"],
    main_class = "com.google.startupos.tools.reviewer.aa.AaScriptHelper",
    deps = [
        ":aa_module",
        ":aa_tool",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/org/slf4j:slf4j_simple",
    ],
)

checkstyle_test(
    name = "aa_script_helper-checkstyle",
    target = ":aa_script_helper",
)
