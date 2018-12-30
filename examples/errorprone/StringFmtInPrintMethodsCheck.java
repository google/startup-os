/*
 * Copyright 2018 The StartupOS Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// SOURCE:
// https://github.com/google/error-prone/blob/master/examples/plugin/bazel/java/com/google/errorprone/sample/MyCustomCheck.java

package com.google.startupos.examples.errorprone;

import static com.google.common.collect.Iterables.getLast;
import static com.google.errorprone.BugPattern.Category.JDK;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;

import com.google.auto.service.AutoService;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

/** Matches on string formatting inside print methods. */
@AutoService(BugChecker.class)
@BugPattern(
    name = "StringFmtInPrintMethodsCheck",
    category = JDK,
    summary = "String formatting inside print method",
    severity = ERROR,
    linkType = CUSTOM,
    link =
        "github.com/google/startup-os/tree/master/examples/errorprone#StringFmtInPrintMethodsCheck")
public class StringFmtInPrintMethodsCheck extends BugChecker
    implements MethodInvocationTreeMatcher {
  @SuppressWarnings({"checkstyle:AbbreviationAsWordInName", "checkstyle:MemberName"})
  private Matcher<ExpressionTree> PRINT_METHOD =
      instanceMethod().onDescendantOf(PrintStream.class.getName()).named("print");
  @SuppressWarnings({"checkstyle:AbbreviationAsWordInName", "checkstyle:MemberName"})
  private Matcher<ExpressionTree> STRING_FORMAT =
      staticMethod().onClass(String.class.getName()).named("format");

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    // check whether it's a method named "print" on PrintStream
    if (!PRINT_METHOD.matches(tree, state)) {
      return NO_MATCH;
    }
    // fuzzy description: searching for parent of `PrintStream`
    Symbol base =
        tree.getMethodSelect()
            .accept(
                new TreeScanner<Symbol, Void>() {
                  @Override
                  public Symbol visitIdentifier(IdentifierTree node, Void unused) {
                    return ASTHelpers.getSymbol(node);
                  }

                  @Override
                  public Symbol visitMemberSelect(MemberSelectTree node, Void unused) {
                    return super.visitMemberSelect(node, null);
                  }
                },
                null);
    // check whether the PrintStream is a static method of System
    // if it is, it's either System.out or System.err
    if (!Objects.equals(base, state.getSymtab().systemType.tsym)) {
      return NO_MATCH;
    }
    // get the argument: System.<out|err>.print(<arg>)
    ExpressionTree arg = Iterables.getOnlyElement(tree.getArguments());
    // check whether <arg> is a String.format() call
    if (!STRING_FORMAT.matches(arg, state)) {
      return NO_MATCH;
    }

    // figure out which of system PrintStream's it is: <out|err>
    // tree is System.<out|err>.print(String.format(<arg>))
    String printStream = tree.toString().replaceFirst("System.([^.]*)(.*)", "$1");

    // <arg> in String.format(<arg>) call
    List<? extends ExpressionTree> formatArgs = ((MethodInvocationTree) arg).getArguments();
    // suggest to replace it with System.<out|err>.printf(<arg>)
    return describeMatch(
        tree,
        SuggestedFix.builder()
            .replace(
                ((JCTree) tree).getStartPosition(),
                ((JCTree) formatArgs.get(0)).getStartPosition(),
                String.format("System.%s.printf(", printStream))
            .replace(
                state.getEndPosition((JCTree) getLast(formatArgs)),
                state.getEndPosition((JCTree) tree),
                ")")
            .build());
  }
}

