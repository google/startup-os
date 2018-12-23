java_library(
    name = "google_api_client",
    exports = [
        "//external:jar/mvncom/google/api_client/google_api_client"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/com/google/http_client:google_http_client_jackson2",
        "//third_party/maven/com/google/oauth_client:google_oauth_client"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "google_api_client_gson",
    exports = [
        "//external:jar/mvncom/google/api_client/google_api_client_gson"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/http_client:google_http_client_gson",
        ":google_api_client"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)


