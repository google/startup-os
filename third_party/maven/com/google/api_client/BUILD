java_library(
    name = "google_api_client",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvncom/google/api_client/google_api_client",
    ],
    runtime_deps = [
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/http_client:google_http_client_jackson2",
        "//third_party/maven/com/google/oauth_client:google_oauth_client",
    ],
)

java_library(
    name = "google_api_client_gson",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvncom/google/api_client/google_api_client_gson",
    ],
    runtime_deps = [
        ":google_api_client",
        "//third_party/maven/com/google/http_client:google_http_client_gson",
    ],
)
