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
    name = "class2_impl",
    srcs = ["Class2Impl.java"],
    deps = [
        ":class1_interface",
        ":class2_interface",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "class2_impl-checkstyle",
    target = ":class2_impl",
)

java_library(
    name = "class2_interface",
    srcs = ["Class2Interface.java"],
)

checkstyle_test(
    name = "class2_interface-checkstyle",
    target = ":class2_interface",
)

java_library(
    name = "class2_module",
    srcs = ["Class2Module.java"],
    deps = [
        ":class1_module",
        ":class2_impl",
        ":class2_interface",
        "//common:dagger_with_annotation_processor",
    ],
)

checkstyle_test(
    name = "class2_module-checkstyle",
    target = ":class2_module",
)

java_library(
    name = "class3",
    srcs = ["Class3.java"],
    deps = [
        ":class2_interface",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "class3-checkstyle",
    target = ":class3",
)

java_library(
    name = "some_component",
    srcs = ["SomeComponent.java"],
    deps = [
        ":class3",
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
    main_class = "com.google.startupos.examples.dagger.example2.App",
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
