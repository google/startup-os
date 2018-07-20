java_test(
    name = "MavenDepsWhitelistTest",
    srcs = ["MavenDepsWhiteListTest.java"],
    data = [
        "//:dependencies.yaml",
        "//:whitelist.yaml",
    ],
    test_class = "com.google.startupos.tools.dep_whitelist.MavenDepsWhiteListTest",
    deps = [
        "//third_party/maven/org/yaml:snakeyaml",
    ],
)

java_test(
    name = "WorkspaceDepsWhitelistTest",
    srcs = ["WorkspaceDepsWhiteListTest.java"],
    data = [
        "//:WORKSPACE",
        "//:whitelist.yaml",
    ],
    test_class = "com.google.startupos.tools.dep_whitelist.WorkspaceDepsWhiteListTest",
    deps = [
        "//third_party/maven/org/yaml:snakeyaml",
    ],
)
