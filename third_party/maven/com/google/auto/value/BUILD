java_library(
    name = "auto_value",
    exported_plugins = [
        ":auto_value_plugin",
    ],
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/mvncom/google/auto/value/auto_value",
    ],
)

java_plugin(
    name = "auto_value_plugin",
    licenses = [
        "permissive",
    ],
    processor_class = "com.google.auto.value.processor.AutoValueProcessor",
    visibility = [
        "//visibility:public",
    ],
    deps = [
        "//external:jar/mvncom/google/auto/value/auto_value",
    ],
)
