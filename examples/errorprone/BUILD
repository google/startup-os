# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_library(
    name = "protobuf_check",
    srcs = ["ProtobufCheck.java"],
    deps = [
        "//third_party/maven/com/google/auto/service:auto_service",
        "//third_party/maven/com/google/errorprone:error_prone_ant",
        "//third_party/maven/com/google/protobuf:protobuf_java",
    ],
)

checkstyle_test(
    name = "protobuf_check-checkstyle",
    target = ":protobuf_check",
)

java_library(
    name = "string_fmt_in_print_methods_check",
    srcs = ["StringFmtInPrintMethodsCheck.java"],
    deps = [
        "//third_party/maven/com/google/auto/service:auto_service",
        "//third_party/maven/com/google/errorprone:error_prone_ant",
        "//third_party/maven/com/google/guava",
    ],
)

checkstyle_test(
    name = "string_fmt_in_print_methods_check-checkstyle",
    target = ":string_fmt_in_print_methods_check",
)

java_binary(
    name = "hello",
    srcs = ["Hello.java"],
    main_class = "com.google.startupos.examples.errorprone.Hello",
    deps = ["//third_party/maven/com/google/flogger:flogger_system_backend"],
)

checkstyle_test(
    name = "hello-checkstyle",
    target = ":hello",
)
