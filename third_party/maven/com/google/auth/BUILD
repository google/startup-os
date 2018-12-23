java_library(
    name = "google_auth_library_credentials",
    exports = [
        "//external:jar/mvncom/google/auth/google_auth_library_credentials"
    ],
    licenses = [
        "permissive"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "google_auth_library_oauth2_http",
    exports = [
        "//external:jar/mvncom/google/auth/google_auth_library_oauth2_http"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/com/google/http_client:google_http_client",
        "//third_party/maven/com/google/http_client:google_http_client_jackson2",
        ":google_auth_library_credentials"
    ],
    visibility = [
        "//visibility:public"
    ]
)


