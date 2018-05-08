package(default_visibility = ["//visibility:public"])

java_binary(
    name = "local_server",
    srcs = [
        "LocalServer.java",
    ],
    main_class = "com.google.startupos.tools.localserver.LocalServer",
    deps = [
        "//common",
        "//common/flags",
        "@io_grpc_grpc_java//core",
        "@io_grpc_grpc_java//stub",
        "//third_party/maven/io/grpc:grpc_auth",
        "//third_party/maven/io/grpc:grpc_context",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_netty",
        "//third_party/maven/io/grpc:grpc_protobuf",
        "//third_party/maven/io/grpc:grpc_stub",
        "//third_party/maven/io/netty:netty_common",
        "//third_party/maven/io/netty:netty_buffer",
        "//third_party/maven/io/netty:netty_codec",
        "//third_party/maven/io/netty:netty_codec_http",
        "//third_party/maven/io/netty:netty_codec_http2",
        "//third_party/maven/io/netty:netty_codec_socks",
        "//third_party/maven/io/netty:netty_handler",
        "//third_party/maven/io/netty:netty_handler_proxy",
        "//third_party/maven/io/netty:netty_resolver",
        "//third_party/maven/io/netty:netty_tcnative_boringssl_static",
        "//third_party/maven/io/netty:netty_transport",
        "//common:dagger_with_annotation_processor",
        "//tools/local_server/service:auth_service_java_grpc",
        "//tools/local_server/service:auth_service_java_proto",
        "//tools/local_server/service:auth_service",
        "//tools/reviewer/service",
        "//tools/reviewer/service:code_review_java_grpc",
        "//tools/reviewer/service:code_review_java_proto",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "//third_party/maven/io/opencensus:opencensus_api",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/org/slf4j:slf4j_simple",
    ],
)


java_library(
    name = "local_http_gateway_grpc_client",
    srcs = [
        "LocalHttpGatewayGrpcClient.java",
    ],
    deps = [
        "//tools/reviewer/service:code_review_java_grpc",
        "//tools/reviewer/service:code_review_java_proto",
        "//tools/local_server/service:auth_service_java_grpc",
        "//tools/local_server/service:auth_service_java_proto",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
    ],
)

java_binary(
    name = "local_http_gateway",
    srcs = [
        "LocalHttpGateway.java",
    ],
    main_class = "com.google.startupos.tools.localserver.LocalHttpGateway",
    deps = [
        "//tools/reviewer/service:code_review_java_grpc",
        "//tools/reviewer/service:code_review_java_proto",
        ":local_http_gateway_grpc_client",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "//third_party/maven/org/json:json",
        "//common/flags",
        "//third_party/maven/org/slf4j:slf4j_simple",
    ],
)
