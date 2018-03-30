java_library(
    name = "google_cloud_core",
    exports = [
        "//external:jar/com/google/cloud/google_cloud_core"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/api/grpc:proto_google_common_protos",
        "//third_party/maven/com/google/api/grpc:proto_google_iam_v1",
        "//third_party/maven/com/google/api:gax",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
        "//third_party/maven/joda_time:joda_time"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "google_cloud_core_grpc",
    exports = [
        "//external:jar/com/google/cloud/google_cloud_core_grpc"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/io/grpc:grpc_context",
        "//third_party/maven/io/grpc:grpc_protobuf"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "google_cloud_core_http",
    exports = [
        "//external:jar/com/google/cloud/google_cloud_core_http"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/api:gax_httpjson",
        "//third_party/maven/com/google/http_client:google_http_client_appengine",
        "//third_party/maven/com/google/http_client:google_http_client_jackson"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "google_cloud_firestore",
    exports = [
        "//external:jar/com/google/cloud/google_cloud_firestore"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/api/grpc:proto_google_cloud_firestore_v1beta1",
        "//third_party/maven/com/google/api:gax_grpc",
        "//third_party/maven/io/grpc:grpc_auth",
        "//third_party/maven/io/grpc:grpc_netty",
        "//third_party/maven/io/grpc:grpc_stub",
        "//third_party/maven/io/netty:netty_tcnative_boringssl_static",
        ":google_cloud_core_grpc"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "google_cloud_storage",
    exports = [
        "//external:jar/com/google/cloud/google_cloud_storage"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/apis:google_api_services_storage",
        ":google_cloud_core",
        ":google_cloud_core_http"
    ],
    visibility = [
        "//visibility:public"
    ]
)


