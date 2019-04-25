load("//third_party:java_grpc_library.bzl", "java_grpc_library")

proto_library(
    name = "messages_proto",
    srcs = ["messages.proto"],
)

java_proto_library(
    name = "messages_java_proto",
    deps = [":messages_proto"],
)

java_grpc_library(
    name = "messages_java_grpc",
    srcs = [":messages_proto"],
    tags = ["checkstyle_ignore"],
    deps = [":messages_java_proto"],
)

java_library(
    name = "service",
    srcs = ["GrpcAuthService.java"],
    deps = [
        "messages_java_proto",
        ":messages_java_grpc",
        "//third_party/maven/io/grpc:grpc_stub",
    ],
)

DEPS = [
    ":messages_java_grpc",
    ":messages_java_proto",
    ":service",
    "//common/flags",
    "//third_party/maven/com/google/flogger",
    "//third_party/maven/com/google/flogger:flogger_system_backend",
    "//third_party/maven/com/google/auth:google_auth_library_credentials",
    "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
    "//third_party/maven/com/google/firebase:firebase_admin",
    "//third_party/maven/io/grpc:grpc_auth",
    "//third_party/maven/io/grpc:grpc_core",
    "//third_party/maven/io/grpc:grpc_netty",
    "//third_party/maven/io/grpc:grpc_stub",
    "//third_party/maven/io/netty:netty_handler",
    "//third_party/maven/io/netty:netty_tcnative_boringssl_static",
]

java_binary(
    name = "server",
    srcs = [
        "Server.java",
        "ServerAuthInterceptor.java",
    ],
    main_class = "com.google.startupos.common.grpc_auth.Server",
    deps = DEPS,
)

java_binary(
    name = "client",
    srcs = [
        "Client.java",
        "ClientAuthInterceptor.java",
    ],
    main_class = "com.google.startupos.common.grpc_auth.Client",
    deps = DEPS,
)
