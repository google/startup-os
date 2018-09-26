package(default_visibility = ["//tools/reviewer:__subpackages__"])

exports_files(["global_registry.prototxt"])

proto_library(
    name = "reviewer_proto",
    srcs = ["reviewer.proto"],
)

java_proto_library(
    name = "reviewer_java_proto",
    deps = [":reviewer_proto"],
)

proto_library(
    name = "reviewer_registry_proto",
    srcs = ["reviewer_registry.proto"],
    deps = [":reviewer_proto"],
)

java_proto_library(
    name = "reviewer_registry_java_proto",
    deps = [":reviewer_registry_proto"],
)

java_test(
    name = "GlobalRegistryTest",
    srcs = [
        "GlobalRegistryTest.java",
    ],
    data = ["//tools/reviewer:global_registry.prototxt"],
    test_class = "com.google.startupos.tools.reviewer.GlobalRegistryTest",
    deps = [
        ":reviewer_java_proto",
        ":reviewer_registry_java_proto",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/annotation:javax_annotation_api",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

java_test(
    name = "ReviewerConfigTest",
    srcs = [
        "ReviewerConfigTest.java",
    ],
    data = ["//:reviewer_config.prototxt"],
    test_class = "com.google.startupos.tools.reviewer.ReviewerConfigTest",
    deps = [
        ":reviewer_java_proto",
        ":reviewer_registry_java_proto",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/annotation:javax_annotation_api",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)
