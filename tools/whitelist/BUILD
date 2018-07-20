java_test(
    name = "MavenDepsWhitelistTest",
    srcs = ["MavenDepsWhiteListTest.java"],
    data = [
        "//:dependencies.yaml",
        "//:whitelist.yaml",
    ],
    test_class = "com.google.startupos.tools.whitelist.MavenDepsWhiteListTest",
    deps = [
        "//third_party/maven/org/yaml:snakeyaml",
    ],
)
