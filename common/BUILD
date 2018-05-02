package(default_visibility = ["//visibility:public"])

load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_library")

java_library(
    name = "common",
    srcs = [
        "FileUtils.java",
        "Logger.java",
        "MessageDifferencer.java",
        "StringBuilder.java",
        "Strings.java",
        "Time.java",
        "CommonModule.java",
        "CommonComponent.java",
    ],
    deps = [
        "//third_party/maven/com/google/auto/value:auto_value",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/org/apache/commons:commons_lang3",
        "//third_party/maven/org/slf4j:slf4j_api",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/javax/inject:javax_inject"
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
        "AbstractTextDifferencer.java",
        "LCSTextDifferencer.java",
        "TextDifferencer.java",
    ],
    deps = [
        ":text_difference_java_proto",
        "//third_party/maven/com/google/guava",
    ],
)

filegroup(
    name = "text_difference_proto",
    srcs = ["text_difference.proto"],
)

java_proto_library(
    name = "text_difference_java_proto",
    protos = [":text_difference_proto"],
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

java_library(
    name = "auto_factory",
    exported_plugins = [":auto_factory_plugin"],
    exports = ["//third_party/maven/com/google/auto/factory:auto_factory"],
)

java_plugin(
    name = "auto_factory_plugin",
    generates_api = True,
    deps = [
        "//third_party/maven/com/google/auto/factory:auto_factory",
        "//third_party/maven/com/google/auto:auto_common"
    ],
    processor_class = "com.google.auto.factory.processor.AutoFactoryProcessor",
)
