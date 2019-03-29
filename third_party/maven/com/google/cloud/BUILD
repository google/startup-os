java_library(
    name = "google_cloud_core",
    exports = [
        "//external:jar/mvncom/google/cloud/google_cloud_core"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/api/grpc:proto_google_common_protos",
        "//third_party/maven/com/google/api/grpc:proto_google_iam_v1",
        "//third_party/maven/com/google/api:api_common",
        "//third_party/maven/com/google/api:gax",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/com/google/http_client:google_http_client",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
        "//third_party/maven/javax/annotation:javax_annotation_api"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "google_cloud_core_grpc",
    exports = [
        "//external:jar/mvncom/google/cloud/google_cloud_core_grpc"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/api:gax_grpc",
        "//third_party/maven/com/google/auth:google_auth_library_credentials",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
        "//third_party/maven/io/grpc:grpc_auth",
        "//third_party/maven/io/grpc:grpc_context",
        "//third_party/maven/io/grpc:grpc_netty_shaded",
        "//third_party/maven/io/grpc:grpc_protobuf",
        "//third_party/maven/io/grpc:grpc_stub",
        "//third_party/maven/javax/annotation:javax_annotation_api",
        ":google_cloud_core"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "google_cloud_core_http",
    exports = [
        "//external:jar/mvncom/google/cloud/google_cloud_core_http"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/api:gax_httpjson",
        "//third_party/maven/com/google/api_client:google_api_client",
        "//third_party/maven/com/google/auth:google_auth_library_credentials",
        "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/com/google/http_client:google_http_client",
        "//third_party/maven/com/google/http_client:google_http_client_appengine",
        "//third_party/maven/io/opencensus:opencensus_api",
        "//third_party/maven/io/opencensus:opencensus_contrib_http_util",
        "//third_party/maven/javax/annotation:javax_annotation_api",
        ":google_cloud_core"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "google_cloud_firestore",
    exports = [
        "//external:jar/mvncom/google/cloud/google_cloud_firestore"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/api/grpc:proto_google_cloud_firestore_v1",
        "//third_party/maven/com/google/api/grpc:proto_google_cloud_firestore_v1beta1",
        "//third_party/maven/com/google/api:api_common",
        "//third_party/maven/com/google/api:gax_grpc",
        "//third_party/maven/io/grpc:grpc_auth",
        "//third_party/maven/io/grpc:grpc_netty_shaded",
        "//third_party/maven/io/grpc:grpc_stub",
        "//third_party/maven/io/opencensus:opencensus_api",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_util",
        "//third_party/maven/javax/annotation:javax_annotation_api",
        ":google_cloud_core",
        ":google_cloud_core_grpc"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "google_cloud_storage",
    exports = [
        "//external:jar/mvncom/google/cloud/google_cloud_storage"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/apis:google_api_services_storage",
        "//third_party/maven/com/google/http_client:google_http_client_apache",
        "//third_party/maven/javax/annotation:javax_annotation_api",
        ":google_cloud_core",
        ":google_cloud_core_http"
    ],
    visibility = [
        "//visibility:public"
    ]
)


