load("@io_bazel_rules_docker//java:image.bzl", "java_image")

# Generates two targets:
# //examples/docker/app (runs app in container)
# and
# //examples/docker/app.binary (runs app on bare metal)
java_image(
    name = "app",
    srcs = ["App.java"],
    main_class = "com.google.startupos.examples.docker.App",
)
