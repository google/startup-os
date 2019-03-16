# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_library(
    name = "firestore_proto_client",
    srcs = ["FirestoreProtoClient.java"],
    deps = [
        ":message_with_id",
        ":proto_event_listener",
        ":proto_query_snapshot",
        "//third_party/maven/com/google/api:api_common",
        "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
        "//third_party/maven/com/google/cloud:google_cloud_firestore",
        "//third_party/maven/com/google/cloud:google_cloud_storage",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/firebase:firebase_admin",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
    ],
)

checkstyle_test(
    name = "firestore_proto_client-checkstyle",
    target = ":firestore_proto_client",
)

java_library(
    name = "message_with_id",
    srcs = ["MessageWithId.java"],
    deps = [
        "//third_party/maven/com/google/auto/value:auto_value_annotations",
        "//third_party/maven/com/google/protobuf:protobuf_java",
    ],
)

checkstyle_test(
    name = "message_with_id-checkstyle",
    target = ":message_with_id",
)

java_library(
    name = "proto_change",
    srcs = ["ProtoChange.java"],
    deps = ["//third_party/maven/com/google/protobuf:protobuf_java"],
)

checkstyle_test(
    name = "proto_change-checkstyle",
    target = ":proto_change",
)

java_library(
    name = "proto_event_listener",
    srcs = ["ProtoEventListener.java"],
)

checkstyle_test(
    name = "proto_event_listener-checkstyle",
    target = ":proto_event_listener",
)

java_library(
    name = "proto_query_snapshot",
    srcs = ["ProtoQuerySnapshot.java"],
    deps = [
        ":firestore_proto_client",
        ":proto_change",
        "//third_party/maven/com/google/cloud:google_cloud_firestore",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
    ],
)

checkstyle_test(
    name = "proto_query_snapshot-checkstyle",
    target = ":proto_query_snapshot",
)
