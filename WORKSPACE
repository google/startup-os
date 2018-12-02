# To find the sha256 for an http_archive, run wget on the URL to download the
# file, and use sha256sum on the file to produce the sha256.

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_file")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_jar")

load("//third_party/maven:package-lock.bzl", "maven_dependencies")

maven_dependencies()

http_archive(
    name = "io_grpc_grpc_java",
    sha256 = "5ba69890c9fe7bf476093d8863f26b861184c623ba43b70ef938a190cfb95bdc",
    strip_prefix = "grpc-java-1.12.0",
    urls = ["https://github.com/grpc/grpc-java/archive/v1.12.0.tar.gz"],
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
http_archive(
    name = "gmaven_rules",
    sha256 = "da44017f6d7bc5148a73cfd9bf8dbb1ee5a1301a596edad9181c5dc7648076ae",
    strip_prefix = "gmaven_rules-20180513-1",
    url = "https://github.com/bazelbuild/gmaven_rules/archive/20180513-1.tar.gz",
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

http_file(
    name = "buildifier",
    executable = True,
    sha256 = "d7d41def74991a34dfd2ac8a73804ff11c514c024a901f64ab07f45a3cf0cfef",
    urls = ["https://github.com/bazelbuild/buildtools/releases/download/0.11.1/buildifier"],
)

http_file(
    name = "buildifier_osx",
    executable = True,
    sha256 = "3cbd708ff77f36413cfaef89cd5790a1137da5dfc3d9b3b3ca3fac669fbc298b",
    urls = ["https://github.com/bazelbuild/buildtools/releases/download/0.11.1/buildifier.osx"],
)

http_file(
    name = "buildozer",
    executable = True,
    sha256 = "3226cfd3ac706b48fe69fc4581c6c163ba5dfa71a752a44d3efca4ae489f1105",
    urls = ["https://github.com/bazelbuild/buildtools/releases/download/0.11.1/buildozer"],
)

http_file(
    name = "buildozer_osx",
    executable = True,
    sha256 = "48109a542da2ad4bf10e7df962514a58ac19a32033e2dae8e682938ed11f4775",
    urls = ["https://github.com/bazelbuild/buildtools/releases/download/0.11.1/buildozer.osx"],
)

http_file(
    name = "unused_deps",
    executable = True,
    sha256 = "686f8943610e1a5e3d196e2209dcb35f463c3b583a056dd8ae355acdc2a089d8",
    urls = ["https://github.com/bazelbuild/buildtools/releases/download/0.11.1/unused_deps"],
)

http_file(
    name = "unused_deps_osx",
    executable = True,
    sha256 = "dd8d58429a258b094b20a1435be3086ecee5d036b87c0e766309842766bc345b",
    urls = ["https://github.com/bazelbuild/buildtools/releases/download/0.11.1/unused_deps.osx"],
)

http_archive(
    name = "com_google_googletest",
    sha256 = "d5ea270c46a25bf3d16643b5333273f2c8e97e73ca8c47586691a92fab476d83",
    strip_prefix = "googletest-08d5b1f33af8c18785fb8ca02792b5fac81e248f",
    urls = ["https://github.com/google/googletest/archive/08d5b1f33af8c18785fb8ca02792b5fac81e248f.zip"],
)

http_archive(
    name = "com_github_google_benchmark",
    sha256 = "3a94ccd74b7d7db1c4e4a9a22d8e56101023c01b378b752eeeee5b2907fcbca1",
    strip_prefix = "benchmark-6d74c0625b8e88c1afce72b4f383c91b9a99dbe6",
    urls = ["https://github.com/google/benchmark/archive/6d74c0625b8e88c1afce72b4f383c91b9a99dbe6.zip"],
)

http_archive(
    name = "com_google_absl",
    sha256 = "1375c123bda19875941625fb34a7280b8663bb4ed99344736f85ac5ddb955721",
    strip_prefix = "abseil-cpp-59ae4d5a0e833bedd9d7cc059ac15a9dc130e3f7",
    urls = ["https://github.com/abseil/abseil-cpp/archive/59ae4d5a0e833bedd9d7cc059ac15a9dc130e3f7.zip"],
)

# TODO: Once we move to Angular Bazel,
# remove all below and changes in commit that added it
# (you can find it by running git blame)

http_archive(
    name = "io_bazel_rules_webtesting",
    url = "https://github.com/bazelbuild/rules_webtesting/archive/ca7b8062d9cf4ef2fde9193c7d37a0764c4262d7.zip",
    strip_prefix = "rules_webtesting-ca7b8062d9cf4ef2fde9193c7d37a0764c4262d7",
    sha256 = "28c73cf9d310fa6dba30e66bdb98071341c99c3feb8662f2d3883a632de97d72",
)

http_archive(
    name = "build_bazel_rules_nodejs",
    url = "https://github.com/bazelbuild/rules_nodejs/archive/0.8.0.zip",
    strip_prefix = "rules_nodejs-0.8.0",
    sha256 = "4e40dd49ae7668d245c3107645f2a138660fcfd975b9310b91eda13f0c973953",
)

load("@build_bazel_rules_nodejs//:defs.bzl", "node_repositories")

node_repositories(package_json = ["//:package.json"])

http_archive(
    name = "build_bazel_rules_typescript",
    url = "https://github.com/bazelbuild/rules_typescript/archive/v0.13.0.zip",
    strip_prefix = "rules_typescript-0.13.0",
    sha256 = "8f2767ff56ad68c80c62e9a1cdc2ba2c2ba0b19d350f713365e5333045df02e3",
)

load("@build_bazel_rules_typescript//:defs.bzl", "ts_setup_workspace")
ts_setup_workspace()

# Rules for examples/docker/

http_archive(
    name = "io_bazel_rules_docker",
    strip_prefix = "rules_docker-0.5.1",
    urls = ["https://github.com/bazelbuild/rules_docker/archive/v0.5.1.tar.gz"],
)

load("@io_bazel_rules_docker//java:image.bzl", _java_image_repos = "repositories")

_java_image_repos()

http_jar(
    name = "grpc_polyglot",
    sha256 = "c2a453921632c0c3559f9df92e1699b69c784181f36a316f9927b70f52e5a7d5",
    url = "https://github.com/grpc-ecosystem/polyglot/releases/download/v1.6.0/polyglot.jar"
)

http_file(
    name = "protoc_bin",
    executable = True,
    sha256 = "84e29b25de6896c6c4b22067fb79472dac13cf54240a7a210ef1cac623f5231d",
    urls = ["https://github.com/google/protobuf/releases/download/v3.6.0/protoc-3.6.0-linux-x86_64.zip"]
)

http_file(
    name = "protoc_bin_osx",
    executable = True,
    sha256 = "768a42032718accd12e056447b0d93d42ffcdc27d1b0f21fc1e30a900da94842",
    urls = ["https://github.com/google/protobuf/releases/download/v3.6.0/protoc-3.6.0-osx-x86_64.zip"]
)

# clang-format tool download, to be used by Formatter tool

http_file(
    name = "clang_format_bin",
    executable = True,
    sha256 = "320f62a8a20941b7d876c09de96913e0d18f0e2649688c2cd010a5f12b5d7616",
    urls = ["https://github.com/oferb/startupos-binaries/releases/download/0.1.0/clang_format_bin"],
)

http_file(
    name = "clang_format_bin_osx",
    executable = True,
    sha256 = "06986eeed23213c5b6a97440c6a3090eabc62ceaf7fcb72f2b95c4744128dccf",
    urls = ["https://github.com/oferb/startupos-binaries/releases/download/0.1.0/clang_format_bin_osx"]
)

"""
We use proto_compiler and proto_java_toolchain bindings to avoid
compilation of protoc. To turn off prebuilt binaries, replace
- //tools:protoc with @com_google_protobuf//:protoc
- //tools:java_toolchain with @com_google_protobuf//:java_toolchain
"""

bind(
    name = "proto_compiler",
    actual = "//tools:protoc"
)

bind(
    name = "proto_java_toolchain",
    actual = "//tools:java_toolchain"
)

bind(
    name = "grpc_java_plugin",
    actual = "//tools:grpc_java_plugin"
)

http_file(
    name = "grpc_java_plugin_linux",
    executable = True,
    sha256 = "d9117f0a987004bee3379654871b4cfeb81e49ebba346442dac84c82c5c20887",
    urls = ["https://github.com/oferb/startupos-binaries/releases/download/0.1.0/grpc_java_plugin_linux"],
)

http_file(
    name = "grpc_java_plugin_osx",
    executable = True,
    sha256 = "e69af502d906199675454ac8af7dfddff78e6213df9abc63434c522adea6c6fb",
    urls = ["https://github.com/oferb/startupos-binaries/releases/download/0.1.0/grpc_java_plugin_osx"],
)

http_file(
    name = "grpcwebproxy_linux",
    executable = True,
    sha256 = "c4a9167a0d6f0e16debbdca2b27248daae436216d43aa6be010febb6fe572474",
    urls = ["https://github.com/oferb/startupos-binaries/releases/download/0.1.0/grpcwebproxy_linux"],
)

http_file(
    name = "grpcwebproxy_osx",
    executable = True,
    sha256 = "805dedb12948aa36ba29caf532774da714e95910292dda9b993fb8fc7f2019d0",
    urls = ["https://github.com/oferb/startupos-binaries/releases/download/0.1.0/grpcwebproxy_osx"],
)

http_file(
    name = "bazel_deps",
    executable = True,
    sha256 = "98b05c2826f2248f70e7356dc6c78bc52395904bb932fbb409a5abf5416e4292",
    urls = ["https://github.com/oferb/startupos-binaries/releases/download/0.1.01/bazel_deps.jar"],
)
