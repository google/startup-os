java_library(
    name = "failureaccess",
    exports = [
        "//external:jar/mvncom/google/guava/failureaccess"
    ],
    licenses = [
        "permissive"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "guava",
    exports = [
        "//external:jar/mvncom/google/guava/guava"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/code/findbugs:jsr305",
        "//third_party/maven/com/google/errorprone:error_prone_annotations",
        "//third_party/maven/com/google/j2objc:j2objc_annotations",
        "//third_party/maven/org/checkerframework:checker_qual",
        "//third_party/maven/org/codehaus/mojo:animal_sniffer_annotations",
        ":failureaccess",
        ":listenablefuture"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "listenablefuture",
    exports = [
        "//external:jar/mvncom/google/guava/listenablefuture"
    ],
    licenses = [
        "permissive"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)


