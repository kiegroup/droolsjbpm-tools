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

import org.drools.eclipse.editors.completion.DefaultCompletionProcessor;
import org.drools.eclipse.editors.completion.RuleCompletionProcessor;
import org.drools.eclipse.editors.scanners.DRLPartionScanner;
import org.drools.eclipse.editors.scanners.DRLScanner;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.MultipleHyperlinkPresenter;
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
import org.eclipse.swt.graphics.RGB;

/**
 * Source viewer config wires up the syntax highlighting, partitioning
 * and content assistance.
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
    
    @Override
    public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
      IHyperlinkDetector[] detectors = super.getHyperlinkDetectors(sourceViewer);
      if(detectors==null) {
        detectors = new IHyperlinkDetector[0];
      }

      IHyperlinkDetector[] drlDetectors = new IHyperlinkDetector[detectors.length + 1];
      DRLHyperlinkDetector detector =  new DRLHyperlinkDetector(editor);
      drlDetectors[0] = detector;
      System.arraycopy(detectors, 0, drlDetectors, 1, detectors.length);
      
      return drlDetectors;
    }

	public IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer) {
		return new MultipleHyperlinkPresenter(new RGB(0, 0, 255));
	}
}
