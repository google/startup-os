load("//third_party:java_grpc_library.bzl", "java_grpc_library")
load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
    name = "api_proto",
    srcs = ["api.proto"],
)

java_proto_library(
    name = "java_api_proto",
    deps = [":api_proto"],
)

java_grpc_library(
    name = "grpc_proxy_service_proto",
    srcs = [":api_proto"],
    tags = ["checkstyle_ignore"],
    deps = [":java_api_proto"],
)

java_binary(
    name = "cas_proxy",
    srcs = [
        "AccessManager.java",
        "Proxy.java",
        "PublicAccess.java",
    ],
    main_class = "com.google.startupos.tools.reviewer.cas_proxy.CASProxy",
    deps = [
        ":grpc_proxy_service_proto",
        "//common/firestore",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_stub",
        "//tools/reviewer/cas_proxy:java_api_proto",
    ],
)

checkstyle_test(
    name = "cas_proxy-checkstyle",
    target = ":cas_proxy",
)

java_binary(
    name = "cas_proxy_client_tool",
    srcs = ["ProxyClientTool.java"],
    main_class = "com.google.startupos.tools.reviewer.cas_proxy.CASProxyClientTool",
    deps = [
        ":grpc_proxy_service_proto",
        "//common/firestore",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_stub",
        "//tools/reviewer/cas_proxy:java_api_proto",
    ],
)

checkstyle_test(
    name = "cas_proxy_client_tool-checkstyle",
    target = ":cas_proxy_client_tool",
)
