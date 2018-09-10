java_library(
    name = "fluent_hc",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/mvnorg/apache/httpcomponents/fluent_hc",
    ],
    runtime_deps = [
        ":httpclient",
        "//third_party/maven/commons_logging",
    ],
)

java_library(
    name = "httpclient",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvnorg/apache/httpcomponents/httpclient",
    ],
    runtime_deps = [
        ":httpcore",
        "//third_party/maven/commons_codec",
        "//third_party/maven/commons_logging",
    ],
)

java_library(
    name = "httpcore",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvnorg/apache/httpcomponents/httpcore",
    ],
)
