# To find the sha256 for an http_archive, run wget on the URL to download the
# file, and use sha256sum on the file to produce the sha256.

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_file")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_jar")

load("//third_party/maven:package-lock.bzl", "maven_dependencies")

RULES_JVM_EXTERNAL_TAG = "1.0"
RULES_JVM_EXTERNAL_SHA = "48e0f1aab74fabba98feb8825459ef08dcc75618d381dff63ec9d4dd9860deaa"

maven_dependencies()

http_archive(
    name = "io_grpc_grpc_java",
    sha256 = "0b86e44f9530fd61eb044b3c64c7579f21857ba96bcd9434046fd22891483a6d",
    strip_prefix = "grpc-java-1.18.0",
    urls = ["https://github.com/grpc/grpc-java/archive/v1.18.0.tar.gz"],
)

load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")

# TODO: Figure out if we also need omit_com_google_protobuf_javalite = True
grpc_java_repositories(
    omit_com_google_api_grpc_google_common_protos = True,
    omit_com_google_auth_google_auth_library_credentials = True,
    omit_com_google_auth_google_auth_library_oauth2_http = True,    
    omit_com_google_code_findbugs_jsr305 = True,
    omit_com_google_code_gson = True,
    omit_com_google_errorprone_error_prone_annotations = True,
    omit_com_google_guava = True,
    omit_com_google_protobuf = True,
    omit_com_google_protobuf_javalite = True,
    omit_com_google_protobuf_nano_protobuf_javanano = True,
    omit_com_google_re2j = True,
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
    omit_javax_annotation = True,
    omit_org_codehaus_mojo_animal_sniffer_annotations = True
)

# Google Maven Repository
http_archive(
    name = "gmaven_rules",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
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

http_archive(
    name = "tsfmt",
    urls = ["https://github.com/oferb/startupos-binaries/releases/download/0.1.03/tsfmt.zip"],
    sha256 = "b07799e1c6a2c8cd5a2e258833a80a9234b346968588ea53f7298eba76f610fc",
    build_file_content = "exports_files(['cli-linux', 'cli-macos'])"
)

http_file(
    name = "buildifier",
    executable = True,
    sha256 = "25159de982ec8896fc8213499df0a7003dfb4a03dd861f90fa5679d16faf0f99",
    urls = ["https://github.com/bazelbuild/buildtools/releases/download/0.22.0/buildifier"],
)

http_file(
    name = "buildifier_osx",
    executable = True,
    sha256 = "ceeedbd3ae0479dc2a5161e17adf7eccaba146b650b07063976df58bc37d7c44",
    urls = ["https://github.com/bazelbuild/buildtools/releases/download/0.22.0/buildifier.osx"],
)

http_file(
    name = "buildozer",
    executable = True,
    sha256 = "7750fe5bfb1247e8a858f3c87f63a5fb554ee43cb10efc1ce46c2387f1720064",
    urls = ["https://github.com/bazelbuild/buildtools/releases/download/0.22.0/buildozer"],
)

http_file(
    name = "buildozer_osx",
    executable = True,
    sha256 = "f2bcb59b96b1899bc27d5791f17a218f9ce76261f5dcdfdbd7ad678cf545803f",
    urls = ["https://github.com/bazelbuild/buildtools/releases/download/0.22.0/buildozer.osx"],
)

http_file(
    name = "unused_deps",
    executable = True,
    sha256 = "bc8a45bdeabdf4db642ebe2f602a653362f7f3c0ca28717fc14441735910eeb0",
    urls = ["https://github.com/bazelbuild/buildtools/releases/download/0.22.0/unused_deps"],
)

http_file(
    name = "unused_deps_osx",
    executable = True,
    sha256 = "c7053d0d371812f98a14f811ebfe680b5537f4e8f3d545b2e76bb3f9d142bf2d",
    urls = ["https://github.com/bazelbuild/buildtools/releases/download/0.22.0/unused_deps.osx"],
)

# Rules for examples/docker/
http_archive(
    name = "io_bazel_rules_docker",
    strip_prefix = "rules_docker-0.5.1",
    sha256 = "29d109605e0d6f9c892584f07275b8c9260803bf0c6fcb7de2623b2bedc910bd",
    urls = ["https://github.com/bazelbuild/rules_docker/archive/v0.5.1.tar.gz"],
)

http_jar(
    name = "grpc_polyglot",
    sha256 = "df48c8ec38a39d4dd4d134250655ee1f1880e953bf30ce54144e1b04d0be0baf",
    url = "https://github.com/grpc-ecosystem/polyglot/releases/download/v2.0.0/polyglot.jar"
)

http_file(
    name = "protoc_bin",
    executable = True,
    sha256 = "6003de742ea3fcf703cfec1cd4a3380fd143081a2eb0e559065563496af27807",
    urls = ["https://github.com/google/protobuf/releases/download/v3.6.1/protoc-3.6.1-linux-x86_64.zip"]
)

http_file(
    name = "protoc_bin_osx",
    executable = True,
    sha256 = "0decc6ce5beed07f8c20361ddeb5ac7666f09cf34572cca530e16814093f9c0c",
    urls = ["https://github.com/google/protobuf/releases/download/v3.6.1/protoc-3.6.1-osx-x86_64.zip"]
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
    sha256 = "cdd93cdf24d11ccd7bad6a4d55c9bbe55e776c3972ef177974512d5aa58debd7",
    urls = ["https://github.com/oferb/startupos-binaries/releases/download/0.1.02/grpc_java_plugin_linux"],
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
    name = "shfmt",
    executable = True,
    sha256 = "adb6022679f230270c87fd447de0eca08e694189a18bcc9490cd3971917fbcb4",
    urls = ["https://github.com/mvdan/sh/releases/download/v2.6.3/shfmt_v2.6.3_linux_amd64"]
)

http_file(
    name = "shfmt_osx",
    executable = True,
    sha256 = "5e1659999df29f06ec90e533670aff336957b43ce434c31d5bbc3e268a85dfae",
    urls = ["https://github.com/mvdan/sh/releases/download/v2.6.3/shfmt_v2.6.3_darwin_amd64"]
)

http_jar(
    name = "bazel_deps",
    sha256 = "98b05c2826f2248f70e7356dc6c78bc52395904bb932fbb409a5abf5416e4292",
    urls = ["https://github.com/oferb/startupos-binaries/releases/download/0.1.01/bazel_deps.jar"],
)

# Docker rules and containers
load("@io_bazel_rules_docker//container:container.bzl", "container_pull")
load("@io_bazel_rules_docker//java:image.bzl", _java_image_repos = "repositories")
_java_image_repos()
container_pull(
    name = "java_base",
    registry = "gcr.io",
    repository = "distroless/java",
    # 'tag' is also supported, but digest is encouraged for reproducibility.
    digest = "sha256:8c1769cb253bdecc257470f7fba05446a55b70805fa686f227a11655a90dfe9e",
)
container_pull(
    name = "alpine_java_git",
    registry = "gcr.io",
    repository = "startup-os/alpine-java-git",
    #tag = "latest",
    digest = "sha256:d27494f6034ff3cd38bd2cde9bfea019f568565d2bb14d2d450a44cd891cf28c",
)
