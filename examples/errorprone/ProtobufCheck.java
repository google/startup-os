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

package com.google.startupos.examples.errorprone;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.predicates.TypePredicates;
import com.google.errorprone.util.ASTHelpers;
import com.google.protobuf.Message;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;

import static com.google.errorprone.BugPattern.Category.JDK;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;

/** Matches on .newBuilder().build() called on proto message class. */
@AutoService(BugChecker.class)
@BugPattern(
    name = "ProtobufCheck",
    category = JDK,
    summary = "Invocation of .newBuilder().build() on proto messages",
    severity = ERROR,
    linkType = CUSTOM,
    link = "github.com/google/startup-os/tree/master/examples/errorprone#ProtobufCheck")
public class ProtobufCheck extends BugChecker implements MethodInvocationTreeMatcher {

  private Matcher<ExpressionTree> NEW_BUILDER =
      staticMethod()
          .onClass(TypePredicates.isDescendantOf("com.google.protobuf.GeneratedMessageV3"))
          .named("newBuilder");

  private Matcher<ExpressionTree> BUILDER_BUILD =
      instanceMethod().onDescendantOf(Message.Builder.class.getName()).named("build");

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {

    // check whether it's a newBuilder() method of any protobuf-generated message class
    if (NEW_BUILDER.matches(tree, state)) {
      // find next method invocation in chain (on obj.method1().method2() it would return method2)
      ExpressionTree methodInvocation =
          ASTHelpers.findEnclosingNode(state.getPath(), MethodInvocationTree.class);
      // next invoked message is a build() method of any protobuf-generated message builder class
      if (BUILDER_BUILD.matches(methodInvocation, state)) {

        // select "newBuilder().build()" part of tree
        int start = state.getEndPosition(tree) - "newBuilder()".length();
        int end = state.getEndPosition(methodInvocation);

        // suggest to replace it with "getDefaultInstance()"
        return describeMatch(
            methodInvocation,
            SuggestedFix.builder().replace(start, end, "getDefaultInstance()").build());
      }
    }
    return NO_MATCH;
  }
}

