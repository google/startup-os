java_library(
    name = "fontbox",
    exports = [
        "//external:jar/org/apache/pdfbox/fontbox"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/commons_logging:commons_logging"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "pdfbox",
    exports = [
        "//external:jar/org/apache/pdfbox/pdfbox"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/commons_logging:commons_logging",
        ":fontbox"
    ],
    visibility = [
        "//visibility:public"
    ]
)


