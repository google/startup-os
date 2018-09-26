java_library(
    name = "google_auth_library_credentials",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/mvncom/google/auth/google_auth_library_credentials",
    ],
)

java_library(
    name = "google_auth_library_oauth2_http",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/mvncom/google/auth/google_auth_library_oauth2_http",
    ],
    runtime_deps = [
        ":google_auth_library_credentials",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/http_client:google_http_client",
        "//third_party/maven/com/google/http_client:google_http_client_jackson2",
    ],
)
