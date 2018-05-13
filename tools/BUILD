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
    name = "unused_deps",
    srcs = ["buildtools_wrappers/unused_deps.sh"],
    data = [
        "@unused_deps//file",
        "@unused_deps_osx//file",
    ],
)
