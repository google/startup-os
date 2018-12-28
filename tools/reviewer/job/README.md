# Reviewer-job

## Purpose

Job is a standalone tool designed to be run on a 
separate cloud machine (Heroku/Google Compute Platform) as a
supplement to Reviewer workflow.

## Architecture

Each second, a timer task `TaskExecutor` asks each task instance
whether it wants to be run by `shouldRun` and if so, runs it on a
separate thread from thread pool.


## Tasks

### ReviewerMetadataUpdaterTask

Pulls the git repo from remote specified in `--repo_url` cmdline arg.
Computes checksum of `.reviewer/global_registry.prototxt`. If it has been changed
from previous version, push it to Firestore.


### CITask
Pulls `CiRequest` from `/reviewer/ci/requests`, clones all the `Repo`s it
contains, checks out relevant commits for each of them, runs `reviewer-ci.sh`
and stores `CiResponse` in `/reviewer/ci/responses/`


### SubmitterTask
For each `Diff` which has `Status.SUBMITTING`, pulls latest `CiResponse` 
from `/reviewer/ci/responses/<diffId>/history`, verifies that all `TargetResult`s are 
successful, clones all the `Repo`s it contains,
checks out `D<diffId>` branch for each of them, checks it matches the commit CI has been ran for,
merges them to `master`, pushes all repos and updates status to `Status.SUBMITTED`
