java_library(
    name = "auto_service",
    exported_plugins = [
        ":auto_service_plugin",
    ],
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/mvncom/google/auto/service/auto_service",
    ],
    runtime_deps = [
        "//third_party/maven/com/google/auto:auto_common",
        "//third_party/maven/com/google/guava",
    ],
)

java_plugin(
    name = "auto_service_plugin",
    licenses = [
        "permissive",
    ],
    processor_class = "com.google.auto.service.processor.AutoServiceProcessor",
    visibility = [
        "//visibility:public",
    ],
    deps = [
        "//external:jar/mvncom/google/auto/service/auto_service",
        "//third_party/maven/com/google/auto:auto_common",
        "//third_party/maven/com/google/guava",
    ],
)
