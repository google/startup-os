java_library(
    name = "dagger",
    exports = [
        "//external:jar/com/google/dagger/dagger"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/javax/inject:javax_inject"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "dagger_compiler",
    exports = [
        "//external:jar/com/google/dagger/dagger_compiler"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/googlejavaformat:google_java_format",
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/com/squareup:javapoet",
        "//third_party/maven/javax/annotation:jsr250_api",
        "//third_party/maven/javax/inject:javax_inject",
        ":dagger",
        ":dagger_producers",
        ":dagger_spi"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "dagger_producers",
    exports = [
        "//external:jar/com/google/dagger/dagger_producers"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/org/checkerframework:checker_compat_qual"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "dagger_spi",
    exports = [
        "//external:jar/com/google/dagger/dagger_spi"
    ],
    licenses = [
        "permissive"
    ],
    visibility = [
        "//visibility:public"
    ]
)


