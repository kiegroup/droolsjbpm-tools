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
 * 
 * @author Michael Neale
 *
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