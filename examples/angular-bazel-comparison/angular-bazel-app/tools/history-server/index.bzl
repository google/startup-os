"""Simple Bazel wrapper around npm history-server package.
See https://www.npmjs.com/package/history-server
"""
load("@build_bazel_rules_nodejs//:defs.bzl", "nodejs_binary")

def history_server(templated_args = [], **kwargs):
  if not templated_args:
    templated_args = [native.package_name()]
  nodejs_binary(
      node_modules = "@history-server_runtime_deps//:node_modules",
      entry_point = "history-server/modules/cli.js",
      templated_args = templated_args,
      **kwargs)
