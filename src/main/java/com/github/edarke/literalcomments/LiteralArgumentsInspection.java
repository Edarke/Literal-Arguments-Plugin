package com.github.edarke.literalcomments;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

/**
 * This inspection detects when literals are passed to a method and suggests adding a comment to
 * make code more clear.
 *
 * @author Evan Darke
 */
class LiteralArgumentsInspection extends AbstractBaseJavaLocalInspectionTool implements LiteralFix {

    @NotNull
    public String getDisplayName() {
        return "Literal Argument";
    }

    @NotNull
    public String getGroupDisplayName() {
        return GroupNames.CONFUSING_GROUP_NAME;
    }

    @NotNull
    public String getShortName() {
        return "LiteralArguments";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new LiteralArgumentElementVisitor(holder);
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    public JComponent createOptionsPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        final JTextField formatOptionField = new JTextField(getCommentFormat(), 20);
        formatOptionField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            public void textChanged(DocumentEvent event) {
                setCommentFormat(formatOptionField.getText());
            }
        });
        panel.add(formatOptionField);
        return panel;
    }

}