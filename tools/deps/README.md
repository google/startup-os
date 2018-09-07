# How Bazel package management works

## Package managers

To understand how bazel package management works, let's first go over package management in general.
Different languages have different package managers. Here's a few:
* Java: Maven
* JavaScript and Typescript: npm, Yarn
* Python: pip
* Go: dep (go get)
* Rust: Cargo
* Swift and Objective-C: CocoaPods

Package managers work with package repositories, such as www.npmjs.com for npm, [mvnrepository.com](https://mvnrepository.com) for Maven, and [pypi.org](https://pypi.org) for pip. They do 3 important things:
1. Find transitive dependencies - the tree of packages (and their versions) a given package depends on.
2. Resolve dependency conflicts, such as if two packages depend on different versions of the same package.
3. Download these dependencies and place them in an appropriate place on your machine.

Bazel uses language-specific package managers for step 1 and 2, and does step 3 by itself.


## Package-lock files

Package managers start with a list of packages the user has defined. After finding transitive dependencies for each package, and resolving conflicts (step 1 and 2 above), we end up with a (much larger) list of packages. To make sure we get the same packages every time, this list is saved in the repository. SHA hashes of the packages are automatically added to make sure the package doesn't change. We'll call this file a `package-lock` file since it locks all packages in place (along with their versions and SHAs).

Some examples of `package-lock` files:
* `package-lock.json` for `npm`
* `yarm-lock.json` for `Yarn`
* [`package-lock.bzl`](https://github.com/google/startup-os/blob/master/third_party/maven/package-lock.bzl) for `Maven` (when using [`bazel-deps`](https://github.com/google/startup-os/blob/161921a280429f7b5a03e6f432159ff167903dcc/WORKSPACE#L7-L11))


## Examples in this repo

### Java
We keep a [`dependencies.yaml`](https://github.com/google/startup-os/blob/master/dependencies.yaml) list of user-defined packages. Every time we modify this list, we need to run a command to update `package-lock.bzl` and BUILD files under `third_party/maven` (see issue [#238](https://github.com/google/startup-os/issues/238) on `mvncom` prefix). These commands do this update:
* For adding a new dependency to `dependencies.yaml`, use [`tools/deps/add_maven_deps.sh`](https://github.com/google/startup-os/tree/master/tools/deps/add_maven_dep.sh).
* After modifying or deleting dependencies in `dependencies.yaml`, use [`tools/deps/update_maven_deps.sh`](https://github.com/google/startup-os/tree/master/tools/deps/update_maven_dep.sh).

## Dependency whitelist

To make sure we don't depend on dependencies we don't want, we check dependencies in the following places, against a `whitelist.yaml` file:
* `WORKSPACE`, using [`WorkspaceDepsWhitelistTest.java`](https://github.com/google/startup-os/blob/master/tools/deps/WorkspaceDepsWhitelistTest.java)
* `dependencies.yaml`, using [`MavenDepsWhitelistTest.java`](https://github.com/google/startup-os/blob/master/tools/deps/MavenDepsWhitelistTest.java)
* `package-lock.bzl`, using [`PackageLockWhitelistTest.java`](https://github.com/google/startup-os/blob/master/tools/deps/PackageLockWhitelistTest.java)

Tests are run as part of CI and so if they fail, a commit cannot be merged to master.
