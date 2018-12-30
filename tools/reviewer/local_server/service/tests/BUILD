package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_binary(
    name = "test_tool",
    srcs = [
        "TestTool.java",
    ],
    main_class = "com.google.startupos.tools.reviewer.localserver.service.tests.TestTool",
    deps = [
        "//common",
        "//common/repo:repo_java_proto",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_netty",
        "//third_party/maven/org/slf4j:slf4j_simple",
        "//tools/reviewer/local_server/service:code_review_java_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)

checkstyle_test(
    name = "test_tool-checkstyle",
    allow_failure = 1,
    target = ":test_tool",
)

java_test(
    name = "CodeReviewServiceTextDiffTest",
    srcs = [
        "CodeReviewServiceTextDiffTest.java",
    ],
    test_class = "com.google.startupos.tools.reviewer.localserver.service.tests.CodeReviewServiceTextDiffTest",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common:text_differencer",
        "//common/firestore",
        "//common/flags",
        "//common/repo",
        "//common/repo:repo_java_proto",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_netty",
        "//third_party/maven/io/grpc:grpc_services",
        "//third_party/maven/junit",
        "//third_party/maven/org/mockito:mockito_core",
        "//third_party/maven/org/objenesis",
        "//tools/reviewer/aa:aa_module",
        "//tools/reviewer/aa/commands",
        "//tools/reviewer/local_server/service:auth_service",
        "//tools/reviewer/local_server/service:code_review_java_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)

checkstyle_test(
    name = "CodeReviewServiceTextDiffTest-checkstyle",
    target = ":CodeReviewServiceTextDiffTest",
)

java_test(
    name = "CodeReviewServiceGetDiffFilesTest",
    srcs = [
        "CodeReviewServiceGetDiffFilesTest.java",
    ],
    test_class = "com.google.startupos.tools.reviewer.localserver.service.tests.CodeReviewServiceGetDiffFilesTest",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common:text_differencer",
        "//common/firestore",
        "//common/flags",
        "//common/repo",
        "//common/repo:repo_java_proto",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_netty",
        "//third_party/maven/io/grpc:grpc_services",
        "//third_party/maven/junit",
        "//third_party/maven/org/mockito:mockito_core",
        "//third_party/maven/org/objenesis",
        "//tools/reviewer/aa:aa_module",
        "//tools/reviewer/aa/commands",
        "//tools/reviewer/local_server/service:auth_service",
        "//tools/reviewer/local_server/service:code_review_java_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)

checkstyle_test(
    name = "CodeReviewServiceGetDiffFilesTest-checkstyle",
    target = ":CodeReviewServiceGetDiffFilesTest",
)
