package(default_visibility = ["//visibility:public"])

java_library(
    name = "example1_lib",
    srcs = glob(["**/*.java"]),
    deps = [
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

java_binary(
    name = "example1",
    jvm_flags = [
        "-Xms32m",
        "-Xmx128m",
    ],
    main_class = "com.google.startupos.examples.dagger.example1.App",
    runtime_deps = [":example1_lib"],
)
