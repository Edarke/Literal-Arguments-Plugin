package com.github.edarke.literalcomments;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import java.util.Optional;

interface SettingAccessor {

  String DEFAULT_COMMENT_FORMAT = "/* %s= */";
  String COMMENT_FORMAT_KEY = "DEFAULT_COMMENT_FORMAT";

  default String getCommentFormat() {
    return PropertiesComponent.getInstance().getValue(COMMENT_FORMAT_KEY, DEFAULT_COMMENT_FORMAT);
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

  default void delelePostComment(PsiElement element, String comment) {
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
