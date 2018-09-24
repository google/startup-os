java_library(
    name = "jackson_annotations",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvncom/fasterxml/jackson/core/jackson_annotations",
    ],
)

java_library(
    name = "jackson_core",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvncom/fasterxml/jackson/core/jackson_core",
    ],
)

java_library(
    name = "jackson_databind",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvncom/fasterxml/jackson/core/jackson_databind",
    ],
    runtime_deps = [
        ":jackson_annotations",
        ":jackson_core",
    ],
)
