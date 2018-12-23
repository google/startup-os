java_library(
    name = "fluent_hc",
    exports = [
        "//external:jar/mvnorg/apache/httpcomponents/fluent_hc"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/commons_logging:commons_logging",
        ":httpclient"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "httpclient",
    exports = [
        "//external:jar/mvnorg/apache/httpcomponents/httpclient"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/commons_codec:commons_codec",
        "//third_party/maven/commons_logging:commons_logging",
        ":httpcore"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "httpcore",
    exports = [
        "//external:jar/mvnorg/apache/httpcomponents/httpcore"
    ],
    licenses = [
        "permissive"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)


