package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_library(
    name = "firestore",
    srcs = [
        "FirestoreProtoClient.java",
        "MessageWithId.java",
        "ProtoChange.java",
        "ProtoEventListener.java",
        "ProtoQuerySnapshot.java",
    ],
    deps = [
        "//common:auto_factory",
        "//common:auto_factory_plugin",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/api:api_common",
        "//third_party/maven/com/google/auth:google_auth_library_credentials",
        "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
        "//third_party/maven/com/google/auto/value:auto_value",
        "//third_party/maven/com/google/auto/value:auto_value_annotations",
        "//third_party/maven/com/google/cloud:google_cloud_core",
        "//third_party/maven/com/google/cloud:google_cloud_core_grpc",
        "//third_party/maven/com/google/cloud:google_cloud_firestore",
        "//third_party/maven/com/google/cloud:google_cloud_storage",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/code/gson",
        "//third_party/maven/com/google/firebase:firebase_admin",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "firestore-checkstyle",
    target = ":firestore",
)
