package(default_visibility = ["//visibility:public"])

proto_library(
    name = "data_proto",
    srcs = ["data.proto"],
)

java_proto_library(
    name = "data_java_proto",
    deps = [":data_proto"],
)

java_binary(
    name = "proto_vs_json_tool",
    srcs = ["ProtoVsJsonTool.java"],
    main_class = "com.google.startupos.proto_vs_json.ProtoVsJsonTool",
    deps = [
        ":data_java_proto",
        "//common",
        "//common/flags",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
        "//third_party/maven/org/slf4j:slf4j_api",
        "//third_party/maven/org/slf4j:slf4j_simple",    
    ],
)
