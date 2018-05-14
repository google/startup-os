package(default_visibility = ["//visibility:private"])

java_test(
    name = "InitCommandTest",
    srcs = [
        "InitCommandTest.java",
    ],
    test_class = "com.google.startupos.tools.aa.commands.tests.InitCommandTest",
    deps = [
        "//common:dagger_with_annotation_processor",
        "//common",
        "//tools/aa/commands",
        "//tools/aa:aa_module",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/com/google/jimfs",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit:junit",
        "//third_party/maven/org/mockito:mockito_core",
        "//third_party/maven/org/objenesis:objenesis",

    ],
)
