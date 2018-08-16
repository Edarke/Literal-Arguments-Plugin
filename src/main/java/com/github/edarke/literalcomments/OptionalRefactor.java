package com.github.edarke.literalcomments;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OptionalRefactor extends PsiElementBaseIntentionAction implements LiteralFix {

  @NotNull
  public String getText() {
    return "Add inline comment for parameter";
  }

  @NotNull
  public String getFamilyName() {
    return getText();
  }

  @Override
  public boolean startInWriteAction() {return true;}

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, @Nullable PsiElement elementUnderCursor) {
    PsiExpressionList arguments = PsiTreeUtil.getParentOfType(elementUnderCursor, PsiExpressionList.class);
    if (arguments == null) {
      return false;
    }
    if (!(findChildOnWayToLeaf(arguments, elementUnderCursor) instanceof PsiExpression)) {
      return false;
    }

    PsiCallExpression methodCall = PsiTreeUtil.getParentOfType(arguments, PsiMethodCallExpression.class, PsiNewExpression.class);
    if (methodCall == null) {
        return false;
    }
    PsiMethod method = methodCall instanceof PsiNewExpression? ((PsiNewExpression) methodCall).resolveConstructor() : methodCall.resolveMethod();

    PsiParameter[] parameters = getParametersOfMethod(method).orElse(null);
    if (parameters == null){
      return false;
    }

    int index = findExpressionOnWayToLeaf(arguments, elementUnderCursor);
    return index < parameters.length && !isCommented(arguments.getExpressions()[index]);
  }

  private PsiElement findChildOnWayToLeaf(PsiElement list, PsiElement leaf) {
    for (int i = 0; i < list.getChildren().length; ++i) {
      PsiElement child = list.getChildren()[i];
      if (PsiTreeUtil.isAncestor(child, leaf, /* strict= */false)) {
        return child;
      }
    }
    throw new RuntimeException("Could not find argument expression");
  }

  private int findExpressionOnWayToLeaf(PsiExpressionList list, PsiElement leaf) {
    for (int i = 0; i < list.getExpressions().length; ++i) {
      PsiExpression child = list.getExpressions()[i];
      if (PsiTreeUtil.isAncestor(child, leaf, /* strict= */false)) {
        return i;
      }
    }
    throw new RuntimeException("Could not find argument expression");
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement elementUnderCursor) throws IncorrectOperationException {
    PsiExpressionList arguments = PsiTreeUtil.getParentOfType(elementUnderCursor, PsiExpressionList.class);
    if (arguments == null) {
      return;
    }
    PsiMethodCallExpression methodCall = PsiTreeUtil.getParentOfType(arguments,
        PsiMethodCallExpression.class);
    if (methodCall == null) {
      return;
    }

    PsiMethod method = methodCall.resolveMethod();
    PsiParameter[] parameters = getParametersOfMethod(method).orElse(null);
    if (parameters != null) {
      int index = findExpressionOnWayToLeaf(arguments, elementUnderCursor);
      PsiExpression argument = arguments.getExpressions()[index];
      if (index < parameters.length) {
        PsiParameter param = parameters[index];
        addComment(project, param.getName(), argument);
      }
    }
  }
}
