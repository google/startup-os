package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_library(
    name = "example1_lib",
    srcs = glob(["**/*.java"]),
    deps = [
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "example1_lib-checkstyle",
    allow_failure = 1,
    target = ":example1_lib",
)

java_binary(
    name = "example1",
    jvm_flags = [
        "-Xms32m",
        "-Xmx128m",
    ],
    main_class = "com.google.startupos.examples.dagger.example1.App",
    runtime_deps = [":example1_lib"],
    tags = ["checkstyle_ignore"],
)
