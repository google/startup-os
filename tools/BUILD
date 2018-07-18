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
