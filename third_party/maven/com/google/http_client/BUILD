java_library(
    name = "google_http_client",
    exports = [
        "//external:jar/mvncom/google/http_client/google_http_client"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/com/google/j2objc:j2objc_annotations",
        "//third_party/maven/org/apache/httpcomponents:httpclient"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "google_http_client_appengine",
    exports = [
        "//external:jar/mvncom/google/http_client/google_http_client_appengine"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        ":google_http_client"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "google_http_client_gson",
    exports = [
        "//external:jar/mvncom/google/http_client/google_http_client_gson"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/code/gson:gson",
        ":google_http_client"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "google_http_client_jackson",
    exports = [
        "//external:jar/mvncom/google/http_client/google_http_client_jackson"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/org/codehaus/jackson:jackson_core_asl",
        ":google_http_client"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "google_http_client_jackson2",
    exports = [
        "//external:jar/mvncom/google/http_client/google_http_client_jackson2"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/fasterxml/jackson/core:jackson_core",
        ":google_http_client"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)


