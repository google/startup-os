package(default_visibility = ["//visibility:public"])

java_library(
  name = "common",
  srcs = [
    "FileUtils.java",
    "Logger.java",
    "StringBuilder.java",
    "Strings.java",
    "Time.java",
    "MessageDifferencer.java",
  ],
  deps = [
    "//third_party/maven/com/google/auto/value:auto_value",
    "//third_party/maven/com/google/code/findbugs:jsr305",
    "//third_party/maven/com/google/guava:guava",
    "//third_party/maven/com/google/protobuf:protobuf_java",
    "//third_party/maven/org/apache/commons:commons_lang3",
    "//third_party/maven/org/slf4j:slf4j_api",
  ]
)

java_library(
  name = "text_differencer",
  srcs = [
    "TextDifferencer.java",
    "CharDifference.java",
    "DifferenceType.java",
  ],
  deps = [
    "//third_party/maven/com/google/guava:guava",
  ],
)

java_library(
  name = "git",
  srcs = [
    "Git.java",
  ],
  deps = [
    "//third_party/maven/com/google/guava:guava",
    "//third_party/maven/com/google/protobuf:protobuf_java",
    "//third_party/maven/com/jcraft:jsch",
    "//third_party/maven/org/eclipse/jgit:org_eclipse_jgit",
  ]
)
