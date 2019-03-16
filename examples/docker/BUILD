# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_binary(
    name = "hello_server",
    srcs = ["HelloServer.java"],
    main_class = "com.google.startupos.examples.docker.HelloServer",
    deps = ["//third_party/maven/com/google/flogger:flogger_system_backend"],
)

checkstyle_test(
    name = "hello_server-checkstyle",
    target = ":hello_server",
)
