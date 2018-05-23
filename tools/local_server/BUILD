package(default_visibility = ["//visibility:public"])

java_binary(
    name = "local_server",
    srcs = [
        "LocalServer.java",
    ],
    main_class = "com.google.startupos.tools.localserver.LocalServer",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/flags",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_netty",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/aa:aa_module",
        "//tools/local_server/service:auth_service",
        "//tools/reviewer/service",
    ],
)

java_library(
    name = "local_http_gateway_grpc_client",
    srcs = [
        "LocalHttpGatewayGrpcClient.java",
    ],
    deps = [
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_netty",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "//tools/local_server/service:auth_service_java_grpc",
        "//tools/local_server/service:auth_service_java_proto",
        "//tools/reviewer/service:code_review_java_grpc",
        "//tools/reviewer/service:code_review_java_proto",
    ],
)

java_binary(
    name = "local_http_gateway",
    srcs = [
        "LocalHttpGateway.java",
    ],
    main_class = "com.google.startupos.tools.localserver.LocalHttpGateway",
    deps = [
        ":local_http_gateway_grpc_client",
        "//common/flags",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/org/json",
        "//tools/reviewer/service:code_review_java_proto",
    ],
)
