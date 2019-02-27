package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
    name = "build_file_generator_proto",
    srcs = ["build_file_generator.proto"],
)

java_proto_library(
    name = "build_file_generator_java_proto",
    deps = [":build_file_generator_proto"],
)

java_library(
    name = "build_file_generator",
    srcs = ["BuildFileGenerator.java"],
    deps = [
        ":build_file_generator_java_proto",
        ":build_file_parser",
        ":java_class_analyzer",
        ":proto_file_analyzer",
        "//common",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "build_file_generator-checkstyle",
    target = ":build_file_generator",
)

java_library(
    name = "build_file_parser",
    srcs = ["BuildFileParser.java"],
    deps = [
        ":build_file_generator_java_proto",
        "//common",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "build_file_parser-checkstyle",
    target = ":build_file_parser",
)

java_library(
    name = "build_file_writer",
    srcs = ["BuildFileWriter.java"],
    deps = [
        ":build_file_generator_java_proto",
        "//common",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "build_file_writer-checkstyle",
    target = ":build_file_writer",
)

java_library(
    name = "java_class_analyzer",
    srcs = ["JavaClassAnalyzer.java"],
    deps = [
        ":build_file_generator_java_proto",
        "//common",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "java_class_analyzer-checkstyle",
    target = ":java_class_analyzer",
)

java_library(
    name = "proto_file_analyzer",
    srcs = ["ProtoFileAnalyzer.java"],
    deps = [
        ":build_file_generator_java_proto",
        "//common",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "proto_file_analyzer-checkstyle",
    target = ":proto_file_analyzer",
)

java_library(
    name = "third_party_deps_analyzer",
    srcs = ["ThirdPartyDepsAnalyzer.java"],
    deps = [
        ":build_file_generator_java_proto",
        "//common",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "third_party_deps_analyzer-checkstyle",
    target = ":third_party_deps_analyzer",
)

java_binary(
    name = "build_file_generator_tool",
    srcs = ["BuildFileGeneratorTool.java"],
    main_class = "com.google.startupos.tools.build_file_generator.BuildFileGeneratorTool",
    deps = [
        ":build_file_generator",
        ":build_file_writer",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "build_file_generator_tool-checkstyle",
    target = ":build_file_generator_tool",
)

java_binary(
    name = "third_party_deps_tool",
    srcs = ["ThirdPartyDepsTool.java"],
    main_class = "com.google.startupos.tools.build_file_generator.ThirdPartyDepsTool",
    deps = [
        ":third_party_deps_analyzer",
        "//common",
        "//common:dagger_with_annotation_processor",
        "//third_party/maven/com/google/api:api_common",
        "//third_party/maven/com/google/auth:google_auth_library_credentials",
        "//third_party/maven/com/google/auth:google_auth_library_oauth2_http",
        "//third_party/maven/com/google/auto:auto_common",
        "//third_party/maven/com/google/auto/factory:auto_factory",
        "//third_party/maven/com/google/auto/service:auto_service",
        "//third_party/maven/com/google/auto/service:auto_service_plugin",
        "//third_party/maven/com/google/auto/value:auto_value",
        "//third_party/maven/com/google/auto/value:auto_value_annotations",
        "//third_party/maven/com/google/auto/value:auto_value_plugin",
        "//third_party/maven/com/google/cloud:google_cloud_core",
        "//third_party/maven/com/google/cloud:google_cloud_core_grpc",
        "//third_party/maven/com/google/cloud:google_cloud_firestore",
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/code/gson",
        "//third_party/maven/com/google/dagger",
        "//third_party/maven/com/google/dagger:dagger_compiler",
        "//third_party/maven/com/google/errorprone:error_prone_ant",
        "//third_party/maven/com/google/firebase:firebase_admin",
        "//third_party/maven/com/google/flogger",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/googlejavaformat:google_java_format",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/com/google/jimfs",
        "//third_party/maven/com/google/protobuf:protobuf_java",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
        "//third_party/maven/com/jcraft:jsch",
        "//third_party/maven/com/puppycrawl/tools:checkstyle",
        "//third_party/maven/com/squareup/okhttp",
        "//third_party/maven/com/squareup/okio",
        "//third_party/maven/commons_logging",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/io/grpc:grpc_netty",
        "//third_party/maven/io/grpc:grpc_protobuf",
        "//third_party/maven/io/grpc:grpc_services",
        "//third_party/maven/io/grpc:grpc_stub",
        "//third_party/maven/io/netty:netty_codec_http2",
        "//third_party/maven/io/netty:netty_tcnative_boringssl_static",
        "//third_party/maven/io/opencensus:opencensus_contrib_grpc_metrics",
        "//third_party/maven/javax/annotation:javax_annotation_api",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/junit",
        "//third_party/maven/org/apache/commons:commons_csv",
        "//third_party/maven/org/apache/commons:commons_lang3",
        "//third_party/maven/org/apache/httpcomponents:fluent_hc",
        "//third_party/maven/org/apache/pdfbox",
        "//third_party/maven/org/apache/pdfbox:fontbox",
        "//third_party/maven/org/javassist",
        "//third_party/maven/org/json",
        "//third_party/maven/org/jsoup",
        "//third_party/maven/org/mockito:mockito_core",
        "//third_party/maven/org/objenesis",
        "//third_party/maven/org/slf4j:slf4j_simple",
        "//third_party/maven/org/yaml:snakeyaml",
    ],
)

checkstyle_test(
    name = "third_party_deps_tool-checkstyle",
    target = ":third_party_deps_tool",
)
