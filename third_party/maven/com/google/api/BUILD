java_library(
    name = "api_common",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/mvncom/google/api/api_common",
    ],
    runtime_deps = [
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/guava",
    ],
)

java_library(
    name = "gax",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvncom/google/api/gax",
    ],
)

java_library(
    name = "gax_grpc",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvncom/google/api/gax_grpc",
    ],
    runtime_deps = [
        "//third_party/maven/com/google/auto/value:auto_value",
        "//third_party/maven/org/threeten:threetenbp",
    ],
)

java_library(
    name = "gax_httpjson",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvncom/google/api/gax_httpjson",
    ],
)
