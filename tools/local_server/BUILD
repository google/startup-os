package(default_visibility = ["//visibility:public"])

java_library(
    name = "local_inprocess_server",
    srcs = [
        "LocalInProcessServer.java",
    ],
    deps = [
        "//tools/reviewer/service:code_review_lib",
        "//tools/reviewer/service:grpc_service",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "@org_pubref_rules_protobuf//java:netty_runtime_deps",
        "//third_party/maven/org/slf4j:slf4j_simple",
    ],
)

java_binary(
    name = "local_server",
    srcs = [
        "LocalServer.java",
    ],
    main_class = "com.google.startupos.tools.localserver.LocalServer",
    deps = [
        "//tools/reviewer/service:code_review_lib",
        "//tools/reviewer/service:grpc_service",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "@org_pubref_rules_protobuf//java:netty_runtime_deps",
        "//common/flags",
        "//third_party/maven/org/slf4j:slf4j_simple",
    ],
)

java_binary(
    name = "local_http_gateway",
    srcs = [
        "LocalHttpGateway.java",
    ],
    main_class = "com.google.startupos.tools.localserver.LocalHttpGateway",
    deps = [
        "//tools/reviewer/service:code_review_lib",
        "//tools/reviewer/service:grpc_client",
        ":local_inprocess_server",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "@org_pubref_rules_protobuf//java:netty_runtime_deps",
        "//third_party/maven/org/json:json",
        "//common/flags",
        "//third_party/maven/org/slf4j:slf4j_simple",
    ],
)
