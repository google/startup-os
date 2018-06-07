load("@io_bazel_rules_docker//java:image.bzl", "java_image")

# bazel build generates two targets:
# //examples/docker/hello_server (runs hello_server in container)
# and
# //examples/docker/hello_server.binary (runs hello_server on bare metal)
#
# To load a container to docker, follow these steps:
#
# $ bazel build //examples/docker:hello_server.tar 
#
# $ docker load -i bazel-bin/examples/docker/hello_server.tar
# 3012758200ff: Loading layer [=================================================>]  18.12MB/18.12MB
# 2986b553e1e9: Loading layer [=================================================>]  1.956MB/1.956MB
# 418f0d19e9db: Loading layer [=================================================>]  102.4MB/102.4MB
# c0e10e23215d: Loading layer [=================================================>]  20.48kB/20.48kB

# Loaded image: bazel/examples/docker:hello_server
#
# $ docker images
# REPOSITORY              TAG                 IMAGE ID            CREATED             SIZE
# bazel/examples/docker   hello_server        beb07c1df42a        48 years ago        121MB

java_image(
    name = "hello_server",
    srcs = ["HelloServer.java"],
    main_class = "com.google.startupos.examples.docker.HelloServer",
)
