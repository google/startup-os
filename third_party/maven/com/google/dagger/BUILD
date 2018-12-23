java_library(
    name = "dagger",
    exports = [
        "//external:jar/mvncom/google/dagger/dagger"
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
        "//external:jar/mvncom/google/dagger/dagger_compiler"
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
        "//external:jar/mvncom/google/dagger/dagger_producers"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/javax/inject:javax_inject",
        "//third_party/maven/org/checkerframework:checker_compat_qual",
        ":dagger"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "dagger_spi",
    exports = [
        "//external:jar/mvncom/google/dagger/dagger_spi"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/guava:guava",
        "//third_party/maven/javax/inject:javax_inject",
        ":dagger",
        ":dagger_producers"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)


