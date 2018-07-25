java_test(
    name = "MavenDepsWhitelistTest",
    srcs = ["MavenDepsWhitelistTest.java"],
    data = [
        "//:dependencies.yaml",
        "//:whitelist.yaml",
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
        "//:whitelist.yaml",
    ],
    test_class = "com.google.startupos.tools.dep_whitelist.WorkspaceDepsWhitelistTest",
    deps = [
        "//third_party/maven/org/yaml:snakeyaml",
    ],
)
