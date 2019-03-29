java_library(
    name = "flogger",
    exports = [
        "//external:jar/mvncom/google/flogger/flogger"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/code/findbugs:jsr305"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "flogger_system_backend",
    exports = [
        "//external:jar/mvncom/google/flogger/flogger_system_backend"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/code/findbugs:jsr305",
        ":flogger"
    ],
    visibility = [
        "//visibility:public"
    ]
)


