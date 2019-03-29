java_library(
    name = "auto_service",
    exported_plugins = [
        ":auto_service_plugin"
    ],
    exports = [
        "//external:jar/mvncom/google/auto/service/auto_service"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        "//third_party/maven/com/google/auto:auto_common",
        "//third_party/maven/com/google/guava:guava"
    ],
    visibility = [
        "//visibility:public"
    ]
)
java_plugin(
    name = "auto_service_plugin",
    deps = [
        "//external:jar/mvncom/google/auto/service/auto_service",
        "//third_party/maven/com/google/auto:auto_common",
        "//third_party/maven/com/google/guava:guava"
    ],
    licenses = [
        "permissive"
    ],
    processor_class = "com.google.auto.service.processor.AutoServiceProcessor",
    visibility = [
        "//visibility:public"
    ]
)



