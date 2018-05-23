package(default_visibility = ["//visibility:public"])

java_library(
    name = "memory_repo",
    srcs = [
        "MemoryRepo.java",
    ],
    deps = [
        "//common/repo",
        "//common/repo:repo_java_proto",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/annotation:javax_annotation_api",
        "//tools/reviewer/service:code_review_java_proto",
    ],
)

java_binary(
    name = "test_tool",
    srcs = [
        "TestTool.java",
    ],
    main_class = "com.google.startupos.common.repo.tests.TestTool",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/repo",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)
