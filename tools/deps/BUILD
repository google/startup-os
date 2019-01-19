load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

exports_files([
    "whitelist.yaml",
])

proto_library(
    name = "dependencies_proto",
    srcs = ["dependencies.proto"],
)

java_proto_library(
    name = "dependencies_java_proto",
    deps = [":dependencies_proto"],
)

java_test(
    name = "MavenDepsWhitelistTest",
    srcs = ["MavenDepsWhitelistTest.java"],
    data = [
        "//:dependencies.yaml",
        "//tools/deps:whitelist.yaml",
    ],
    test_class = "com.google.startupos.tools.deps.MavenDepsWhitelistTest",
    deps = [
        "//third_party/maven/org/yaml:snakeyaml",
    ],
)

checkstyle_test(
    name = "MavenDepsWhitelistTest-checkstyle",
    target = ":MavenDepsWhitelistTest",
)

java_test(
    name = "WorkspaceDepsWhitelistTest",
    srcs = ["WorkspaceDepsWhitelistTest.java"],
    data = [
        "//:WORKSPACE",
        "//tools/deps:whitelist.yaml",
    ],
    test_class = "com.google.startupos.tools.deps.WorkspaceDepsWhitelistTest",
    deps = [
        "//third_party/maven/org/apache/commons:commons_lang3",
        "//third_party/maven/org/yaml:snakeyaml",
    ],
)

checkstyle_test(
    name = "WorkspaceDepsWhitelistTest-checkstyle",
    target = ":WorkspaceDepsWhitelistTest",
)

java_test(
    name = "PackageLockWhitelistTest",
    srcs = ["PackageLockWhitelistTest.java"],
    data = [
        "//third_party/maven:package-lock.bzl",
        "//tools/deps:whitelist.yaml",
    ],
    test_class = "com.google.startupos.tools.deps.PackageLockWhitelistTest",
    deps = [
        ":dependencies_java_proto",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
        "//third_party/maven/org/yaml:snakeyaml",
    ],
)

checkstyle_test(
    name = "PackageLockWhitelistTest-checkstyle",
    target = ":PackageLockWhitelistTest",
)

java_binary(
    name = "workspace_patcher",
    srcs = ["WorkspacePatcher.java"],
    main_class = "com.google.startupos.tools.deps.WorkspacePatcher",
    deps = [
        "//common/flags",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
    ],
)

checkstyle_test(
    name = "workspace_patcher-checkstyle",
    target = ":workspace_patcher",
)
