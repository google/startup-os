package(default_visibility = ["//visibility:public"])

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
        "//tools/aa/commands",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/org/slf4j:slf4j_simple",
    ],
)

java_binary(
    name = "aa_tool",
    srcs = ["AaTool.java"],
    main_class = "com.google.startupos.tools.aa.AaTool",
    deps = [
        ":aa_module",
        ":config_java_proto",
        "//tools/aa/commands",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/flags",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/org/slf4j:slf4j_simple",
    ],
)
