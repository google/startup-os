package(default_visibility = ["//visibility:public"])

java_binary(
    name = "test_tool",
    srcs = [
        "TestTool.java",
    ],
    main_class = "com.google.startupos.tools.reviewer.service.tests.TestTool",
    deps = [
        "//common",
        "//common/repo:repo_java_proto",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_netty",
        "//third_party/maven/org/slf4j:slf4j_simple",
        "//tools/reviewer/service",
        "//tools/reviewer/service:code_review_java_grpc",
        "//tools/reviewer/service:code_review_java_proto",
    ],
)
