java_library(
    name = "google_auth_library_credentials",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/com/google/auth/google_auth_library_credentials",
    ],
)

java_library(
    name = "google_auth_library_oauth2_http",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/com/google/auth/google_auth_library_oauth2_http",
    ],
    runtime_deps = [
        ":google_auth_library_credentials",
    ],
)
