package(default_visibility = ["//visibility:public"])

java_library(
    name = "flags",
    srcs = [
        "ClassScanner.java",
        "Flag.java",
        "FlagDesc.java",
        "Flags.java",
        "GflagsParser.java",
        "UsagePrinter.java",
    ],
    deps = [
        ":flags_java_proto",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/annotation:javax_annotation_api",
        "//third_party/maven/org/javassist",
    ],
)

proto_library(
    name = "flags_proto",
    srcs = ["flags.proto"],
)

java_proto_library(
    name = "flags_java_proto",
    deps = [":flags_proto"],
)
