package(default_visibility = ["//visibility:public"])

java_library(
    name = "common",
    srcs = [
        "FileUtils.java",
        "Logger.java",
        "MessageDifferencer.java",
        "StringBuilder.java",
        "Strings.java",
        "Time.java",
    ],
    deps = [
        "//third_party/maven/com/google/auto/value:auto_value",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/org/apache/commons:commons_lang3",
        "//third_party/maven/org/slf4j:slf4j_api",
    ],
)

java_library(
    name = "git",
    srcs = [
        "Git.java",
    ],
    deps = [
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/com/jcraft:jsch",
        "//third_party/maven/org/eclipse/jgit:org_eclipse_jgit",
    ],
)

java_library(
    name = "text_differencer",
    srcs = [
        "TextDifferencer.java",
    ],
    deps = [
        ":char_difference_java_proto",
        "//third_party/maven/com/google/guava",
    ],
)

proto_library(
    name = "char_difference_proto",
    srcs = ["char_difference.proto"],
)

java_proto_library(
    name = "char_difference_java_proto",
    deps = [":char_difference_proto"],
)

java_plugin(
    name = "dagger_compiler_plugin",
    generates_api = True,
    processor_class = "dagger.internal.codegen.ComponentProcessor",
    deps = ["//third_party/maven/com/google/dagger:dagger_compiler"],
)

java_library(
    name = "dagger_with_annotation_processor",
    exported_plugins = [":dagger_compiler_plugin"],
    exports = ["//third_party/maven/com/google/dagger"],
)
