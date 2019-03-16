# THIS FILE IS AUTO-GENERATED

package(default_visibility = ["//visibility:public"])

load("//tools/checkstyle:checkstyle.bzl", "checkstyle_test")

java_library(
    name = "aa_command",
    srcs = ["AaCommand.java"],
)

checkstyle_test(
    name = "aa_command-checkstyle",
    target = ":aa_command",
)

java_library(
    name = "add_repo_command",
    srcs = ["AddRepoCommand.java"],
    deps = [
        ":aa_command",
        "//common",
        "//common/flags",
        "//common/repo",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer/aa:config_java_proto",
    ],
)

checkstyle_test(
    name = "add_repo_command-checkstyle",
    target = ":add_repo_command",
)

java_library(
    name = "diff_command",
    srcs = ["DiffCommand.java"],
    deps = [
        ":aa_command",
        "//common",
        "//common/flags",
        "//common/repo",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer/local_server/service:code_review_java_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
    ],
)

checkstyle_test(
    name = "diff_command-checkstyle",
    target = ":diff_command",
)

java_library(
    name = "fix_command",
    srcs = ["FixCommand.java"],
    deps = [
        ":aa_command",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer/aa/commands/checks:fix_command_check",
        "//tools/reviewer/aa/commands/checks:formatting_check",
    ],
)

checkstyle_test(
    name = "fix_command-checkstyle",
    target = ":fix_command",
)

java_library(
    name = "init_command",
    srcs = ["InitCommand.java"],
    deps = [
        ":aa_command",
        "//common",
        "//common/flags",
        "//common/repo",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer:reviewer_registry_java_proto",
    ],
)

checkstyle_test(
    name = "init_command-checkstyle",
    target = ":init_command",
)

java_library(
    name = "kill_server_command",
    srcs = ["KillServerCommand.java"],
    deps = [
        ":aa_command",
        "//common",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "kill_server_command-checkstyle",
    target = ":kill_server_command",
)

java_library(
    name = "patch_command",
    srcs = ["PatchCommand.java"],
    deps = [
        ":aa_command",
        "//common",
        "//common/flags",
        "//common/repo",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "patch_command-checkstyle",
    target = ":patch_command",
)

java_library(
    name = "review_command",
    srcs = ["ReviewCommand.java"],
    deps = [
        ":aa_command",
        "//common",
        "//common/repo",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer/aa:config_java_proto",
        "//tools/reviewer/local_server/service:code_review_java_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
    ],
)

checkstyle_test(
    name = "review_command-checkstyle",
    target = ":review_command",
)

java_library(
    name = "snapshot_command",
    srcs = ["SnapshotCommand.java"],
    deps = [
        ":aa_command",
        "//common",
        "//common/repo",
        "//common/repo:repo_java_proto",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer/local_server/service:code_review_java_grpc",
    ],
)

checkstyle_test(
    name = "snapshot_command-checkstyle",
    target = ":snapshot_command",
)

java_library(
    name = "submit_command",
    srcs = ["SubmitCommand.java"],
    deps = [
        ":aa_command",
        "//common",
        "//common/repo",
        "//third_party/maven/io/grpc:grpc_core",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer/local_server/service:code_review_java_grpc",
        "//tools/reviewer/local_server/service:code_review_java_proto",
    ],
)

checkstyle_test(
    name = "submit_command-checkstyle",
    target = ":submit_command",
)

java_library(
    name = "sync_command",
    srcs = ["SyncCommand.java"],
    deps = [
        ":aa_command",
        "//common",
        "//common/repo",
        "//third_party/maven/javax/inject:javax_inject",
    ],
)

checkstyle_test(
    name = "sync_command-checkstyle",
    target = ":sync_command",
)

java_library(
    name = "workspace_command",
    srcs = ["WorkspaceCommand.java"],
    deps = [
        ":aa_command",
        "//common",
        "//common/flags",
        "//third_party/maven/javax/inject:javax_inject",
        "//tools/reviewer/aa:config_java_proto",
    ],
)

checkstyle_test(
    name = "workspace_command-checkstyle",
    target = ":workspace_command",
)
