java_library(
    name = "netty_buffer",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvnio/netty/netty_buffer",
    ],
    runtime_deps = [
        ":netty_common",
    ],
)

java_library(
    name = "netty_codec",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvnio/netty/netty_codec",
    ],
    runtime_deps = [
        ":netty_buffer",
        ":netty_common",
        ":netty_transport",
    ],
)

java_library(
    name = "netty_codec_http",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvnio/netty/netty_codec_http",
    ],
    runtime_deps = [
        ":netty_buffer",
        ":netty_codec",
        ":netty_common",
        ":netty_transport",
    ],
)

java_library(
    name = "netty_codec_http2",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/mvnio/netty/netty_codec_http2",
    ],
    runtime_deps = [
        ":netty_buffer",
        ":netty_codec",
        ":netty_codec_http",
        ":netty_common",
        ":netty_handler",
        ":netty_transport",
    ],
)

java_library(
    name = "netty_codec_socks",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvnio/netty/netty_codec_socks",
    ],
    runtime_deps = [
        ":netty_codec",
    ],
)

java_library(
    name = "netty_common",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvnio/netty/netty_common",
    ],
)

java_library(
    name = "netty_handler",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvnio/netty/netty_handler",
    ],
    runtime_deps = [
        ":netty_buffer",
        ":netty_codec",
        ":netty_common",
        ":netty_transport",
    ],
)

java_library(
    name = "netty_handler_proxy",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvnio/netty/netty_handler_proxy",
    ],
    runtime_deps = [
        ":netty_codec_http",
        ":netty_codec_socks",
        ":netty_transport",
    ],
)

java_library(
    name = "netty_resolver",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvnio/netty/netty_resolver",
    ],
    runtime_deps = [
        ":netty_common",
    ],
)

java_library(
    name = "netty_tcnative_boringssl_static",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//visibility:public",
    ],
    exports = [
        "//external:jar/mvnio/netty/netty_tcnative_boringssl_static",
    ],
)

java_library(
    name = "netty_transport",
    licenses = [
        "permissive",
    ],
    visibility = [
        "//third_party/maven:__subpackages__",
    ],
    exports = [
        "//external:jar/mvnio/netty/netty_transport",
    ],
    runtime_deps = [
        ":netty_buffer",
        ":netty_common",
        ":netty_resolver",
    ],
)
