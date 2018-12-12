package(default_visibility = ["//visibility:public"])

java_library(
    name = "common",
    srcs = [
        "CommandLine.java",
        "CommonComponent.java",
        "CommonModule.java",
        "FileUtils.java",
        "Lists.java",
        "MessageDifferencer.java",
        "StringBuilder.java",
        "Strings.java",
        "Time.java",
    ],
    deps = [
        ":text_differencer",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/auto/value:auto_value",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/org/apache/commons:commons_lang3",
    ],
)

java_library(
    name = "lists",
    srcs = ["Lists.java"],
    deps = [
        "//third_party/maven/com/google/auto/value:auto_value",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/guava",
    ],
)

java_library(
    name = "text_differencer",
    srcs = [
        "TextDifferencer.java",
    ],
    deps = [
        ":diff_match_patch",
        ":lists",
        ":text_difference_java_proto",
        "//common/repo:repo_java_proto",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

proto_library(
    name = "text_difference_proto",
    srcs = ["text_difference.proto"],
)

java_proto_library(
    name = "text_difference_java_proto",
    deps = [":text_difference_proto"],
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
    processor_class = "com.google.auto.factory.processor.AutoFactoryProcessor",
    deps = [
        "//third_party/maven/com/google/auto:auto_common",
        "//third_party/maven/com/google/auto/factory:auto_factory",
    ],
)

java_library(
    name = "diff_match_patch",
    srcs = ["DiffMatchPatch.java"],
)

java_binary(
    name = "base64_tool",
    srcs = [
        "Base64Tool.java",
    ],
    main_class = "com.google.startupos.common.Base64Tool",
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)
