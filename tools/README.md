# Tools

![alt text](https://www.memecreator.org/static/images/memes/4810307.jpg)

We want to automate as much as possible. Here are some tools we use:
* Formatting - we automatically format using a [multi-language formatter](https://github.com/google/startup-os/tree/master/tools/formatter), run the formatter using [fix-formatting.sh](https://github.com/google/startup-os/blob/master/tools/fix_formatting.sh),
and verify it in CI (see [here](https://github.com/google/startup-os/blob/fedbc14b6e39f12721994651cd152b410004fa9b/.circleci/config.yml#L37)). 
* We use 3 tools to handle BUILD files (see [here](https://github.com/google/startup-os/tree/master/tools/buildtools_wrappers)):
  * [buildifier](https://github.com/bazelbuild/buildtools/blob/master/buildifier/README.md) - For formatting BUILD files
  * [buildozer](https://github.com/bazelbuild/buildtools/blob/master/buildozer/README.md) - For doing command-line operations on these files (e.g add dependency to target).
  * [unused_deps](https://github.com/bazelbuild/buildtools/tree/master/unused_deps) - For finding unneeded dependencies in java_library rules.
* Precommit checks: [tools/pre-commit.sh](https://github.com/google/startup-os/blob/master/tools/pre-commit.sh)
* CI (using CircleCI) to enforce formatting, pre-commit checks, compile and test everything (see [here](https://github.com/google/startup-os/tree/master/.circleci))
* Dependency management: see [tools/deps](https://github.com/google/startup-os/tree/master/tools/deps).

This part is still WIP:
* `aa` - a tool for a multi-repo code-review workflow (e.g "Create a PR from these changes across these 2 repos", "I'm done fixing, notify reviewer").
* `local_server` and `reviewer` are the code review webtool. The overall approach:
    * A local server serves code to browser (so works with any git server).
    * Code is synced through git
    * Review metadata (e.g comments) are synced through Firestore (nosql storage).
