java_library(
    name = "slf4j_api",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvnorg/slf4j/slf4j_api",
    ],
)

java_library(
    name = "slf4j_simple",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/mvnorg/slf4j/slf4j_simple",
    ],
    runtime_deps = [
        ":slf4j_api",
    ],
)
