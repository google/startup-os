"""Simple Bazel wrapper around npm history-server package.
See https://www.npmjs.com/package/history-server
"""
load("@build_bazel_rules_nodejs//:defs.bzl", "nodejs_binary")

def history_server(port = 5432, args = [], **kwargs):
  if not args:
    args = [native.package_name()]
  args.extend(["-p", str(port)])
  nodejs_binary(
      node_modules = "@history-server_runtime_deps//:node_modules",
      entry_point = "history-server/modules/cli.js",
      args = args,
      **kwargs)
