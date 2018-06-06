java_library(
    name = "fluent_hc",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/org/apache/httpcomponents/fluent_hc",
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
        "//visibility:public",
    ],
    exports = [
        "//external:jar/org/apache/httpcomponents/httpclient",
    ],
    runtime_deps = [
        ":httpcore",
        "//third_party/maven/commons_codec",
    ],
)

java_library(
    name = "httpcore",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/org/apache/httpcomponents/httpcore",
    ],
)
