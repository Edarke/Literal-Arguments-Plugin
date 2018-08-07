package com.github.edarke.literalcomments;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;

import java.util.Optional;

interface LiteralFix {

  String DEFAULT_COMMENT_FORMAT = "/* %s= */";
  String COMMENT_FORMAT_KEY = "DEFAULT_COMMENT_FORMAT";


  default boolean isCommented(PsiExpression argument) {
    PsiElement sibling = argument;
    while ((sibling = sibling.getPrevSibling()) != null && !(sibling instanceof PsiJavaToken)) {
      if (sibling instanceof PsiComment){
        return true;
      }
    }
    return false;
  }

  default String getCommentFormat() {
    return PropertiesComponent.getInstance().getValue(COMMENT_FORMAT_KEY, DEFAULT_COMMENT_FORMAT);
  }

  default void addComment(Project project, String argumentName, PsiExpression literalArgument) {
    PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
    String commentContent = String.format(getCommentFormat(), argumentName);
    PsiComment comment = factory.createCommentFromText(commentContent, null);
    literalArgument.getParent().addBefore(comment, literalArgument);
    deletePostComment(literalArgument, commentContent);
    CodeStyleManager.getInstance(project).reformat(literalArgument.getParent());
  }

  default void setCommentFormat(String format){
    format = format.trim();
    String testFormat = String.format(format, "test_param"); // assert valid
    if (testFormat.startsWith("/*") && testFormat.endsWith("*/")){
      PropertiesComponent.getInstance().setValue(COMMENT_FORMAT_KEY, format);
    }
  }

  default Optional<PsiParameter[]> getParametersOfMethod(PsiMethod method) {
    if (method == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(method.getParameterList().getParameters());
    } catch (NullPointerException npe) {
      return Optional.empty();
    }
  }

  default void deletePostComment(PsiElement element, String comment) {
    comment = comment.replaceAll("[ =]", "");

    PsiElement sibling = element;
    while ((sibling = sibling.getNextSibling()) != null && !(sibling instanceof PsiJavaToken)) {
      if (sibling instanceof PsiComment) {
        PsiComment existingComment = (PsiComment) sibling;
        if (existingComment.getText().replaceAll("[ =]", "").equalsIgnoreCase(comment)) {
          existingComment.delete();
          return;
        }
      }
    }
  }
}
