java_library(
    name = "flogger",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/com/google/flogger/flogger",
    ],
    runtime_deps = [
        "//third_party/maven/com/google/code/findbugs:jsr305",
    ],
)

java_library(
    name = "flogger_system_backend",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/com/google/flogger/flogger_system_backend",
    ],
    runtime_deps = [
        ":flogger",
        "//third_party/maven/com/google/code/findbugs:jsr305",
    ],
)
