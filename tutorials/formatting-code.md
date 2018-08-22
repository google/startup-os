# Keeping the code formatted

To keep code in `startup-os` consistently formatted,
we've developed a tool called `FormatterTool`.
It works for C++, Java, Python, BUILD 
and `dependencies.yaml` file.

## Ensuring Bazel has correct version
To make use of the tutorial, `bazel`'s version should be
equal or more than `0.16.1rc2`
(see the reasoning [here](https://github.com/bazelbuild/bazel/issues/5766))

To check the version, execute
```bash
bazel version
```

To upgrade, execute
```bash
wget https://releases.bazel.build/0.16.1/rc2/bazel_0.16.1rc2-linux-x86_64.deb &&
sudo dpkg -i bazel_0.16.1rc2-linux-x86_64.deb && 
rm bazel_0.16.1rc2-linux-x86_64.deb
```

## Listing available targets

To list all build targets available, execute 
```bash
bazel query //...
```
from the repo root

## Compiling a target

Once you've selected a target (i.e. `//tools/formatter`),
try building it
```bash
bazel build //tools/formatter
```

## Change some Java code
Try adding a new method to the target we've just compiled.
For example, add this snippet
```java
System.err.println(
	"Hello world!"
	);
```
to `main` method in 
<walkthrough-editor-open-file 
	filePath="startup-os/tools/formatter/FormatterTool.java" 
	text="FormatterTool.java">
</walkthrough-editor-open-file>.
It is intentionally misformatted, so we can show the tool in work.

## Commit the results
We use `git` for source control. Firstly, you have to set your identity:
```bash
git config --global user.email "you@example.com"
```

```bash
git config --global user.name "Your Name"
```

Then, commit all changed files
```bash
git commit --all --message "commit message"
```

## Ensuring code is formatted
Before submitting a pull request with your changes,
you have to ensure that code is properly formatted. 
Run 
```bash
./fix_formatting.sh
```

## Look at the changes
Run
```bash
git diff
```
to see what was changed during formatting.
