# To find the sha256 for an http_archive, run wget on the URL to download the
# file, and use sha256sum on the file to produce the sha256.

# bazel-deps-deploy is a prebuilt version of johnynek/bazel-deps
# we cannot use it via http_archive directly for the moment
# relevant issue: https://github.com/johnynek/bazel-deps/issues/126
git_repository(
    name = "bazel_deps",
    commit = "a15c2f64e099e78871ee78ff1f4e6bec5ec7ed4c",
    remote = "https://github.com/vmax/bazel-deps-deploy",
)

# deployed version of //tools:simple_formatter
git_repository(
    name = "simple_formatter",
    commit = "2034166aab69ed754f9bcbb27589d74e2b0a7300",
    remote = "https://github.com/vmax/simple-formatter-deploy",
)

load("//third_party/maven:workspace.bzl", "maven_dependencies")

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
GMAVEN_TAG = "20180513-1"

http_archive(
    name = "gmaven_rules",
    sha256 = "da44017f6d7bc5148a73cfd9bf8dbb1ee5a1301a596edad9181c5dc7648076ae",
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
    artifact = "com.squareup.okio:okio:jar:1.6.0",
)

http_archive(
    name = "com_google_protobuf",
    sha256 = "091d4263d9a55eccb6d3c8abde55c26eaaa933dea9ecabb185cdf3795f9b5ca2",
    strip_prefix = "protobuf-3.5.1.1",
    urls = ["https://github.com/google/protobuf/archive/3.5.1.1.zip"],
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
    strip_prefix = "rules_webtesting-master",
    urls = [
        "https://github.com/bazelbuild/rules_webtesting/archive/master.tar.gz",
    ],
)

git_repository(
    name = "build_bazel_rules_nodejs",
    remote = "https://github.com/bazelbuild/rules_nodejs.git",
    tag = "0.9.1",
)

load("@build_bazel_rules_nodejs//:defs.bzl", "node_repositories")

node_repositories(package_json = ["//:package.json"])

local_repository(
    name = "build_bazel_rules_typescript",
    path = "node_modules/@bazel/typescript",
)

load("@build_bazel_rules_typescript//:defs.bzl", "ts_setup_workspace")
ts_setup_workspace()

http_archive(
    name = "com_github_gflags_gflags",
    sha256 = "7d17922978692175c67ef5786a014df44bfbfe3b48b30937cca1413d4ff65f75",
    strip_prefix = "gflags-e292e0452fcfd5a8ae055b59052fc041cbab4abf",
    urls = ["https://github.com/gflags/gflags/archive/e292e0452fcfd5a8ae055b59052fc041cbab4abf.zip"],
)

http_archive(
    name = "com_github_google_glog",
    sha256 = "ae86d645a282137007420c280494a51cc1eb8729cd095348de0953a444705f45",
    strip_prefix = "glog-2faa186e62d544e930305ffd8f8e507b2054cc9b",
    urls = ["https://github.com/google/glog/archive/2faa186e62d544e930305ffd8f8e507b2054cc9b.zip"],
)

new_http_archive(
    name = "startupos_external_jsoncpp",
    strip_prefix = "jsoncpp-cfab607c0d6d4f4cab7bdde69769964c558913cb",
    urls = ["https://github.com/open-source-parsers/jsoncpp/archive/cfab607c0d6d4f4cab7bdde69769964c558913cb.zip"],
    sha256 = "0e0abc6b521a6df8eec5b32593781aa2f2f6f24ea71f8b9d3b504e966c849176",
    build_file="third_party/BUILD.jsoncpp",
)

http_archive(
    name = "io_bazel",
    sha256 = "b0269e75b40d87ff87886e5f3432cbf88f70c96f907ab588e6c21b2922d72db0",
    url = "https://github.com/bazelbuild/bazel/releases/download/0.13.1/bazel-0.13.1-dist.zip",
)

# Rules for examples/docker/

http_archive(
    name = "io_bazel_rules_docker",
    strip_prefix = "rules_docker-0.4.0",
    urls = ["https://github.com/bazelbuild/rules_docker/archive/v0.4.0.tar.gz"],
)

load(
    "@io_bazel_rules_docker//java:image.bzl",
    _java_image_repos = "repositories",
)

_java_image_repos()
