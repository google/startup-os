package(default_visibility = ["//visibility:public"])

java_binary(
    name = "simple_formatter",
    srcs = ["SimpleFormatterTool.java"],
    main_class = "com.google.startupos.tools.simple_formatter.SimpleFormatterTool",
    deps = [
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/googlejavaformat:google_java_format",
        "//third_party/maven/com/google/guava",
    ],
)
