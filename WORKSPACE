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

http_archive(
    name = "io_grpc_grpc_java",
    urls = ["https://github.com/grpc/grpc-java/archive/v1.12.0.tar.gz"],
    strip_prefix = "grpc-java-1.12.0",
)

load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")

# TODO: Figure out if we also need omit_com_google_protobuf_javalite = True
grpc_java_repositories(
    omit_com_google_api_grpc_google_common_protos = True,
    omit_com_google_auth_google_auth_library_credentials = True,
    omit_com_google_code_findbugs_jsr305 = True,
    omit_com_google_code_gson = True,
    omit_com_google_errorprone_error_prone_annotations = True,
    omit_com_google_guava = True,
    omit_com_google_protobuf = True,
    omit_com_google_protobuf_nano_protobuf_javanano = True,
    omit_com_google_truth_truth = True,
    omit_com_squareup_okhttp = True,
    omit_com_squareup_okio = True,
    omit_io_netty_buffer = True,
    omit_io_netty_codec = True,
    omit_io_netty_codec_http = True,
    omit_io_netty_codec_http2 = True,
    omit_io_netty_codec_socks = True,
    omit_io_netty_common = True,
    omit_io_netty_handler = True,
    omit_io_netty_handler_proxy = True,
    omit_io_netty_resolver = True,
    omit_io_netty_tcnative_boringssl_static = True,
    omit_io_netty_transport = True,
    omit_io_opencensus_api = True,
    omit_io_opencensus_grpc_metrics = True,
    omit_junit_junit = True,
    omit_org_apache_commons_lang3 = True,
)

# Google Maven Repository
GMAVEN_TAG = "20180513-1"
http_archive(
    name = "gmaven_rules",
    strip_prefix = "gmaven_rules-%s" % GMAVEN_TAG,
    url = "https://github.com/bazelbuild/gmaven_rules/archive/%s.tar.gz" % GMAVEN_TAG,
)

load("@gmaven_rules//:gmaven.bzl", "gmaven_rules")
gmaven_rules()

# Android SDK configuration. For more details, see:
# https://docs.bazel.build/versions/master/be/android.html#android_sdk_repository
android_sdk_repository(
    name = "androidsdk",
    api_level = 27,
    build_tools_version = "27.0.3",
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

http_file(
    name = "buildifier",
    urls = ["https://github.com/bazelbuild/buildtools/releases/download/0.11.1/buildifier"],
    executable = True,
    sha256 = "d7d41def74991a34dfd2ac8a73804ff11c514c024a901f64ab07f45a3cf0cfef"
)

http_file(
    name = "buildifier_osx",
    urls = ["https://github.com/bazelbuild/buildtools/releases/download/0.11.1/buildifier.osx"],
    executable = True,
    sha256 = "3cbd708ff77f36413cfaef89cd5790a1137da5dfc3d9b3b3ca3fac669fbc298b"
)


http_file(
    name = "unused_deps",
    urls = ["https://github.com/bazelbuild/buildtools/releases/download/0.11.1/unused_deps"],
    executable = True,
    sha256 = "686f8943610e1a5e3d196e2209dcb35f463c3b583a056dd8ae355acdc2a089d8"
)

http_file(
    name = "unused_deps_osx",
    urls = ["https://github.com/bazelbuild/buildtools/releases/download/0.11.1/unused_deps.osx"],
    executable = True,
    sha256 = "dd8d58429a258b094b20a1435be3086ecee5d036b87c0e766309842766bc345b"
)

http_archive(
    name = "com_google_googletest",
    strip_prefix = "googletest-master",
    urls = ["https://github.com/google/googletest/archive/master.zip"],
)

http_archive(
    name = "com_github_google_benchmark",
    strip_prefix = "benchmark-master",
    urls = ["https://github.com/google/benchmark/archive/master.zip"],
)

http_archive(
    name = "com_google_absl",
    strip_prefix = "abseil-cpp-master",
    urls = ["https://github.com/abseil/abseil-cpp/archive/master.zip"],
)
