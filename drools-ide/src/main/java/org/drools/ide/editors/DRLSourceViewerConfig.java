package org.drools.ide.editors;

import org.drools.ide.editors.completion.DefaultCompletionProcessor;
import org.drools.ide.editors.completion.RuleCompletionProcessor;
import org.drools.ide.editors.scanners.DRLPartionScanner;
import org.drools.ide.editors.scanners.DRLScanner;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.Color;


/**
 * Source viewer config wires up the syntax highlighting, partitioning
 * and content assistance.
 * 
 * @author Michael Neale
 *
 */
public class DRLSourceViewerConfig extends SourceViewerConfiguration {
	private DRLScanner scanner;
	private static Color DEFAULT_COLOR = ColorManager.getInstance().getColor(ColorManager.DEFAULT);
		
	public DRLSourceViewerConfig() {
	}

	protected DRLScanner getScanner() {
		if (scanner == null) {
			scanner = new DRLScanner();
			scanner.setDefaultReturnToken(
				new Token(new TextAttribute(DEFAULT_COLOR)));
		}
		return scanner;
	}

	/**
	 * Define reconciler - this has to be done for each partition.
	 */
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        
        
		PresentationReconciler reconciler = new PresentationReconciler();
        
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
        

        dr = new DefaultDamagerRepairer(getScanner());
        reconciler.setDamager(dr, DRLPartionScanner.RULE_PART_CONTENT);
        reconciler.setRepairer(dr, DRLPartionScanner.RULE_PART_CONTENT);        
        
        return reconciler;
	}
	
    /**
     * Get the appropriate content assistance, for each partition.
     */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

		ContentAssistant assistant = new ContentAssistant();
        
        //setup the content assistance, which is
        //sensitive to the partition that it is in.
        
		assistant.setContentAssistProcessor(new DefaultCompletionProcessor(),
		                                    IDocument.DEFAULT_CONTENT_TYPE);
        
        
        assistant.setContentAssistProcessor(new RuleCompletionProcessor(),
                                            DRLPartionScanner.RULE_PART_CONTENT);
        

                                                                                    

//		assistant.enableAutoActivation(false);
//		assistant.setAutoActivationDelay(500);
		assistant.setProposalPopupOrientation(
			IContentAssistant.PROPOSAL_OVERLAY);
		return assistant;
	}

    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return DRLPartionScanner.LEGAL_CONTENT_TYPES;
    }

    
    
}