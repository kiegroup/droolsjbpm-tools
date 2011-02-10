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

import org.drools.eclipse.editors.DRLRuleEditor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.part.FileEditorInput;

public class DSLRuleEditor extends DRLRuleEditor {

    protected DSLAdapter dslAdapter;

    public DSLAdapter getDSLAdapter() {
        if (dslAdapter == null) {
            try {
                String content = getSourceViewer().getDocument().get();
                dslAdapter = new DSLAdapter(content, ((FileEditorInput) getEditorInput()).getFile());
                if (!dslAdapter.isValid()) {
                    dslAdapter = null;
                }
            } catch (CoreException exc) {
                dslAdapter = null;
            }
        }
        return dslAdapter;
    }

    protected SourceViewerConfiguration createSourceViewerConfiguration() {
        return new DSLRuleSourceViewerConfig(this);
    }

    public void doSave(IProgressMonitor monitor) {
        super.doSave(monitor);
        // remove cached content
        dslAdapter = null;
    }
}
