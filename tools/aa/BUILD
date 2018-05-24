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
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/flags",
        "//common/repo",
        "//external:jar/com/google/protobuf/protobuf_java",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/org/reflections",
        "//tools/aa/commands",
    ],
)

java_binary(
    name = "aa_tool",
    srcs = ["AaTool.java"],
    main_class = "com.google.startupos.tools.aa.AaTool",
    deps = [
        ":aa_module",
        ":config_java_proto",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/flags",
        "//external:jar/com/google/protobuf/protobuf_java",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/org/reflections",
        "//tools/aa/commands",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/org/slf4j:slf4j_simple",
    ],
)
