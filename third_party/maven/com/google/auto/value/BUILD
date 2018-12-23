java_library(
    name = "auto_value",
    exported_plugins = [
        ":auto_value_plugin"
    ],
    exports = [
        "//external:jar/mvncom/google/auto/value/auto_value"
    ],
    licenses = [
        "permissive"
    ],
    visibility = [
        "//visibility:public"
    ]
)
java_plugin(
    name = "auto_value_plugin",
    deps = [
        "//external:jar/mvncom/google/auto/value/auto_value"
    ],
    licenses = [
        "permissive"
    ],
    processor_class = "com.google.auto.value.processor.AutoValueProcessor",
    visibility = [
        "//visibility:public"
    ]
)




java_library(
    name = "auto_value_annotations",
    exports = [
        "//external:jar/mvncom/google/auto/value/auto_value_annotations"
    ],
    licenses = [
        "permissive"
    ],
    visibility = [
        "//visibility:public"
    ]
)


