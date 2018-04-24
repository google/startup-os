package(default_visibility = ["//visibility:public"])

java_binary(
    name = "local_server",
    srcs = [
        "LocalServer.java",
    ],
    main_class = "com.google.startupos.tools.localserver.LocalServer",
    deps = [
        "//tools/reviewer/service:code_review_service_lib",
        "//tools/reviewer/service:grpc_service",
        "//tools/local_server/service:auth_service",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "@org_pubref_rules_protobuf//java:netty_runtime_deps",
        "//common/flags",
        "//third_party/maven/org/slf4j:slf4j_simple",
    ],
)


java_library(
    name = "local_http_gateway_grpc_client",
    srcs = [
        "LocalHttpGatewayGrpcClient.java",
    ],
    deps = [
        "//tools/reviewer/service:code_review_service_lib",
        "//tools/local_server/service:auth_service_lib",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "@org_pubref_rules_protobuf//java:grpc_compiletime_deps",
    ],
)

java_binary(
    name = "local_http_gateway",
    srcs = [
        "LocalHttpGateway.java",
    ],
    main_class = "com.google.startupos.tools.localserver.LocalHttpGateway",
    deps = [
        "//tools/reviewer/service:code_review_service_lib",
        ":local_http_gateway_grpc_client",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "@org_pubref_rules_protobuf//java:netty_runtime_deps",
        "//third_party/maven/org/json:json",
        "//common/flags",
        "//third_party/maven/org/slf4j:slf4j_simple",
    ],
)
