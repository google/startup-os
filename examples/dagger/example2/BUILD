package(default_visibility = ["//visibility:public"])

java_library(
    name = "example2_lib",
    srcs = glob(["**/*.java"]),
    deps = [
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/javax/inject:javax_inject"
    ],
)

java_binary(
    name = "example2",
    main_class = "com.google.startupos.examples.dagger.example2.App",
    runtime_deps = [":example2_lib"],
    jvm_flags = [
        "-Xms32m",
        "-Xmx128m",
        ],
)
