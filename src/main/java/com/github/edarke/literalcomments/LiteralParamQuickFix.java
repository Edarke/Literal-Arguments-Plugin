package com.github.edarke.literalcomments;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.RefactoringQuickFix;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.refactoring.RefactoringActionHandler;
import org.jetbrains.annotations.NotNull;

class LiteralParamQuickFix implements LocalQuickFix, SettingAccessor {

    private static final Logger LOG = Logger.getInstance(LiteralArgumentsInspection.class.getName());


    private final SmartPsiElementPointer<PsiElement> paramLiteral;
    private final String paramName;

    LiteralParamQuickFix(SmartPsiElementPointer<PsiElement> paramLiteral, String paramName) {
        this.paramLiteral = paramLiteral;
        this.paramName = paramName;
    }

    @NotNull
    public String getName() {
        return "Add inline comment for parameter";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        try {
            final PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
            String commentContent = String.format(getCommentFormat(), paramName);
            PsiComment comment = factory.createCommentFromText(commentContent, /* psiElement= */null);
            paramLiteral.getElement().getParent().addBefore(comment, paramLiteral.getElement());
            CodeStyleManager.getInstance(project).reformat(descriptor.getPsiElement());
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    @NotNull
    public String getFamilyName() {
        return getName();
    }
}
