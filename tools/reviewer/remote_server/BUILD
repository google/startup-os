load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
    name = "messages_proto",
    srcs = ["messages.proto"],
)

java_proto_library(
    name = "messages_java_proto",
    deps = [":messages_proto"],
)

java_binary(
    name = "remote_server",
    srcs = ["RemoteServer.java"],
    main_class = "com.google.startupos.tools.reviewer.remote_server.RemoteServer",
    deps = [
        ":messages_java_proto",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
        "//third_party/maven/org/json",
    ],
)

checkstyle_test(
    name = "remote_server-checkstyle",
    target = ":remote_server",
)
