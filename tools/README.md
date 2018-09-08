# Tools

![Fix all the things!](https://www.memecreator.org/static/images/memes/4810307.jpg)

We want to automate as much as possible. Here are some tools we use:
* Formatting - we automatically format using a [multi-language formatter](https://github.com/google/startup-os/tree/master/tools/formatter), run the formatter using [fix-formatting.sh](fix_formatting.sh),
and verify it in CI (see [here](https://github.com/google/startup-os/blob/fedbc14b6e39f12721994651cd152b410004fa9b/.circleci/config.yml#L37)). 
* We use 3 tools to handle BUILD files (see [here](tools/buildtools_wrappers)):
  * [buildifier](https://github.com/bazelbuild/buildtools/blob/master/buildifier/README.md) - For formatting BUILD files
  * [buildozer](https://github.com/bazelbuild/buildtools/blob/master/buildozer/README.md) - For doing command-line operations on these files (e.g add dependency to target).
  * [unused_deps](https://github.com/bazelbuild/buildtools/tree/master/unused_deps) - For finding unneeded dependencies in java_library rules.
* Precommit checks: [pre-commit.sh](pre-commit.sh)
* CI (using CircleCI) to enforce formatting, pre-commit checks, compile and test everything (see [here](https://github.com/google/startup-os/tree/master/.circleci))
* Dependency management: see [deps](deps).
* [Reviewer](reviewer) - a multi-repo code review tool, that works with any combination of on-prem, GitHub, GitLab, Bitbucket, Google CSR etc.
