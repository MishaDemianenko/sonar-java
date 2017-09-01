/*
 * SonarQube Java
 * Copyright (C) 2012-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.checks;

import org.sonar.check.Rule;
import org.sonar.java.checks.methods.AbstractMethodDetection;
import org.sonar.java.matcher.MethodMatcher;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.Arrays;
import java.util.List;

@Rule(key = "S3415")
public class AssertionArgumentOrderCheck extends AbstractMethodDetection {

  private static final String ORG_JUNIT_ASSERT = "org.junit.Assert";
  private static final Tree.Kind[] LITERAL_KINDS = {Tree.Kind.STRING_LITERAL, Tree.Kind.INT_LITERAL, Tree.Kind.LONG_LITERAL, Tree.Kind.CHAR_LITERAL,
    Tree.Kind.NULL_LITERAL, Tree.Kind.BOOLEAN_LITERAL, Tree.Kind.DOUBLE_LITERAL, Tree.Kind.FLOAT_LITERAL};

  @Override
  protected List<MethodMatcher> getMethodInvocationMatchers() {
    return Arrays.asList(MethodMatcher.create().typeDefinition(ORG_JUNIT_ASSERT).name("assertEquals").withAnyParameters(),
      MethodMatcher.create().typeDefinition(ORG_JUNIT_ASSERT).name("assertSame").withAnyParameters(),
      MethodMatcher.create().typeDefinition(ORG_JUNIT_ASSERT).name("assertNotSame").withAnyParameters());
  }

  @Override
  protected void onMethodInvocationFound(MethodInvocationTree mit) {
    ExpressionTree argToCheck = getActualArgument(mit);
    if (isConstant(argToCheck)) {
      context.reportIssue(this, argToCheck, "Make sure these 2 arguments are in the correct order: expected value, actual value.");
    }
  }

  private static ExpressionTree getActualArgument(MethodInvocationTree mit) {
    int arity = mit.arguments().size();
    ExpressionTree arg = mit.arguments().get(arity - 1);
    // Check for assert equals method with delta
    if (arity > 2 && (arity == 4 || mit.arguments().stream().allMatch(AssertionArgumentOrderCheck::isDoubleOrFloat))) {
      // last arg is actually delta, take the previous last to get the actual arg.
      arg = mit.arguments().get(arity - 2);
    }
    return arg;
  }

  private static boolean isDoubleOrFloat(ExpressionTree a) {
    return a.symbolType().isPrimitive(Type.Primitives.DOUBLE) || a.symbolType().isPrimitive(Type.Primitives.FLOAT);
  }

  private static boolean isConstant(Tree argToCheck) {
    return argToCheck.is(LITERAL_KINDS)
      || (argToCheck.is(Tree.Kind.IDENTIFIER) && isStaticFinal(((IdentifierTree) argToCheck).symbol()))
      || (argToCheck.is(Tree.Kind.MEMBER_SELECT) && isStaticFinal(((MemberSelectExpressionTree) argToCheck).identifier().symbol()));
  }

  private static boolean isStaticFinal(Symbol symbol) {
    return symbol.isStatic() && symbol.isFinal();
  }
}