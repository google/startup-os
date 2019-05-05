load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_binary(
    name = "proto_tool",
    srcs = [
        "ProtoLoader.java",
        "ProtoTool.java",
    ],
    data = [
        "//tools:protoc",
    ],
    main_class = "com.google.startupos.tools.proto_tool.ProtoTool",
    deps = [
        "//common/flags",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
    ],
)

checkstyle_test(
    name = "proto_tool-checkstyle",
    target = ":proto_tool",
)
