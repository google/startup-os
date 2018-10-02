# rules-updater

## Purpose

`rules-updater` is a tool to generate Firestore rules according
to users in prototxt-stored `ReviewerConfig`

## How to run

`bazel run //tools/reviewer/rules-updater:rules_updater -- 
--config reviewer_config.prototxt 
--output tools/reviewer/webapp/firestore.rules`
