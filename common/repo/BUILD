package(default_visibility = ["//visibility:public"])

proto_library(
    name = "repo_proto",
    srcs = ["repo.proto"],
    deps = ["//tools/reviewer/service:code_review_proto"],
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
        "//common:auto_factory",
        "//common:auto_factory_plugin",
        "//common:dagger_with_annotation_processor",
        "//tools/reviewer/service:code_review_java_proto",
        "//third_party/maven/com/google/auto/value:auto_value",
        "//third_party/maven/com/google/auto/value:auto_value_plugin",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/com/jcraft:jsch",
        "//third_party/maven/javax/annotation:javax_annotation_api",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/org/eclipse/jgit:org_eclipse_jgit",
    ]
)
