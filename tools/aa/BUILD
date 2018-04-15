package(default_visibility = ["//visibility:public"])

proto_library(
    name = "aa_proto",
    srcs = ["data.proto"],
)

java_proto_library(
    name = "aa_java_proto",
    deps = [":aa_proto"],
)

java_binary(
    name = "aa",
    srcs = glob([
        "*.java",
        "commands/*.java",
    ]),
    main_class = "com.appstory.tools.aa.Aa",
    deps = [
        ":aa_java_proto",
        "//external:jar/com/google/protobuf/protobuf_java",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/org/reflections",
        "//third_party/maven/org/slf4j:slf4j_simple",
        "//common",
        "//common/flags",
    ],
)
