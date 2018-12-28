package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_binary(
    name = "formatter",
    srcs = ["FormatterTool.java"],
    main_class = "com.google.startupos.tools.formatter.FormatterTool",
    runtime_deps = [
        ":clang_format",
        "//tools:buildifier",
        "//tools:shfmt",
    ],
    deps = [
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/googlejavaformat:google_java_format",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/org/slf4j:slf4j_simple",
    ],
)

checkstyle_test(
    name = "formatter-checkstyle",
    allow_failure = 1,
    target = ":formatter",
)

sh_binary(
    name = "clang_format",
    srcs = ["clang-format.sh"],
    data = [
        "@clang_format_bin//file",
        "@clang_format_bin_osx//file",
    ],
)
