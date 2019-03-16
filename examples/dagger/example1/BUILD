# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_library(
    name = "class1_impl",
    srcs = ["Class1Impl.java"],
    deps = [":class1_interface"],
)

checkstyle_test(
    name = "class1_impl-checkstyle",
    target = ":class1_impl",
)

java_library(
    name = "class1_interface",
    srcs = ["Class1Interface.java"],
)

checkstyle_test(
    name = "class1_interface-checkstyle",
    target = ":class1_interface",
)

java_library(
    name = "class1_module",
    srcs = ["Class1Module.java"],
    deps = [
        ":class1_impl",
        ":class1_interface",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "class1_module-checkstyle",
    target = ":class1_module",
)

java_library(
    name = "class2",
    srcs = ["Class2.java"],
    deps = [
        ":class1_interface",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "class2-checkstyle",
    target = ":class2",
)

java_library(
    name = "some_component",
    srcs = ["SomeComponent.java"],
    deps = [
        ":class2",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "some_component-checkstyle",
    target = ":some_component",
)

java_binary(
    name = "app",
    srcs = ["App.java"],
    main_class = "com.google.startupos.examples.dagger.example1.App",
    deps = [
        ":dagger_some_component",
        ":some_component",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
    ],
)

checkstyle_test(
    name = "app-checkstyle",
    target = ":app",
)
