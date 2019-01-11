package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
    name = "repo_proto",
    srcs = ["repo.proto"],
)

java_proto_library(
    name = "repo_java_proto",
    deps = [":repo_proto"],
)

java_library(
    name = "repo",
    srcs = [
        "GitRepo.java",
        "Repo.java",
    ],
    deps = [
        ":repo_java_proto",
        "//common",
        "//common:auto_factory",
        "//common:auto_factory_plugin",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/auto/value:auto_value",
        "//third_party/maven/com/google/auto/value:auto_value_annotations",
        "//third_party/maven/com/google/auto/value:auto_value_plugin",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/jcraft:jsch",
        "//third_party/maven/javax/annotation:javax_annotation_api",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer/local_server/service:code_review_java_proto",
    ],
)

checkstyle_test(
    name = "repo-checkstyle",
    target = ":repo",
)
