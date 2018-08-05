package com.github.edarke.literalcomments;

import com.google.common.collect.ImmutableSet;
import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.intellij.psi.JavaTokenType.COMMA;
import static com.intellij.psi.JavaTokenType.C_STYLE_COMMENT;
import static java.util.stream.Collectors.toList;

/**
 * This inspection detects when literals are passed to a method and suggests adding a comment to
 * make code more clear.
 *
 * @author Evan Darke
 */
public class LiteralArgumentsInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final Logger LOG = Logger.getInstance(LiteralArgumentsInspection.class.getName());

    /**
     * These classes are self-documenting and shouldn't need a comment.
     */
    private static final ImmutableSet<String> EXEMPT_TYPES = ImmutableSet.of(String.class.getName(),
            char.class.getName());

    private static String COMMENT_FORMAT = "/* %s= */";

    @NonNls
    private static final String DESCRIPTION_TEMPLATE = "Magic values should not be passed to " +
            "methods without a comment.";

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
        return new JavaElementVisitor() {

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                PsiMethod method = (PsiMethod) expression.getMethodExpression().resolve();
                PsiParameterList parameterList = null;
                try {
                    parameterList = method.getParameterList();
                } catch (NullPointerException npe) {
                    super.visitMethodCallExpression(expression);
                    return;
                }
                if (parameterList.getParametersCount() == 0) {
                    super.visitMethodCallExpression(expression);
                    return;
                }
                if (isBlackListed(method, parameterList)) {
                    super.visitMethodCallExpression(expression);
                    return;
                }

                int index = 0;
                boolean hasComment = false;
                try {
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
                                if (index < parameterList.getParametersCount()) {
                                    PsiParameter param = parameterList.getParameters()[index];
                                    SmartPsiElementPointer<PsiElement> smartParamLiteral =
                                            SmartPointerManager.getInstance(expression.getProject()).createSmartPsiElementPointer(expr);
                                    holder.registerProblem(expr, DESCRIPTION_TEMPLATE,
                                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                            new LiteralParamQuickFix(smartParamLiteral, param.getName()));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error(String.format("Text: %s; Method: %s; Param Count: %s; index: %d; " +
                                    "hasComment: %s", expression.getText(), method, parameterList.getParametersCount(),
                            index, hasComment), e);
                }

                super.visitMethodCallExpression(expression);
            }
        };
    }


    private final List<Pattern> BLACKLIST = ImmutableSet.of(
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
            "ImmutableSet\\.of\\(.*\\)",
            "ImmutableList\\.of\\(.*\\)",
            "ImmutableMultiset\\.of\\(.*\\)",
            "ImmutableSortedMultiset\\.of\\(.*\\)",
            "ImmutableMap\\.of\\(.*\\)",
            "ImmutableSortedSet\\.of\\(.*\\)",
            "Arrays\\.asList\\(.*\\)").stream().map(Pattern::compile).collect(toList());

    private boolean isBlackListed(PsiMethod method, PsiParameterList parameterList) {
        String paramPattern =
                Arrays.stream(parameterList.getParameters()).map(PsiNamedElement::getName).collect(Collectors.joining(", ", "(", ")"));

      String className = PsiTreeUtil.getParentOfType(method, PsiClass.class).getName();
      String signature = String.format("%s.%s%s", className, method.getName(), paramPattern);
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
    public boolean isEnabledByDefault() {
        return true;
    }

    private static class LiteralParamQuickFix implements LocalQuickFix {

        private final SmartPsiElementPointer<PsiElement> paramLiteral;
        private final String paramName;

        private LiteralParamQuickFix(SmartPsiElementPointer<PsiElement> paramLiteral,
                                     String paramName) {
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
                String commentContent = String.format(COMMENT_FORMAT, paramName);
                PsiComment comment = factory.createCommentFromText(commentContent, null);
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

    public JComponent createOptionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JTextField formatOptionField = new JTextField("/* %s= */");
        formatOptionField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            public void textChanged(DocumentEvent event) {
                COMMENT_FORMAT = formatOptionField.getText();
            }
        });

        panel.add(formatOptionField);
        return panel;
    }
}