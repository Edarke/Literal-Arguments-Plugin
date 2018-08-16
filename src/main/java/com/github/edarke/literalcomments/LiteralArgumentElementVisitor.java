package com.github.edarke.literalcomments;

import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableSet;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiCallExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeCastExpression;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NonNls;

class LiteralArgumentElementVisitor extends JavaElementVisitor implements LiteralFix {

  private static final Logger LOG = Logger.getInstance(LiteralArgumentsInspection.class.getName());

  @NonNls
  private static final String DESCRIPTION_TEMPLATE = "Magic values should not be passed to " +
      "methods without a comment.";

  /**
   * These classes are self-documenting and shouldn't need a comment.
   */
  private static final ImmutableSet<String> EXEMPT_TYPES =
      ImmutableSet.of(String.class.getName(), char.class.getName());

  private static final List<Pattern> BLACKLIST = ImmutableSet.of(
      ".*\\([^,]*\\)", // Single Arg Functions
      "SimpleMoney\\..*",
      "Iterables\\..*",
      "Assert\\.\\(.\\)*",
      ".*\\(expected, actual\\)",
      ".*\\(begin[^,]*, end[^,]*\\)",
      ".*\\(start[^,]*, end[^,]*\\)",
      ".*\\(first[^,]*, last[^,]*\\)",
      ".*\\(first[^,]*, second[^,]*\\)",
      ".*\\(from[^,]*, to[^,]*\\)",
      ".*\\(min[^,]*, max[^,]*\\)",
      ".*\\(key, value\\)",
      ".*\\(format, arg[^,]*\\)",
      ".*\\(message, error\\)",
      ".*set\\([^,]*, [^,]*\\)",
      ".*setProperty\\([^,]*, [^,]*\\)",
      ".*\\.valueOf\\([^,]*\\)",
      ".*\\.of\\([^,]*\\)",
      ".*compare\\([^,]*, [^,]*\\)",
      "(Strict)?Math\\..*",
      "Optional\\..*",
      "Immutable.*\\.of\\(.*\\)",
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
        Arrays.stream(parameterList).map(PsiNamedElement::getName).collect(Collectors.joining(", "
            , "(", ")"));

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
      return expr.getText().matches("((java\\.util\\.)?Optional\\.(<[a-zA-Z0-9]*>)?empty\\(\\))|(" +
          "(com\\.google\\.common\\.base\\.)?Optional\\.(<[a-zA-Z0-9]*>)?absent\\(\\))");
    } else if (expr instanceof PsiTypeCastExpression) {
      PsiTypeCastExpression castExpr = (PsiTypeCastExpression) expr;
      return isLiteral(castExpr.getOperand());
    }

    return false;
  }

  private void getQuickFixes(PsiMethod method, PsiCallExpression expression) {
    PsiParameter[] parameters = getParametersOfMethod(method).orElse(new PsiParameter[0]);
    int i = 0;

    try {
      if (isBlackListed(method, parameters)) {
        super.visitCallExpression(expression);
        return;
      }

      if (expression.getArgumentList() == null) {
        return;
      }

      PsiExpressionList expressions = expression.getArgumentList();
      for (i = 0; i < expressions.getExpressions().length && i < parameters.length; ++i) {
        PsiParameter paramDecl = parameters[i];
        PsiExpression paramExp = expressions.getExpressions()[i];

        if (!paramDecl.isVarArgs() && !isCommented(paramExp) && isLiteral(paramExp)) {
          SmartPsiElementPointer<PsiExpression> smartParamLiteral =
              SmartPointerManager.getInstance(expression.getProject())
                  .createSmartPsiElementPointer(paramExp);
          holder.registerProblem(paramExp, DESCRIPTION_TEMPLATE,
              ProblemHighlightType.WEAK_WARNING, new LiteralParamQuickFix(smartParamLiteral,
                  paramDecl.getName()));
        }
      }
    } catch (Exception e) {
      LOG.error(String.format("Text: %s; Method: %s; Param Count: %s; index: %d",
          expression.getText(), method, parameters.length, i), e);
    }
  }

  @Override
  public void visitNewExpression(PsiNewExpression expression) {
    PsiMethod method = expression.resolveConstructor();
    getQuickFixes(method, expression);
  }



  @Override
  public void visitMethodCallExpression(PsiMethodCallExpression expression) {
    PsiMethod method = (PsiMethod) expression.getMethodExpression().resolve();
    getQuickFixes(method, expression);
  }
}
