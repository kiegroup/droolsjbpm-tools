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

import org.drools.eclipse.editors.DRLDocumentProvider;
import org.eclipse.jface.text.IDocument;

public class DSLtoDRLDocumentProvider extends DRLDocumentProvider {

    private DSLtoDRLRuleViewer drlViewer;
    private IDocument document;

    public DSLtoDRLDocumentProvider(DSLtoDRLRuleViewer drlViewer) {
        this.drlViewer = drlViewer;
    }

    public IDocument getDocument(Object element) {
        if (document == null) {
            IDocument superDocument = super.getDocument(element);
            document = new DSLtoDRLDocument(superDocument, drlViewer);
        }
        return document;
    }

    public boolean isModifiable(Object element) {
        return false;
    }

}
