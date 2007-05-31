package org.drools.eclipse.flow.ruleflow.editor.action;
/*
 * Copyright 2005 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.ruleflow.editor.RuleFlowModelEditor;
import org.drools.ruleflow.core.RuleFlowProcessValidationError;
import org.drools.ruleflow.core.impl.RuleFlowProcessValidatorImpl;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Action for checking a RuleFlow.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class CheckRuleFlowAction extends ActionDelegate implements IEditorActionDelegate {

    private IEditorPart editor;
    
    public void run(IAction action) {
        execute();
    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        editor = targetEditor;
    }

    private void execute() {
        RuleFlowProcessValidationError[] errors = RuleFlowProcessValidatorImpl.getInstance().validateProcess(
            ((RuleFlowModelEditor) editor).getRuleFlowModel().getRuleFlowProcess());
        if (errors.length == 0) {
            MessageDialog.openInformation(editor.getSite().getShell(),
            "Check RuleFlow", "The RuleFlow model was checked successfully.");
        } else {
			StringBuffer error = new StringBuffer(errors[0].toString());
			error.append("\n");
            for (int i = 1; i < errors.length; i++) {
				error.append(" ");
				error.append(errors[i]);
				error.append("\n");
            }
            ErrorDialog.openError(editor.getSite().getShell(),
                "Check RuleFlow", "The RuleFlow model contains errors.", 
                new Status(
                    IStatus.ERROR,
                    DroolsEclipsePlugin.getDefault().getBundle().getSymbolicName(),
                    IStatus.ERROR,
                    error.toString(),
                    null)
                );
        }

    }
}
