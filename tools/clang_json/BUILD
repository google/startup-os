cc_binary(
    name = "update_compile_database",
    srcs = ["update_compile_database.cc"],
    deps = [
        ":extra_actions_base_cc_proto",
        "@com_google_protobuf//:protobuf",
        "@startupos_external_jsoncpp//:jsoncpp",
    ],
)

cc_proto_library(
    name = "extra_actions_base_cc_proto",
    deps = ["@io_bazel//src/main/protobuf:extra_actions_base_proto"],
)

action_listener(
    name = "compile_database_listener",
    extra_actions = [":compile_database_action"],
    mnemonics = ["CppCompile"],
    visibility = ["//visibility:public"],
)

extra_action(
    name = "compile_database_action",
    cmd = "$(location :update_compile_database) " +
          "$(EXTRA_ACTION_FILE) " +
          "$(output compile_commands.json.$(ACTION_ID))",
    out_templates = ["compile_commands.json.$(ACTION_ID)"],
    tools = [":update_compile_database"],
)
