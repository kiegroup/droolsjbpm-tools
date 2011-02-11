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

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextStore;

/**
 * A document that transforms the input of the original document
 * to something else.  Changing something in this document will
 * NOT change the original document (as the transformation is only
 * defined in one way). All changes will also be overridden as soon
 * as the original document changes.
 */
public abstract class TransformedDocument extends AbstractDocument {

    private IDocument parentDocument;
    private boolean changed = true;

    public TransformedDocument(IDocument parentDocument) {
        this.parentDocument = parentDocument;
        parentDocument.addDocumentListener(new IDocumentListener() {
            public void documentAboutToBeChanged(DocumentEvent event) {
                // Do nothing
            }
            public void documentChanged(DocumentEvent event) {
                changed = true;
            }
        });
        setTextStore(new StringTextStore());
        setLineTracker(new DefaultLineTracker());
        completeInitialization();
    }

    /**
     * Always check that the store is up-to-date.
     * All read operations access the store so this method makes sure
     * that the document is updated whenever necessary.
     */
    protected ITextStore getStore() {
        if (changed) {
            update();
        }
        return super.getStore();
    }

    private void update() {
        String translation = transformInput(parentDocument.get());
        super.getStore().set(translation);
        getTracker().set(translation);
        changed = false;
    }

    /**
     * Transforms the original content of the document.
     */
    protected abstract String transformInput(String content);

    /**
     * Default text store.
     */
    private static class StringTextStore implements ITextStore {

        private String fContent;

        public StringTextStore() {
        }

        public char get(int offset) {
            return fContent.charAt(offset);
        }

        public String get(int offset, int length) {
            return fContent.substring(offset, offset + length);
        }

        public int getLength() {
            return fContent.length();
        }

        public void replace(int offset, int length, String text) {
        }

        public void set(String text) {
            this.fContent = text;
        }
    }

}
