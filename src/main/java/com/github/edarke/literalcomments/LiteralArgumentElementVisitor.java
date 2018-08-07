// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.github.edarke.literalcomments;

import com.google.common.collect.ImmutableSet;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NonNls;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.intellij.psi.JavaTokenType.COMMA;
import static com.intellij.psi.JavaTokenType.C_STYLE_COMMENT;
import static java.util.stream.Collectors.toList;

class LiteralArgumentElementVisitor extends JavaElementVisitor implements SettingAccessor {

  private static final Logger LOG = Logger.getInstance(LiteralArgumentsInspection.class.getName());

  @NonNls
  private static final String DESCRIPTION_TEMPLATE = "Magic values should not be passed to " +
      "methods without a comment.";

  /**
   * These classes are self-documenting and shouldn't need a comment.
   */
  private static final ImmutableSet<String> EXEMPT_TYPES = ImmutableSet.of(String.class.getName(),
      char.class.getName());

  private final List<Pattern> BLACKLIST = ImmutableSet.of(
      "SimpleMoney\\..*",
      "Iterables\\..*",
      ".*\\([^,]*\\)",
      "Assert\\.\\(.\\)*",
      ".*\\(expected, actual\\)",
      ".*\\(expected\\)",
      ".*\\(index\\)",
      ".*\\(begin[^,]*\\)",
      ".*\\(begin[^,]*, end[^,]*\\)",
      ".*\\(start[^,]*, end[^,]*\\)",
      ".*\\(first[^,]*, last[^,]*\\)",
      ".*\\(first[^,]*, second[^,]*\\)",
      ".*\\(from[^,]*, to[^,]*\\)",
      ".*\\(min[^,]*, max[^,]*\\)",
      ".*\\(key, value\\)",
      ".*\\.Optional\\.\\(.*\\)",
      ".*\\(format, arg[^,]*\\)",
      ".*\\(message\\)",
      ".*\\(message, error\\)",
      ".*Exception",
      ".*\\.set.*\\(*\\)",
      ".*\\.add\\([^,]*\\)",
      ".*set\\([^,]*, [^,]*\\)",
      ".*get\\([^,]*\\)",
      ".*create\\([^,]*\\)",
      ".*getProperty\\([^,]*\\)",
      ".*setProperty\\([^,]*, [^,]*\\)",
      ".*\\.valueOf\\([^,]*\\)",
      ".*\\.of\\([^,]*\\)",
      ".*print\\([^,]*\\)",
      ".*println\\([^,]*\\)",
      ".*append\\([^,]*\\)",
      ".*charAt\\([^,]*\\)",
      ".*indexOf\\([^,]*\\)",
      ".*startsWith\\([^,]*\\)",
      ".*endsWith\\([^,]*\\)",
      ".*equals\\([^,]*\\)",
      ".*equal\\([^,]*\\)",
      ".*compareTo\\([^,]*\\)",
      ".*compare\\([^,]*, [^,]*\\)",
      "(Strict)?Math\\..*",
      ".*\\.singleton\\(.*\\)",
      ".*\\.singletonList\\(.*\\)",
      "Optional\\..*",
      "ImmutableSet\\.of\\(.*\\)",
      "ImmutableList\\.of\\(.*\\)",
      "ImmutableMultiset\\.of\\(.*\\)",
      "ImmutableSortedMultiset\\.of\\(.*\\)",
      "ImmutableMap\\.of\\(.*\\)",
      "ImmutableSortedSet\\.of\\(.*\\)",
      "Arrays\\.asList\\(.*\\)").stream().map(Pattern::compile).collect(toList());

  private final ProblemsHolder holder;

  LiteralArgumentElementVisitor(ProblemsHolder holder) {
    this.holder = holder;
  }

  private boolean isBlackListed(PsiMethod method, PsiParameter[] parameterList) {
    if (parameterList.length < 2) {
      return true;
    }

    String paramPattern =
        Arrays.stream(parameterList).map(PsiNamedElement::getName)
            .collect(Collectors.joining(", ", "(", ")"));

    PsiClass psiClass = PsiTreeUtil.getParentOfType(method, PsiClass.class, false);
    if (psiClass == null) {
      return true;
    }
    String signature = String.format("%s.%s%s", psiClass.getName(), method.getName(), paramPattern);
    LOG.info("Method signature: " + signature);
    return BLACKLIST.stream().anyMatch(p -> p.matcher(signature).matches());
  }

  private boolean isLiteral(PsiExpression expr) {
    if (expr instanceof PsiLiteralExpression) {
      PsiType type = expr.getType();
      return type == null || EXEMPT_TYPES.stream().noneMatch(type::equalsToText);
    } else if (expr instanceof PsiMethodCallExpression) {
      // Optional.empty() and Optional.absent() are considered literals
      return expr.getText().matches(
          "((java\\.util\\.)?Optional\\.(<[a-zA-Z0-9]*>)?empty\\(\\))|((com\\.google\\.common\\.base\\.)?Optional\\.(<[a-zA-Z0-9]*>)?absent\\(\\))");
    } else if (expr instanceof PsiTypeCastExpression) {
      PsiTypeCastExpression castExpr = (PsiTypeCastExpression) expr;
      return isLiteral(castExpr.getOperand());
    }

    return false;
  }

  @Override
  public void visitMethodCallExpression(PsiMethodCallExpression expression) {
    PsiMethod method = (PsiMethod) expression.getMethodExpression().resolve();
    PsiParameter[] parameters = getParametersOfMethod(method).orElse(null);
    int index = 0;
    boolean hasComment = false;

    try {
      if (parameters == null || isBlackListed(method, parameters)) {
        super.visitMethodCallExpression(expression);
        return;
      }

      PsiExpressionList expressions = expression.getArgumentList();
      for (PsiElement expr : expressions.getChildren()) {
        if (expr instanceof PsiJavaToken) {
          if (COMMA.toString().equals(((PsiJavaToken) expr).getTokenType().toString())) {
            ++index;
            hasComment = false;
          }
        } else if (expr instanceof PsiComment) {
          if (C_STYLE_COMMENT.toString().equals(((PsiComment) expr).getTokenType().toString())) {
            hasComment = true;
          }
        } else if (expr instanceof PsiExpression && isLiteral((PsiExpression) expr)) {
          if (!hasComment) {
            // Varargs have more commas than parameters
            if (index < parameters.length) {
              PsiParameter param = parameters[index];
              if (!param.isVarArgs()) {
                SmartPsiElementPointer<PsiElement> smartParamLiteral = SmartPointerManager
                    .getInstance(expression.getProject()).createSmartPsiElementPointer(expr);
                holder
                    .registerProblem(expr, DESCRIPTION_TEMPLATE, ProblemHighlightType.WEAK_WARNING,
                        new LiteralParamQuickFix(smartParamLiteral, param.getName()));
              }
            }
          }
        }
      }
    } catch (Exception e) {
      LOG.error(String.format("Text: %s; Method: %s; Param Count: %s; index: %d; " +
              "hasComment: %s", expression.getText(), method,
          parameters == null ? null : parameters.length,
          index, hasComment), e);
    }

    super.visitMethodCallExpression(expression);
  }
}
