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

package org.drools.eclipse.editors;

import org.drools.eclipse.editors.scanners.DRLPartionScanner;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;


/**
 * Simple document provider.
 */
public class DRLDocumentProvider extends TextFileDocumentProvider {

    public IDocument getDocument(Object element) {
        IDocument document = getParentDocument(element);
        if (document != null) {
            IDocumentPartitioner partitioner =
                new FastPartitioner(
                    new DRLPartionScanner(),
                    DRLPartionScanner.LEGAL_CONTENT_TYPES);
            partitioner.connect(document);
            document.setDocumentPartitioner(partitioner);
        }
        return document;
    }
    
    protected IAnnotationModel createAnnotationModel(IFile file) {
        return new DRLAnnotationModel(file);
    }
    
    protected IDocument getParentDocument(Object element) {
        return super.getDocument(element);
    }
    
}
