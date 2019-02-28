# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_binary(
    name = "test_tool",
    srcs = ["TestTool.java"],
    main_class = "com.google.startupos.tools.reviewer.local_server.service.tests.TestTool",
    deps = [
        "//common/repo:repo_java_proto",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/io/grpc:grpc_core",
        "//tools/reviewer/local_server/service:code_review_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
    ],
)

checkstyle_test(
    name = "test_tool-checkstyle",
    target = ":test_tool",
)

java_test(
    name = "code_review_service_get_diff_files_test",
    srcs = ["CodeReviewServiceGetDiffFilesTest.java"],
    test_class = "com.google.startupos.tools.reviewer.local_server.service.tests.CodeReviewServiceGetDiffFilesTest",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common:text_differencer",
        "//common/firestore",
        "//common/flags",
        "//common/repo",
        "//common/repo:repo_java_proto",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit",
        "//third_party/maven/org/mockito:mockito_core",
        "//tools/reviewer/aa:aa_module",
        "//tools/reviewer/aa/commands",
        "//tools/reviewer/local_server/service:auth_service",
        "//tools/reviewer/local_server/service:code_review_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)

checkstyle_test(
    name = "code_review_service_get_diff_files_test-checkstyle",
    target = ":code_review_service_get_diff_files_test",
)

java_test(
    name = "code_review_service_text_diff_test",
    srcs = ["CodeReviewServiceTextDiffTest.java"],
    test_class = "com.google.startupos.tools.reviewer.local_server.service.tests.CodeReviewServiceTextDiffTest",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common:text_differencer",
        "//common/flags",
        "//common/repo",
        "//common/repo:repo_java_proto",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit",
        "//tools/reviewer/aa:aa_module",
        "//tools/reviewer/aa/commands",
        "//tools/reviewer/local_server/service:auth_service",
        "//tools/reviewer/local_server/service:code_review_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)

checkstyle_test(
    name = "code_review_service_text_diff_test-checkstyle",
    target = ":code_review_service_text_diff_test",
)
