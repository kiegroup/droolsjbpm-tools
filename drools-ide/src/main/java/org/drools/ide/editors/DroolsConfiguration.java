package org.drools.ide.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

/**
 * The Drools configuration.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DroolsConfiguration extends SourceViewerConfiguration {
	private DroolsDoubleClickStrategy doubleClickStrategy;
	private DroolsTagScanner tagScanner;
	private DroolsScanner scanner;
	private ColorManager colorManager;

	public DroolsConfiguration(ColorManager colorManager) {
		this.colorManager = colorManager;
	}
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			DroolsPartitionScanner.COMMENT,
			DroolsPartitionScanner.TAG };
	}
	public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourceViewer,
		String contentType) {
		if (doubleClickStrategy == null)
			doubleClickStrategy = new DroolsDoubleClickStrategy();
		return doubleClickStrategy;
	}

	protected DroolsScanner getXMLScanner() {
		if (scanner == null) {
			scanner = new DroolsScanner(colorManager);
			scanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(IDroolsColorConstants.DEFAULT))));
		}
		return scanner;
	}
	protected DroolsTagScanner getXMLTagScanner() {
		if (tagScanner == null) {
			tagScanner = new DroolsTagScanner(colorManager);
			tagScanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(IDroolsColorConstants.TAG))));
		}
		return tagScanner;
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr =
			new DefaultDamagerRepairer(getXMLTagScanner());
		reconciler.setDamager(dr, DroolsPartitionScanner.TAG);
		reconciler.setRepairer(dr, DroolsPartitionScanner.TAG);

		dr = new DefaultDamagerRepairer(getXMLScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		NonRuleBasedDamagerRepairer ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(
					colorManager.getColor(IDroolsColorConstants.COMMENT)));
		reconciler.setDamager(ndr, DroolsPartitionScanner.COMMENT);
		reconciler.setRepairer(ndr, DroolsPartitionScanner.COMMENT);

		return reconciler;
	}

}