# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_library(
    name = "firestore_initializer",
    srcs = ["FirestoreInitializer.java"],
    deps = [
        "android/content:context",
        "//third_party/maven/com/google/firebase:firebase_admin",
        "//third_party/maven/org/json",
    ],
)

checkstyle_test(
    name = "firestore_initializer-checkstyle",
    target = ":firestore_initializer",
)

java_library(
    name = "main_application",
    srcs = ["MainApplication.java"],
    deps = [
        "android/app:application",
        "android/util:log",
        ":firestore_initializer",
        "//third_party/maven/org/json",
    ],
)

checkstyle_test(
    name = "main_application-checkstyle",
    target = ":main_application",
)
