load("@io_bazel_rules_docker//java:image.bzl", "java_image")

# bazel build generates two targets:
# //examples/docker/app (runs app in container)
# and
# //examples/docker/app.binary (runs app on bare metal)
#
# To run target in docker:
# bazel run //examples/docker:app
#
# To load a container to docker, follow these steps:
#
# $ bazel build //examples/docker:app.tar 
#
# $ docker load -i bazel-bin/examples/docker/app.tar
# 3012758200ff: Loading layer [=================================================>]  18.12MB/18.12MB
# 2986b553e1e9: Loading layer [=================================================>]  1.956MB/1.956MB
# 418f0d19e9db: Loading layer [=================================================>]  102.4MB/102.4MB
# c0e10e23215d: Loading layer [=================================================>]  20.48kB/20.48kB
# Loaded image: bazel/examples/docker:app
#
# $ docker images
# REPOSITORY              TAG                 IMAGE ID            CREATED             SIZE
# bazel/examples/docker   app                 beb07c1df42a        48 years ago        121MB

java_image(
    name = "app",
    srcs = ["App.java"],
    main_class = "com.google.startupos.examples.docker.App",
)
