java_library(
    name = "slf4j_api",
    exports = [
        "//external:jar/mvnorg/slf4j/slf4j_api"
    ],
    licenses = [
        "permissive"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "slf4j_simple",
    exports = [
        "//external:jar/mvnorg/slf4j/slf4j_simple"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        ":slf4j_api"
    ],
    visibility = [
        "//visibility:public"
    ]
)


