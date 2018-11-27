# Keeping the code formatted

To keep code in `startup-os` consistently formatted,
we've developed a tool called `FormatterTool`.
It works for Java, C++, Python and BUILD files.


## Preliminary setup
As _most_ of our code, as well as `bazel` itself is written in Java, `bazel` needs
to know where to find JDK. Unfortunately, it seems that first-in-mind Google Cloud Build is configured to
*run* Java programs, not to _build_ them - this is why `JAVA_HOME` is pointing to **JRE**
which confuses `bazel`. To fix it, before running rest of the tutorial, run either of:
```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
```
to use system JDK or
```bash
unset JAVA_HOME
```
to use JDK embedded in bazel

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
It is intentionally misformatted, so we can show the tool at work.

## Commit the results
We use `git` for source control. First, you have to set your identity:
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
tools/fix_formatting.sh
```

## Look at the changes
Run
```bash
git diff
```
to see what was changed during formatting.
