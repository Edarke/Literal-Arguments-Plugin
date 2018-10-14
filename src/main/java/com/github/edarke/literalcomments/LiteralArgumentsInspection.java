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

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.ui.DocumentAdapter;
import java.awt.FlowLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import org.jetbrains.annotations.NotNull;

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
    final JTextField formatOptionField = new JTextField(getCommentFormat(), /* columns= */20);
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