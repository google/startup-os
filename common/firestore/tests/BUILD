package(default_visibility = ["//visibility:private"])

java_binary(
    name = "firestore_client_tool",
    srcs = [
        "FirestoreClientTool.java",
    ],
    main_class = "com.google.startupos.common.firestore.tests.FirestoreClientTool",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common:diff_match_patch",
        "//common:text_difference_java_proto",
        "//common:text_differencer",
        "//common/firestore",
        "//common/flags",
        "//common/repo",
        "//third_party/maven/com/google/api:api_common",
        "//third_party/maven/com/google/cloud:google_cloud_firestore",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/firebase:firebase_admin",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/org/slf4j:slf4j_simple",
        "//tools/reviewer:reviewer_java_proto",
        "//tools/reviewer/local_server/service:auth_service",
        "//tools/reviewer/local_server/service:code_review_java_proto",
    ],
)
