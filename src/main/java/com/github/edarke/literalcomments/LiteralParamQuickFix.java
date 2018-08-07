package com.github.edarke.literalcomments;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

class LiteralParamQuickFix implements LocalQuickFix, LiteralFix {

    private static final Logger LOG = Logger.getInstance(LiteralArgumentsInspection.class.getName());


    private final SmartPsiElementPointer<PsiExpression> paramLiteral;
    private final String paramName;

    LiteralParamQuickFix(SmartPsiElementPointer<PsiExpression> paramLiteral, String paramName) {
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
            addComment(project, this.paramName, paramLiteral.getElement());
        } catch (Exception e) {
            LOG.error(e);
        }
    }



    @NotNull
    public String getFamilyName() {
        return getName();
    }
}
