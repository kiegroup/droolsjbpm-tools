/*
 * Copyright 2010 JBoss Inc
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

package org.drools.eclipse.dsl.editor;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.editors.AbstractRuleEditor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class DSLtoDRLRuleViewer extends AbstractRuleEditor {

    private DSLRuleEditor dslRuleEditor;

    public DSLtoDRLRuleViewer(DSLRuleEditor dslRuleEditor) {
        this.dslRuleEditor = dslRuleEditor;
    }

    protected IDocumentProvider createDocumentProvider() {
        return new DSLtoDRLDocumentProvider(this);
    }
    
    public String getDSLRuleContent() {
        return dslRuleEditor.getContent();
    }

    public void handleError(Throwable t) {
        DroolsEclipsePlugin.log(t);
        Throwable cause = t.getCause();
        if (cause == null) {
            cause = t;
        }
        String message = cause.getClass().getName() + ": " + cause.getMessage();
        if (message == null || message.length() == 0) {
            message = "Uncategorized Error!";
        }
        IStatus status = new Status(IStatus.ERROR, DroolsEclipsePlugin
                .getUniqueIdentifier(), -1, message, null);
        ErrorDialog.openError(getSite().getShell(),
                "DSL Rule Translation Error!", "DSL Rule Translation Error!",
                status);
    }

    public int getSelectedRange() {
        return getSourceViewer().getTopIndex();
    }

    public void setSelectedRange(int index) {
        getSourceViewer().setTopIndex(index);
    }

}
