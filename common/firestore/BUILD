package(default_visibility = ["//visibility:public"])

proto_library(
    name = "data_proto",
    srcs = ["data.proto"],
)

java_proto_library(
    name = "data_java_proto",
    deps = [":data_proto"],
)

java_library(
    name = "firestore",
    srcs = [
        "FirestoreClient.java",
        "FirestoreJsonFormat.java",
        "MessageWithId.java",
    ],
    deps = [
        ":data_java_proto",
        "//common:auto_factory",
        "//common:auto_factory_plugin",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/auto/value:auto_value",
        "//third_party/maven/com/google/code/gson",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

java_library(
    name = "firestore_admin_client",
    srcs = [
        "FirestoreAdminClient.java",
    ],
    deps = [
        ":data_java_proto",
        "//common:auto_factory",
        "//common:auto_factory_plugin",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/auth:google_auth_library_credentials",
        "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
        "//third_party/maven/com/google/code/gson",
        "//third_party/maven/com/google/firebase:firebase_admin",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)
