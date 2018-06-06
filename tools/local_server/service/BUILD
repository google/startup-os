package(default_visibility = ["//visibility:public"])

load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")

proto_library(
    name = "auth_service_proto",
    srcs = ["auth_service.proto"],
)

java_proto_library(
    name = "auth_service_java_proto",
    deps = [":auth_service_proto"],
)

java_grpc_library(
    name = "auth_service_java_grpc",
    srcs = [":auth_service_proto"],
    deps = [
        ":auth_service_java_proto",
    ],
)

java_library(
    name = "auth_service",
    srcs = [
        "AuthService.java",
    ],
    deps = [
        ":auth_service_java_grpc",
        ":auth_service_java_proto",
        "//common",
        "//common/flags",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_stub",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/org/json",
    ],
)
