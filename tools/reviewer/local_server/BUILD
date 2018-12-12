package(default_visibility = ["//visibility:public"])

load("@io_bazel_rules_docker//java:image.bzl", "java_image")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")

java_image(
    name = "local_server",
    srcs = [
        "LocalServer.java",
    ],
    base = "@alpine_java_git//image",
    main_class = "com.google.startupos.tools.localserver.LocalServer",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/flags",
        "//common/repo",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_netty",
        "//third_party/maven/io/grpc:grpc_services",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer/aa:aa_module",
        "//tools/reviewer/aa:config_java_proto",
        "//tools/reviewer/local_server/service:auth_service",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)

container_push(
    name = "push_local_server",
    format = "Docker",
    image = ":local_server",
    registry = "gcr.io",
    repository = "startup-os/reviewer",
    tag = "latest",
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
        "//tools/reviewer/local_server/service:auth_service_java_grpc",
        "//tools/reviewer/local_server/service:auth_service_java_proto",
        "//tools/reviewer/local_server/service:code_review_java_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
    ],
)

java_image(
    name = "local_http_gateway",
    srcs = [
        "LocalHttpGateway.java",
    ],
    base = "@alpine_java_git//image",
    main_class = "com.google.startupos.tools.localserver.LocalHttpGateway",
    deps = [
        ":local_http_gateway_grpc_client",
        "//common/flags",
        "//common/repo:repo_java_proto",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/org/json",
        "//tools/reviewer/local_server/service:code_review_java_proto",
    ],
)

container_push(
    name = "push_local_http_gateway",
    format = "Docker",
    image = ":local_http_gateway",
    registry = "gcr.io",
    repository = "startup-os/reviewer",
    tag = "latest",
)
