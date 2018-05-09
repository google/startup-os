package(default_visibility = ["//visibility:public"])

java_library(
    name = "commands",
    srcs = glob(["*.java"]),
    deps = [
        "//tools/aa:config_java_proto",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/flags",
        "//external:jar/com/google/protobuf/protobuf_java",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/org/reflections",
        "//third_party/maven/org/slf4j:slf4j_simple",
        "//third_party/maven/org/eclipse/jgit:org_eclipse_jgit",
    ],
)
