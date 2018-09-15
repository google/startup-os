# Reviewer

Reviewer is a multi-repo Code Review tool.
It can do some cool stuff:
- Well, multi-repo. Repos can be any combination of on-prem, GitHub, GitLab, Bitbucket, Google CSR etc.
- No server to maintain. Code is served locally from your machine, metadata from Firebase.
- Can view local changes in tool - no need to push.
- Comes with multi-repo cli tool.
- Supports multiple auth providers - GitHub, Google (this is just part of Firebase)

Here's a 2-min video of the tool:
https://www.youtube.com/watch?v=BKdCrtcmYsM

The grand idea is that having multi-repo code review, multi-repo cli and multi-repo CI, we can do cross-repo changes, repos can depend on head of other repos, so also cross-repo tests at head. These are the main advantages of a monorepo, while also staying multi-repo.

## Architecture
Reviewer has 3 components:
* [Web front-end](webapp), written in Angular.
* [Local server](local_server), to serve code and diffs to the web front-end from the local machine.
* [cli tool](aa) called aa (easy to type) to manage multiple repos locally.

![Reviewer architecture diagram!](https://image.ibb.co/dOMa9p/rsz_3gcfpfw4wuj.png)

## Reviewer instances and the global registry
There can be multiple Reviewer instances in the world, each covering a set of repos. To help in
their discovery, instances can be registered in a [global registry](global_registry.prototxt) file
in this repo.
As each Reviewer instance covers a set of repos, its configuration is stored in one of the repos,
in a root file called [reviewer_config.prototxt](../../reviewer_config.prototxt). This repo is
pointed at from the global registry.
Since Reviewer instances run a local server that holds a port, to try to avoid collisions, we also
define a default port for each Reviewer instance in the global registry.

## Current status:
The tool is WIP. The video is up-to-date as of Sep 6th 2018. If you want to use the alpha version please contact oferb@google.com.
If you want to help, these are the areas we need most help in:
* GitHub integration - syncing comments, syncing review status and GitHub integrations status.
* Multi-repo CI, to complete the multi-repo story. How to set up CI to test multiple repos and push to master on all of them if all tests pass.
