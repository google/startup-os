package(default_visibility = ["//visibility:private"])

java_library(
    name = "FirestoreTestBase",
    srcs = ["util/FirestoreJsonFormatTestBase.java"],
    deps = [
        "//common",
        "//common:dagger_with_annotation_processor",
        "//common/firestore",
        "//third_party/maven/com/google/code/gson",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit",
        "//third_party/maven/org/mockito:mockito_core",
    ],
)

java_test(
    name = "FirestoreJsonFormatPrinterTest",
    srcs = [
        "FirestoreJsonFormatPrinterTest.java",
    ],
    data = glob(["*.json"]),
    resources = glob(["resources/*.json"]),
    test_class = "com.google.startupos.common.firestore.test.FirestoreJsonFormatPrinterTest",
    deps = [
        ":FirestoreTestBase",
        ":test_java_proto",
        "//common/firestore",
        "//third_party/maven/com/google/code/gson",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/junit",
        "//third_party/maven/org/mockito:mockito_core",
    ],
)

java_test(
    name = "FirestoreJsonFormatParserTest",
    srcs = [
        "FirestoreJsonFormatParserTest.java",
    ],
    data = glob(["*.json"]),
    resources = glob(["resources/*.json"]),
    test_class = "com.google.startupos.common.firestore.test.FirestoreJsonFormatParserTest",
    deps = [
        ":FirestoreTestBase",
        ":test_java_proto",
        "//common/firestore",
        "//third_party/maven/com/google/code/gson",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/junit",
        "//third_party/maven/org/mockito:mockito_core",
    ],
)

proto_library(
    name = "test_proto",
    srcs = ["test.proto"],
)

java_proto_library(
    name = "test_java_proto",
    deps = [":test_proto"],
)
