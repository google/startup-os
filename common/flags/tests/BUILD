package(default_visibility = ["//visibility:private"])

java_test(
    name = "FlagsTest",
    srcs = [
        "FlagDescTestClass.java",
        "FlagsTest.java", 
    ],
    test_class = "com.google.startupos.common.flags.FlagsTest",
    deps = [
        "//common/flags",
        "//third_party/maven/junit:junit",
        "//third_party/maven/org/mockito:mockito_core",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/org/objenesis:objenesis",
    ],
)
