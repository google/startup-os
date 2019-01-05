load("@io_bazel_rules_docker//java:image.bzl", "java_image")
load("@io_bazel_rules_docker//container:container.bzl", "container_push")
load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

JOB_DEPS = [
    "//common",
    "//common:dagger_with_annotation_processor",
    "//common/firestore",
    "//common/flags",
    "//common/repo",
    "//tools/reviewer/job/tasks:ci_task",
    "//tools/reviewer/job/tasks:submitter_task",
    "//tools/reviewer/job/tasks:reviewer_metadata_updater_task",
    "//tools/reviewer/job/tasks:task_executor",
    "//third_party/maven/com/google/auth:google_auth_library_credentials",
    "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
    "//third_party/maven/com/google/flogger",
    "//third_party/maven/com/google/flogger:flogger_system_backend",
    "//third_party/maven/javax/inject:javax_inject",
    "//third_party/maven/io/grpc:grpc_services",
    "//third_party/maven/org/apache/commons:commons_lang3",
    "//tools/reviewer:reviewer_java_proto",
    "//tools/reviewer:reviewer_registry_java_proto",
    "//tools/reviewer/aa:aa_module",
    "//tools/reviewer/aa/commands",
    "//tools/reviewer/local_server",
    "//third_party/maven/com/google/guava",
    "//tools/reviewer/local_server:local_http_gateway",
    "//tools/reviewer/local_server/service:code_review_service",
    "//tools/reviewer/local_server/service:code_review_java_proto",
]

java_binary(
    name = "job",
    srcs = ["ReviewerJob.java"],
    main_class = "com.google.startupos.tools.reviewer.job.ReviewerJob",
    deps = JOB_DEPS,
)

checkstyle_test(
    name = "job-checkstyle",
    target = ":job",
)

java_image(
    name = "job_image",
    srcs = ["ReviewerJob.java"],
    base = "@alpine_java_git//image",
    main_class = "com.google.startupos.tools.reviewer.job.ReviewerJob",
    deps = JOB_DEPS,
)

checkstyle_test(
    name = "job_image.binary-checkstyle",
    target = ":job_image.binary",
)

container_push(
    name = "push_job",
    format = "Docker",
    image = ":job_image",
    registry = "gcr.io",
    repository = "startup-os/reviewer",
    tag = "latest",
)
