# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_binary(
    name = "firestore_client_tool",
    srcs = ["FirestoreClientTool.java"],
    main_class = "com.google.startupos.common.firestore.tests.FirestoreClientTool",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/firestore",
        "//common/flags",
        "//third_party/maven/com/google/cloud:google_cloud_firestore",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer:reviewer_java_proto",
        "//tools/reviewer/local_server/service:auth_service",
        "//tools/reviewer/local_server/service:code_review_java_proto",
    ],
)

checkstyle_test(
    name = "firestore_client_tool-checkstyle",
    target = ":firestore_client_tool",
)
