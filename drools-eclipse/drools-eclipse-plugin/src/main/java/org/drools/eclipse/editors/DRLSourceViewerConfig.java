package org.drools.eclipse.editors;

import org.drools.eclipse.editors.completion.DefaultCompletionProcessor;
import org.drools.eclipse.editors.completion.RuleCompletionProcessor;
import org.drools.eclipse.editors.scanners.DRLPartionScanner;
import org.drools.eclipse.editors.scanners.DRLScanner;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

/**
 * Source viewer config wires up the syntax highlighting, partitioning
 * and content assistance.
 * 
 * @author Michael Neale
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class DRLSourceViewerConfig extends SourceViewerConfiguration {

	private DRLScanner scanner;

	private AbstractRuleEditor editor;

	public DRLSourceViewerConfig(AbstractRuleEditor editor) {
		this.editor = editor;
	}
	
	protected AbstractRuleEditor getEditor() {
		return editor;
	}

	protected DRLScanner getScanner() {
		if (scanner == null) {
			scanner = new DRLScanner();
		}
		return scanner;
	}

	/**
	 * Define reconciler - this has to be done for each partition.
	 * Currently there are 3 partitions, Inside rule, outside rule and inside comment.
	 */
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		//bucket partition... (everything else outside a rule)
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		//inside a rule partition
		dr = new DefaultDamagerRepairer(getScanner());
		reconciler.setDamager(dr, DRLPartionScanner.RULE_PART_CONTENT);
		reconciler.setRepairer(dr, DRLPartionScanner.RULE_PART_CONTENT);

		//finally, inside a multi line comment.
		dr = new DefaultDamagerRepairer(new SingleTokenScanner(
				new TextAttribute(ColorManager.getInstance().getColor(
						ColorManager.SINGLE_LINE_COMMENT))));
		reconciler.setDamager(dr, DRLPartionScanner.RULE_COMMENT);
		reconciler.setRepairer(dr, DRLPartionScanner.RULE_COMMENT);

		return reconciler;
	}

	/**
	 * Single token scanner, used for scanning for multiline comments mainly.
	 */
	static class SingleTokenScanner extends BufferedRuleBasedScanner {
		public SingleTokenScanner(TextAttribute attribute) {
			setDefaultReturnToken(new Token(attribute));
		}
	}

	/**
	 * Get the appropriate content assistance, for each partition.
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		//setup the content assistance, which is
		//sensitive to the partition that it is in.
		assistant.setContentAssistProcessor(
			new DefaultCompletionProcessor(editor), IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(
			new RuleCompletionProcessor(editor), DRLPartionScanner.RULE_PART_CONTENT);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		return assistant;
	}

	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return DRLPartionScanner.LEGAL_CONTENT_TYPES;
	}

	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		MonoReconciler reconciler = null;
		if (sourceViewer != null) {
			reconciler = new MonoReconciler(
				new DRLReconcilingStrategy(sourceViewer, editor), false);
			reconciler.setDelay(500);
			reconciler.setProgressMonitor(new NullProgressMonitor());
		}
		return reconciler;
	}

	public IAnnotationHover getOverviewRulerAnnotationHover(ISourceViewer sourceViewer) {
		return new DefaultAnnotationHover();
	}

	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new DefaultAnnotationHover();
	}
}