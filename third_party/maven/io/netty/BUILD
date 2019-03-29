java_library(
    name = "netty_buffer",
    exports = [
        "//external:jar/mvnio/netty/netty_buffer"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        ":netty_common"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "netty_codec",
    exports = [
        "//external:jar/mvnio/netty/netty_codec"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        ":netty_buffer",
        ":netty_common",
        ":netty_transport"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "netty_codec_http",
    exports = [
        "//external:jar/mvnio/netty/netty_codec_http"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        ":netty_buffer",
        ":netty_codec",
        ":netty_common",
        ":netty_transport"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "netty_codec_http2",
    exports = [
        "//external:jar/mvnio/netty/netty_codec_http2"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        ":netty_buffer",
        ":netty_codec",
        ":netty_codec_http",
        ":netty_common",
        ":netty_handler",
        ":netty_transport"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "netty_codec_socks",
    exports = [
        "//external:jar/mvnio/netty/netty_codec_socks"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        ":netty_codec"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "netty_common",
    exports = [
        "//external:jar/mvnio/netty/netty_common"
    ],
    licenses = [
        "permissive"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "netty_handler",
    exports = [
        "//external:jar/mvnio/netty/netty_handler"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        ":netty_buffer",
        ":netty_codec",
        ":netty_common",
        ":netty_transport"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "netty_handler_proxy",
    exports = [
        "//external:jar/mvnio/netty/netty_handler_proxy"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        ":netty_codec_http",
        ":netty_codec_socks",
        ":netty_transport"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "netty_resolver",
    exports = [
        "//external:jar/mvnio/netty/netty_resolver"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        ":netty_common"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)



java_library(
    name = "netty_tcnative_boringssl_static",
    exports = [
        "//external:jar/mvnio/netty/netty_tcnative_boringssl_static"
    ],
    licenses = [
        "permissive"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "netty_transport",
    exports = [
        "//external:jar/mvnio/netty/netty_transport"
    ],
    licenses = [
        "permissive"
    ],
    runtime_deps = [
        ":netty_buffer",
        ":netty_common",
        ":netty_resolver"
    ],
    visibility = [
        "//third_party/maven:__subpackages__"
    ]
)


