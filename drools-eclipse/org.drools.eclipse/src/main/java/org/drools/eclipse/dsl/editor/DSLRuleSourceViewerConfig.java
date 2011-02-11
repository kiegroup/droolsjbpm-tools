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

import org.drools.eclipse.dsl.editor.completion.DSLRuleCompletionProcessor;
import org.drools.eclipse.editors.DRLSourceViewerConfig;
import org.drools.eclipse.editors.completion.DefaultCompletionProcessor;
import org.drools.eclipse.editors.scanners.DRLPartionScanner;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Source viewer config wires up the syntax highlighting, partitioning
 * and content assistance.
 */
public class DSLRuleSourceViewerConfig extends DRLSourceViewerConfig {

    public DSLRuleSourceViewerConfig(DSLRuleEditor editor) {
        super(editor);
    }

    /**
     * Get the appropriate content assistance, for each partition.
     */
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        ContentAssistant assistant = new ContentAssistant();
        assistant.setContentAssistProcessor(
            new DefaultCompletionProcessor(getEditor()), IDocument.DEFAULT_CONTENT_TYPE);
        assistant.setContentAssistProcessor(
            new DSLRuleCompletionProcessor(getEditor()), DRLPartionScanner.RULE_PART_CONTENT);
        assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
        assistant.setAutoActivationDelay(0);
        return assistant;
    }
}
