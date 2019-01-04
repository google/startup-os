package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_library(
    name = "example2_lib",
    srcs = glob(["**/*.java"]),
    deps = [
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "example2_lib-checkstyle",
    target = ":example2_lib",
)

java_binary(
    name = "example2",
    jvm_flags = [
        "-Xms32m",
        "-Xmx128m",
    ],
    main_class = "com.google.startupos.examples.dagger.example2.App",
    tags = ["checkstyle_ignore"],
    runtime_deps = [":example2_lib"],
)
