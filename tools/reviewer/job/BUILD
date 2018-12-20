load("@io_bazel_rules_docker//java:image.bzl", "java_image")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")

proto_library(
    name = "submitter_proto",
    srcs = ["submitter.proto"],
)

JOB_FILES = [
    "ReviewerJob.java",
    "impl/CiTask.java",
    "impl/FirestoreTaskBase.java",
    "impl/ReviewerMetadataUpdaterTask.java",
    "impl/SubmitterTask.java",
    "tasks/Task.java",
    "tasks/TaskExecutor.java",
]

JOB_DEPS = [
    "//common",
    "//common:dagger_with_annotation_processor",
    "//common/firestore",
    "//common/flags",
    "//common/repo",
    "//third_party/maven/com/google/auth:google_auth_library_credentials",
    "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
    "//third_party/maven/com/google/flogger",
    "//third_party/maven/com/google/flogger:flogger_system_backend",
    "//third_party/maven/javax/inject:javax_inject",
    "//third_party/maven/io/grpc:grpc_services",
    "//tools/reviewer:reviewer_java_proto",
    "//tools/reviewer:reviewer_registry_java_proto",
    "//tools/reviewer/aa:aa_module",
    "//tools/reviewer/aa/commands",
    "//tools/reviewer/local_server",
    "//tools/reviewer/local_server:local_http_gateway",
    "//tools/reviewer/local_server/service:code_review_service",
]

java_binary(
    name = "job",
    srcs = JOB_FILES,
    main_class = "com.google.startupos.tools.reviewer.job.ReviewerJob",
    deps = JOB_DEPS,
)

java_image(
    name = "job_image",
    srcs = JOB_FILES,
    base = "@alpine_java_git//image",
    main_class = "com.google.startupos.tools.reviewer.job.ReviewerJob",
    deps = JOB_DEPS,
)

container_push(
    name = "push_job",
    format = "Docker",
    image = ":job_image",
    registry = "gcr.io",
    repository = "startup-os/reviewer",
    tag = "latest",
)
