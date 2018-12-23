package(default_visibility = ["//visibility:public"])

proto_library(
    name = "submitter_proto",
    srcs = ["submitter.proto"],
)

java_library(
    name = "task_interface",
    srcs = [
        "Task.java",
    ],
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/firestore",
        "//common/flags",
        "//common/repo",
        "//third_party/maven/com/google/auth:google_auth_library_credentials",
        "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/io/grpc:grpc_services",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer:reviewer_java_proto",
        "//tools/reviewer:reviewer_registry_java_proto",
        "//tools/reviewer/aa:aa_module",
        "//tools/reviewer/aa/commands",
        "//tools/reviewer/local_server",
        "//tools/reviewer/local_server:local_http_gateway",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)

java_library(
    name = "task_executor",
    srcs = [
        "TaskExecutor.java",
    ],
    deps = [
        ":reviewer_metadata_updater_task",
        ":task_interface",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/firestore",
        "//common/flags",
        "//common/repo",
        "//third_party/maven/com/google/auth:google_auth_library_credentials",
        "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/io/grpc:grpc_services",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer:reviewer_java_proto",
        "//tools/reviewer:reviewer_registry_java_proto",
        "//tools/reviewer/aa:aa_module",
        "//tools/reviewer/aa/commands",
        "//tools/reviewer/local_server",
        "//tools/reviewer/local_server:local_http_gateway",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)

java_library(
    name = "ci_task",
    srcs = [
        "CiTask.java",
    ],
    deps = [
        ":firestore_task_base",
        ":task_interface",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/firestore",
        "//common/flags",
        "//common/repo",
        "//third_party/maven/com/google/auth:google_auth_library_credentials",
        "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/io/grpc:grpc_services",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer:reviewer_java_proto",
        "//tools/reviewer:reviewer_registry_java_proto",
        "//tools/reviewer/aa:aa_module",
        "//tools/reviewer/aa/commands",
        "//tools/reviewer/local_server",
        "//tools/reviewer/local_server:local_http_gateway",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)

java_library(
    name = "firestore_task_base",
    srcs = [
        "FirestoreTaskBase.java",
    ],
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/firestore",
        "//common/flags",
        "//common/repo",
        "//third_party/maven/com/google/auth:google_auth_library_credentials",
        "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/io/grpc:grpc_services",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer:reviewer_java_proto",
        "//tools/reviewer:reviewer_registry_java_proto",
        "//tools/reviewer/aa:aa_module",
        "//tools/reviewer/aa/commands",
        "//tools/reviewer/local_server",
        "//tools/reviewer/local_server:local_http_gateway",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)

java_library(
    name = "reviewer_metadata_updater_task",
    srcs = [
        "ReviewerMetadataUpdaterTask.java",
    ],
    deps = [
        ":firestore_task_base",
        ":task_interface",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/firestore",
        "//common/flags",
        "//common/repo",
        "//third_party/maven/com/google/auth:google_auth_library_credentials",
        "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/io/grpc:grpc_services",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer:reviewer_java_proto",
        "//tools/reviewer:reviewer_registry_java_proto",
        "//tools/reviewer/aa:aa_module",
        "//tools/reviewer/aa/commands",
        "//tools/reviewer/local_server",
        "//tools/reviewer/local_server:local_http_gateway",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)

java_library(
    name = "submitter_task",
    srcs = [
        "SubmitterTask.java",
    ],
    deps = [
        ":firestore_task_base",
        ":task_interface",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/firestore",
        "//common/flags",
        "//common/repo",
        "//third_party/maven/com/google/auth:google_auth_library_credentials",
        "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/io/grpc:grpc_services",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/org/apache/commons:commons_lang3",
        "//tools/reviewer:reviewer_java_proto",
        "//tools/reviewer:reviewer_registry_java_proto",
        "//tools/reviewer/aa:aa_module",
        "//tools/reviewer/aa/commands",
        "//tools/reviewer/local_server",
        "//tools/reviewer/local_server:local_http_gateway",
        "//tools/reviewer/local_server/service:code_review_java_proto",
        "//tools/reviewer/local_server/service:code_review_service",
    ],
)