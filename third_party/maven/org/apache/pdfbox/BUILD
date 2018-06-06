java_library(
    name = "fontbox",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/org/apache/pdfbox/fontbox",
    ],
    runtime_deps = [
        "//third_party/maven/commons_logging",
    ],
)

java_library(
    name = "pdfbox",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/org/apache/pdfbox/pdfbox",
    ],
    runtime_deps = [
        ":fontbox",
        "//third_party/maven/commons_logging",
    ],
)
