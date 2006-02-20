package org.drools.ide.editors;

import org.drools.ide.editors.scanners.RuleEditorMessages;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextEditor;

import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;


/**
 * Generic ruleset editor for drools.
 * @author Michael Neale
 */
public class DRLRuleSetEditor extends TextEditor {
    
	public DRLRuleSetEditor()
	{
		super();
		setSourceViewerConfiguration(new DRLSourceViewerConfig());
        setDocumentProvider(new DRLDocumentProvider());
	}

    public void dispose() {
        super.dispose();
    }
    
    /** For user triggered content assistance */
    protected void createActions() {
        super.createActions();
        
        IAction a= new TextOperationAction(RuleEditorMessages.getResourceBundle(), "ContentAssistProposal.", this, ISourceViewer.CONTENTASSIST_PROPOSALS); 
        a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        setAction("ContentAssistProposal", a); 
        
        a= new TextOperationAction(RuleEditorMessages.getResourceBundle(), "ContentAssistTip.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION);  //$NON-NLS-1$
        a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
        setAction("ContentAssistTip", a); 
        
    }    
    
    
}

