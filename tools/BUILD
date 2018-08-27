package(default_visibility = ["//visibility:public"])

sh_binary(
    name = "buildifier",
    srcs = ["buildtools_wrappers/buildifier.sh"],
    data = [
        "@buildifier//file",
        "@buildifier_osx//file",
    ],
)

sh_binary(
    name = "buildozer",
    srcs = ["buildtools_wrappers/buildozer.sh"],
    data = [
        "@buildozer//file",
        "@buildozer_osx//file",
    ],
)

sh_binary(
    name = "unused_deps",
    srcs = ["buildtools_wrappers/unused_deps.sh"],
    data = [
        "@unused_deps//file",
        "@unused_deps_osx//file",
    ],
)

java_binary(
    name = "grpc_polyglot",
    main_class = "me.dinowernli.grpc.polyglot.Main",
    runtime_deps = ["@grpc_polyglot//jar"],
)

sh_binary(
    name = "protoc",
    srcs = ["protoc.sh"],
    data = [
        "@protoc_bin//file",
        "@protoc_bin_osx//file",
    ],
)

proto_lang_toolchain(
    name = "java_toolchain",
    command_line = "--java_out=$(OUT)",
    runtime = "//third_party/maven/com/google/protobuf:protobuf_java",
    visibility = ["//visibility:public"],
)

sh_binary(
    name = "grpc_java_plugin",
    srcs = ["grpc_java_plugin.sh"],
    data = [
        "@grpc_java_plugin_linux//file",
        "@grpc_java_plugin_osx//file",
    ],
    visibility = ["//visibility:public"],
)

java_import(
    name = "bazel_deps_jar",
    jars = ["@bazel_deps//file"],
)

java_binary(
    name = "bazel_deps",
    main_class = "com.github.johnynek.bazel_deps.ParseProject",
    visibility = ["//visibility:public"],
    runtime_deps = [":bazel_deps_jar"],
)

sh_binary(
    name = "grpcwebproxy",
    srcs = ["grpcwebproxy.sh"],
    data = [
        "@grpcwebproxy_linux//file",
        "@grpcwebproxy_osx//file",
    ],
    visibility = ["//visibility:public"],
)

sh_binary(
    name = "clang_format",
    srcs = ["clang-format.sh"],
    data = [
        # See TODO in tools/formatter/BUILD about download size.
        #"@clang_format_bin//file",
        #"@clang_format_bin_osx//file",
    ],
)
