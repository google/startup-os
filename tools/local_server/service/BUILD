package(default_visibility = ["//visibility:public"])
load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_library")

java_proto_library(
    name = "auth_service_lib",
    protos = ["auth_service.proto"],
    verbose = 1,
    with_grpc = True,
)

java_library(
    name = "auth_service",
    srcs = [
        "AuthService.java",
    ],
    deps = [
        ":auth_service_lib",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "@org_pubref_rules_protobuf//java:netty_runtime_deps",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)
