package(default_visibility = ["//visibility:public"])

java_library(
  name = "flags",
  srcs = [
      "ClassScanner.java", 
      "Flags.java",
      "GflagsParser.java",
      "UsagePrinter.java",
      "Flag.java",
      "FlagDesc.java", 
  ],
  deps = [
      ":flags_java_proto",
      "//third_party/maven/org/reflections:reflections",
      "//third_party/maven/org/javassist:javassist",
      "//third_party/maven/org/slf4j:slf4j_api",
      "//third_party/maven/com/google/code/findbugs:jsr305",
      "//third_party/maven/javax/annotation:javax_annotation_api",
      "//third_party/maven/com/google/guava:guava",
  ]
)

proto_library(
    name = "flags_proto",
    srcs = ["flags.proto"],
)

java_proto_library(
    name = "flags_java_proto",
    deps = [":flags_proto"],
)
