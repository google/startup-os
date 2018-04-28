package(default_visibility = ["//visibility:public"])

proto_library(
    name = "config_proto",
    srcs = ["config.proto"],
)

java_proto_library(
    name = "config_java_proto",
    deps = [":config_proto"],
)

java_binary(
    name = "aa_tool",
    srcs = glob([
        "AaTool.java",
        "commands/*.java",
    ]),
    main_class = "com.google.startupos.tools.aa.AaTool",
    deps = [
        ":config_java_proto",
        "//external:jar/com/google/protobuf/protobuf_java",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/org/reflections",
        "//third_party/maven/org/slf4j:slf4j_simple",
        "//common",
        "//common/flags",
    ],
)
