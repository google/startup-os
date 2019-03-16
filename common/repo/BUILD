# THIS FILE IS AUTO-GENERATED

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
    name = "git_repo",
    srcs = ["GitRepo.java"],
    deps = [
        ":repo",
        ":repo_java_proto",
        "//common",
        "//third_party/maven/com/google/auto/factory:auto_factory",
        "//third_party/maven/com/google/guava",
    ],
)

checkstyle_test(
    name = "git_repo-checkstyle",
    target = ":git_repo",
)

java_library(
    name = "repo",
    srcs = ["Repo.java"],
    deps = [
        ":repo_java_proto",
        "//third_party/maven/com/google/guava",
    ],
)

checkstyle_test(
    name = "repo-checkstyle",
    target = ":repo",
)
