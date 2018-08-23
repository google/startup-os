function createBuild(brickfile, brickCamel) {
  return `package(default_visibility = ["//visibility:public"])

load("@angular//:index.bzl", "ng_module")
load("@io_bazel_rules_sass//sass:sass.bzl", "sass_binary")
load("@build_bazel_rules_typescript//:defs.bzl", "ts_library", "ts_web_test_suite")

sass_binary(
    name = "${brickfile}-styles",
    src = "${brickfile}.component.scss",
)

ng_module(
    name = "${brickfile}",
    srcs = [
        "${brickfile}.component.ts",
        "${brickfile}.module.ts",
    ],
    assets = [
        ":${brickfile}.component.html",
        ":${brickfile}-styles",
    ],
    deps = [
        "//src/services",
        "@angular//packages/core",
        "@angular//packages/forms",
        "@rxjs",
    ],
)

ts_library(
    name = "test_lib",
    testonly = 1,
    srcs = glob(["*.spec.ts"]),
    deps = [
        ":${brickfile}",
        "@angular//packages/core",
        "@angular//packages/core/testing",
        "@angular//packages/platform-browser",
        "@angular//packages/platform-browser-dynamic/testing",
    ],
)

ts_web_test_suite(
    name = "test",
    srcs = ["//:node_modules/tslib/tslib.js"],
    # do not sort
    bootstrap = [
        "//:node_modules/zone.js/dist/zone-testing-bundle.js",
        "//:node_modules/reflect-metadata/Reflect.js",
    ],
    browsers = [
        "@io_bazel_rules_webtesting//browsers:chromium-local",
    ],
    deps = [
        ":test_lib",
    ],
)
`;
}

module.exports = createBuild;
