#!/usr/bin/env bash

# Pre-commit hook for StartupOS
# Run it before committing to perform several quality checks
# so you won't fail early on review
# Either do it manually or by creating a symlink
# To do this, execute, from repo root
# ln -s $(pwd)/tools/pre-commit.sh $(pwd)/.git/hooks/pre-commit

bazel query 'kind(java_*, //...) - kind(java_proto_library, //...) - //third_party/... - attr("tags", "checkstyle_ignore", //...)' | sort >/tmp/all_java_targets
bazel query 'kind(checkstyle_test, //...)' | sed -e 's|-checkstyle||g' | sort >/tmp/checkstyle_covered_java_targets

NON_COVERED_JAVA_TARGETS=$(comm -23 /tmp/all_java_targets /tmp/checkstyle_covered_java_targets)

if [[ ! -z "$NON_COVERED_JAVA_TARGETS" ]]; then
  echo "$(tput setaf 1)[!] These java targets do not have accompanying checkstyle targets:"$(tput sgr0)
  echo -e $(sed -e 's/\s\+/\n/g' <<<${NON_COVERED_JAVA_TARGETS})
  exit 1
fi
