# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

proto_library(
    name = "dependencies_proto",
    srcs = ["dependencies.proto"],
)

java_proto_library(
    name = "dependencies_java_proto",
    deps = [":dependencies_proto"],
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

java_test(
    name = "maven_deps_whitelist_test",
    srcs = ["MavenDepsWhitelistTest.java"],
    test_class = "com.google.startupos.tools.deps.MavenDepsWhitelistTest",
    deps = [
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/junit",
        "//third_party/maven/org/yaml:snakeyaml",
    ],
)

checkstyle_test(
    name = "maven_deps_whitelist_test-checkstyle",
    target = ":maven_deps_whitelist_test",
)

java_test(
    name = "package_lock_whitelist_test",
    srcs = ["PackageLockWhitelistTest.java"],
    test_class = "com.google.startupos.tools.deps.PackageLockWhitelistTest",
    deps = [
        ":dependencies_java_proto",
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/com/google/protobuf:protobuf_java_util",
        "//third_party/maven/junit",
        "//third_party/maven/org/yaml:snakeyaml",
    ],
)

checkstyle_test(
    name = "package_lock_whitelist_test-checkstyle",
    target = ":package_lock_whitelist_test",
)

java_test(
    name = "workspace_deps_whitelist_test",
    srcs = ["WorkspaceDepsWhitelistTest.java"],
    test_class = "com.google.startupos.tools.deps.WorkspaceDepsWhitelistTest",
    deps = [
        "//third_party/maven/com/google/flogger:flogger_system_backend",
        "//third_party/maven/junit",
        "//third_party/maven/org/apache/commons:commons_lang3",
        "//third_party/maven/org/yaml:snakeyaml",
    ],
)

checkstyle_test(
    name = "workspace_deps_whitelist_test-checkstyle",
    target = ":workspace_deps_whitelist_test",
)
