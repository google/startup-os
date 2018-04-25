package(default_visibility = ["//visibility:private"])

java_library(name = 'FirestoreTestBase',
    srcs = ["util/FirestoreJsonFormatTestBase.java"],
    deps = [
        "//common/firestore:firestore",
        "//third_party/maven/com/google/code/gson:gson",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/junit:junit",
        "//third_party/maven/org/mockito:mockito_core",
    ])

java_test(
    name = "FirestoreJsonFormatPrinterTest",
    srcs = [
        "FirestoreJsonFormatPrinterTest.java",
    ],
    test_class = "com.google.startupos.common.firestore.test.FirestoreJsonFormatPrinterTest",
    deps = [
        ":FirestoreTestBase",
        ":test_java_proto",
        "//common/firestore:firestore",
        "//third_party/maven/com/google/code/gson:gson",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/junit:junit",
        "//third_party/maven/org/mockito:mockito_core",
    ],
    resources = glob(['resources/*.json']),
    data = glob(['*.json']),
)

java_test(
    name = "FirestoreJsonFormatParserTest",
    srcs = [
        "FirestoreJsonFormatParserTest.java",
    ],
    test_class = "com.google.startupos.common.firestore.test.FirestoreJsonFormatParserTest",
    deps = [
        ":FirestoreTestBase",
        ":test_java_proto",
        "//common/firestore:firestore",
        "//third_party/maven/com/google/code/gson:gson",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/junit:junit",
        "//third_party/maven/org/mockito:mockito_core",
    ],
    resources = glob(['resources/*.json']),
    data = glob(['*.json']),
)

proto_library(
    name = "test_proto",
    srcs = ["test.proto"],
)

java_proto_library(
    name = "test_java_proto",
    deps = [":test_proto"],
)
