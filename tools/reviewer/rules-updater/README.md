# rules-updater

## Purpose

`rules-updater` is a tool to generate Firestore rules according
to users in prototxt-stored `ReviewerConfig`

## How to run

`$(pwd)` is needed because `bazel run` creates a sandbox directory
which it changes current directory to

`bazel run //tools/reviewer/rules-updater:rules_updater -- 
--config $(pwd)/reviewer_config.prototxt 
--output $(pwd)/tools/reviewer/webapp/firestore.rules`
