#!/usr/bin/env bash

# Pre-commit hook for StartupOS
# Run it before committing to perform several quality checks
# so you won't fail early on review
# Either do it manually or by creating a symlink
# To do this, execute, from repo root
# ln -s $(pwd)/tools/pre-commit.sh $(pwd)/.git/hooks/pre-commit

tools/checkstyle/check-for-missing-targets.sh
exit $?
