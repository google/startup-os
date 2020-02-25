# Structure representing info about Java source files
JavaSourceFiles = provider(
    fields = {
        'files' : 'java source files'
    }
)

# This aspect is responsible for collecting Java sources for target
# When applied to target, `JavaSourceFiles` struct will be attached to
# target info
def collect_sources_impl(target, ctx):
    files = []
    if hasattr(ctx.rule.attr, 'srcs'):
        for src in ctx.rule.attr.srcs:
            for file in src.files.to_list():
                if file.extension == 'java':
                    files.append(file)
    return [JavaSourceFiles(files = files)]


collect_sources = aspect(
    implementation = collect_sources_impl,
)


# `ctx` is rule context: https://docs.bazel.build/versions/master/skylark/lib/ctx.html
def _checkstyle_test_impl(ctx):
    suppressions = ctx.file.suppressions
    opts = ctx.attr.opts
    sopts = ctx.attr.string_opts

    # Checkstyle and its dependencies
    checkstyle_dependencies = ctx.attr._checkstyle[JavaInfo].transitive_runtime_deps
    classpath = ":".join([file.path for file in checkstyle_dependencies.to_list()])


    args = ""
    inputs = []
    if ctx.file.config:
      args += " -c %s" % ctx.file.config.path
      inputs.append(ctx.file.config)
    if suppressions:
      inputs.append(suppressions)

    # All the java files should be added to depset for executing the checkstyle script
    sourcefiles = []
    for target in ctx.attr.targets:
        sourcefiles += target[JavaSourceFiles].files

    # Create a file with all sourcefile paths to be passed to the command
    filename = "targets.txt"
    arg_file = ctx.actions.declare_file(filename)
    file_paths = []
    for file in sourcefiles:
        file_paths.append(file.path)

    ctx.actions.write(output = arg_file, content = " ".join(file_paths))

    # Build command to run Checkstyle test
    cmd = " ".join(
        ["java -cp %s com.puppycrawl.tools.checkstyle.Main" % classpath] +
        [args] +
        ["--%s" % x for x in opts] +
        ["--%s %s" % (k, sopts[k]) for k in sopts] +
        ["@%s" % arg_file.short_path]
    )

    # Wrap checkstyle command in a shell script so allow_failure is supported
    ctx.actions.expand_template(
        template = ctx.file._checkstyle_sh_template,
        output = ctx.outputs.checkstyle_script,
        substitutions = {
            "{command}" : cmd,
            "{allow_failure}": str(int(ctx.attr.allow_failure)),
        },
        is_executable = True,
    )

    files = [ctx.outputs.checkstyle_script, ctx.file.license, arg_file] + sourcefiles + checkstyle_dependencies.to_list() + inputs
    runfiles = ctx.runfiles(
        files = files,
        collect_data = True
    )
    return DefaultInfo(
        executable = ctx.outputs.checkstyle_script,
        files = depset(files),
        runfiles = runfiles,
    )

checkstyle_test = rule(
    implementation = _checkstyle_test_impl,
    test = True,
    attrs = {
        "config": attr.label(
            allow_single_file=True,
            doc = "A checkstyle configuration file",
            default = "//tools/checkstyle:config.xml",
        ),
        "suppressions": attr.label(
            allow_single_file=True,
            doc = ("A file for specifying files and lines " +
                   "that should be suppressed from checks." +
                   "Example: https://github.com/checkstyle/checkstyle/blob/master/config/suppressions.xml")
        ),
        "license": attr.label(
            allow_single_file=True,
            doc = "A license file that can be used with the checkstyle license target",
            default = "//tools/checkstyle:license-header.txt",
        ),
        "opts": attr.string_list(
            doc = "Options to be passed on the command line that have no argument"
        ),
        "string_opts": attr.string_dict(
            doc = "Options to be passed on the command line that have an argument"
        ),
        "targets": attr.label_list(
            doc = "The java_library targets to check sources on",
            aspects = [collect_sources],
            mandatory = True
        ),
        "allow_failure": attr.bool(
            default = False,
            doc = "Successfully finish the test even if checkstyle failed"
        ),
        "_checkstyle_sh_template": attr.label(
             allow_single_file = True,
             default = "//tools/checkstyle:checkstyle.sh"
        ),
        "_checkstyle": attr.label(
            default = "//third_party/maven/com/puppycrawl/tools:checkstyle"
        ),
    },
    outputs = {
        "checkstyle_script": "%{name}.sh",
    },
)
