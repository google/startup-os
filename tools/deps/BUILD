exports_files([
    "whitelist.yaml",
])

java_test(
    name = "MavenDepsWhitelistTest",
    srcs = ["MavenDepsWhitelistTest.java"],
    data = [
        "//:dependencies.yaml",
        "//tools/deps:whitelist.yaml",
    ],
    test_class = "com.google.startupos.tools.dep_whitelist.MavenDepsWhitelistTest",
    deps = [
        "//third_party/maven/org/yaml:snakeyaml",
    ],
)

java_test(
    name = "WorkspaceDepsWhitelistTest",
    srcs = ["WorkspaceDepsWhitelistTest.java"],
    data = [
        "//:WORKSPACE",
        "//tools/deps:whitelist.yaml",
    ],
    test_class = "com.google.startupos.tools.dep_whitelist.WorkspaceDepsWhitelistTest",
    deps = [
        "//third_party/maven/org/apache/commons:commons_lang3",
        "//third_party/maven/org/yaml:snakeyaml",
    ],
)
