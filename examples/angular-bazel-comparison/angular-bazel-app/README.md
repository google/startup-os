[![CircleCI](https://circleci.com/gh/alexeagle/angular-bazel-example.svg?style=svg)](https://circleci.com/gh/alexeagle/angular-bazel-example)

# Example of building an Angular app with Bazel

**This is experimental! There may be breaking changes.**

This is part of the ABC project. The overall goal is to make it possible to
develop Angular applications the same way we do at Google.
See http://g.co/ng/abc for an overview.

You can read the documentation in the wiki of this repository to understand how
this works.

Follow https://github.com/angular/angular/issues/19058 for updates.

## Installation

Install Bazel from the distribution, see [install] instructions.
On Mac, just `brew install bazel`.

Bazel will install a hermetic version of Node, npm, and Yarn when
you run the first build.

[install]: https://bazel.build/versions/master/docs/install.html

Also add `ibazel` to your `$PATH`:

```
yarn global add @bazel/ibazel
```

or

```
npm install -g @bazel/ibazel
```

## Setup

Before building the app, we install packages, just as with any npm-based development workflow.
However, to get the same version of the toolchain as co-workers and our continuous integration, let's have Bazel run the
package manager using a pinned version it manages. You should not even need
node, npm, or yarn installed on a machine where you develop with Bazel.

```bash
$ bazel run :install
```

## Development

Next we'll run the development server:

```bash
$ ibazel run src:devserver
```

> The `ibazel` command is a "watch mode"
> for Bazel, which means it will watch any files that are inputs to the devserver,
> and when they change it will ask Bazel to re-build them. The devserver stays
> running, and when the re-build is finished, it will trigger the LiveReload in
> the browser.

This command prints a URL on the terminal. Open that page to see the demo app
running. Now you can edit one of the source files (`src/lib/file.ts` is an easy
one to understand and see the effect). As soon as you save a change, the app
should refresh in the browser with the new content. Our intent is that this time
is less than two seconds, even for a large application.

Control-C twice to kill the devserver and also stop `ibazel`.

We can also run all the unit tests:

```bash
$ ibazel test ...
```

This will run all the tests. In this example, there is a test for the
`hello-world` component. Note that Bazel will only re-run the tests whose inputs
changed since the last run.

## Production

We can run the application in production mode, where the code has been bundled
and optimized. This can be slower than the development mode, because any change
requires re-optimizing the app. This example uses Rollup and Uglify, but other
bundlers can be integrated with Bazel.

```bash
$ ibazel run src:prodserver
```

We also use Protractor to run end-to-end tests. We don't have a protractor rule
yet, so we'll take the build results from Bazel and run the test outside of Bazel.

```bash
$ yarn e2e
```

## Coming soon

- Protractor bazel rule
- Code-splitting and lazy loading (planned for Q2/Q3 2018)
