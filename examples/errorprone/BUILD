java_plugin(
    name = "StringFmtInPrintMethodsCheckPlugin",
    srcs = ["StringFmtInPrintMethodsCheck.java"],
    deps = [
        "//third_party/maven/com/google/auto/service:auto_service",
        "//third_party/maven/com/google/errorprone:error_prone_ant",
        "//third_party/maven/com/google/guava",
    ],
)

java_plugin(
    name = "ProtobufCheckPlugin",
    srcs = ["ProtobufCheck.java"],
    deps = [
        "//third_party/maven/com/google/auto/service:auto_service",
        "//third_party/maven/com/google/errorprone:error_prone_ant",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
    ],
)

java_binary(
    name = "Hello",
    srcs = ["Hello.java"],
    main_class = "com.google.startupos.examples.errorprone.Hello",
    plugins = [
        ":StringFmtInPrintMethodsCheckPlugin",
        ":ProtobufCheckPlugin",
    ],
    deps = ["//examples/proto_vs_json:data_java_proto"],
)
