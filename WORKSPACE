# bazel-deps-deploy is a prebuilt version of johnynek/bazel-deps
# we cannot use it via http_archive directly for the moment
# relevant issue: https://github.com/johnynek/bazel-deps/issues/126
git_repository(
    name = 'bazel_deps',
    remote = 'https://github.com/vmax/bazel-deps-deploy',
    commit = 'a15c2f64e099e78871ee78ff1f4e6bec5ec7ed4c'
)

load("//third_party/maven:workspace.bzl", "maven_dependencies")
maven_dependencies()

git_repository(
    name = 'gmaven_rules',
    remote = 'https://github.com/aj-michael/gmaven_rules',
    commit = '5e89b7cdc94d002c13576fad3b28b0ae30296e55',
)
load('@gmaven_rules//:gmaven.bzl', 'gmaven_rules')
gmaven_rules()

# Android SDK configuration. For more details, see:
# https://docs.bazel.build/versions/master/be/android.html#android_sdk_repository
android_sdk_repository(
    name = "androidsdk",
    api_level = 27,
    build_tools_version = "27.0.3"
)

maven_jar(
  name = "com_squareup_okhttp_okhttp_2_7_2",
  artifact = "com.squareup.okhttp:okhttp:jar:2.7.2",
)

maven_jar(
  name = "com_squareup_okio_okio_1_6_0",
  artifact = "com.squareup.okio:okio:jar:1.6.0"
)

http_archive(
    name = "com_google_protobuf",
    urls = ["https://github.com/google/protobuf/archive/3.5.1.1.zip"],
    strip_prefix = "protobuf-3.5.1.1",
)


git_repository(
  name = "org_pubref_rules_protobuf",
  remote = "https://github.com/pubref/rules_protobuf",
  tag = "v0.8.2",
)

http_archive(
    name = "io_grpc_grpc_java",
    urls = ["https://github.com/grpc/grpc-java/archive/46079fff8aa50b5a9222b79f9ea01472fcd5b44f.zip"],
    strip_prefix = "grpc-java-46079fff8aa50b5a9222b79f9ea01472fcd5b44f",
)

maven_jar(
  name = "javax_annotation_api",
  artifact = "javax.annotation:javax.annotation-api:1.2"
)

bind(
    name = "gson",
    actual = "//third_party/maven/com/google/code/gson",
)


bind(
    name = "guava",
    actual = "//third_party/maven/com/google/guava",
)

load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_repositories")
java_proto_repositories(excludes = [
    "com_google_protobuf",
    "com_google_api_grpc_proto_google_common_protos",
    "com_google_code_gson_gson",
    "com_google_errorprone_error_prone_annotations",
    "com_google_guava_guava",
    "com_google_protobuf_protobuf_java",
    "com_google_protobuf_protobuf_java_util",
    "io_grpc_grpc_context",
    "io_grpc_grpc_core",
    "io_grpc_grpc_netty",
    "io_grpc_grpc_protobuf",
    "io_grpc_grpc_protobuf_lite",
    "io_grpc_grpc_stub",
    "io_netty_netty_buffer",
    "io_netty_netty_codec",
    "io_netty_netty_codec_http",
    "io_netty_netty_codec_http2",
    "io_netty_netty_codec_socks",
    "io_netty_netty_common",
    "io_netty_netty_handler",
    "io_netty_netty_handler_proxy",
    "io_netty_netty_resolver",
    "io_netty_netty_transport",
    "io_opencensus_opencensus_api",
])
