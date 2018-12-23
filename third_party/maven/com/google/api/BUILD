java_library(
    name = "api_common",
    exports = [
        "//external:jar/mvncom/google/api/api_common"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/guava:guava"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "gax",
    exports = [
        "//external:jar/mvncom/google/api/gax"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/org/threeten:threetenbp",
        ":api_common"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "gax_grpc",
    exports = [
        "//external:jar/mvncom/google/api/gax_grpc"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/api/grpc:proto_google_common_protos",
        "//third_party/maven/com/google/auth:google_auth_library_credentials",
        "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/io/grpc:grpc_auth",
        "//third_party/maven/io/grpc:grpc_netty_shaded",
        "//third_party/maven/io/grpc:grpc_protobuf",
        "//third_party/maven/io/grpc:grpc_stub",
        "//third_party/maven/org/threeten:threetenbp",
        ":api_common",
        ":gax"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "gax_httpjson",
    exports = [
        "//external:jar/mvncom/google/api/gax_httpjson"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/auth:google_auth_library_credentials",
        "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/code/gson:gson",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/com/google/http_client:google_http_client",
        "//third_party/maven/org/threeten:threetenbp",
        ":api_common",
        ":gax"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)


