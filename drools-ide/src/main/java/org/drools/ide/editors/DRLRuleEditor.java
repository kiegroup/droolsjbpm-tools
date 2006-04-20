package org.drools.ide.editors;

import java.util.List;

import org.drools.ide.editors.outline.RuleContentOutlinePage;
import org.drools.ide.editors.scanners.RuleEditorMessages;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * Generic rule editor for drools.
 * @author Michael Neale
 */
public class DRLRuleEditor extends TextEditor {

    //used to provide additional content assistance/popups when DSLs are used.
    private DSLAdapter             dslAdapter;
    private List imports;

    private RuleContentOutlinePage ruleContentOutline = null;

    public DRLRuleEditor() {
        setSourceViewerConfiguration( new DRLSourceViewerConfig( this ) );
        setDocumentProvider( new DRLDocumentProvider() );
    }

    public void dispose() {
        super.dispose();
    }
    
    /** For user triggered content assistance */
    protected void createActions() {
        super.createActions();

        IAction a = new TextOperationAction( RuleEditorMessages.getResourceBundle(),
                                             "ContentAssistProposal.",
                                             this,
                                             ISourceViewer.CONTENTASSIST_PROPOSALS );
        a.setActionDefinitionId( ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS );
        setAction( "ContentAssistProposal",
                   a );

        a = new TextOperationAction( RuleEditorMessages.getResourceBundle(),
                                     "ContentAssistTip.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION ); //$NON-NLS-1$
        a.setActionDefinitionId( ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION );
        setAction( "ContentAssistTip",
                   a );

    }

    /** Return the DSL adapter if one is present */
    public DSLAdapter getDSLAdapter() {
        return dslAdapter;
    }

    /** Set the DSL adapter, used for content assistance */
    public void setDSLAdapter(DSLAdapter adapter) {
        dslAdapter = adapter;
    }
    
    public void setImports(List imports) {
    	this.imports = imports;
    }
    
    public List getImports() {
    	return imports;
    }

    public Object getAdapter(Class adapter) {
        if ( adapter.equals( IContentOutlinePage.class ) ) {
            return getContentOutline();
        }
        return super.getAdapter( adapter );
    }

    protected ContentOutlinePage getContentOutline() {
        if ( ruleContentOutline == null ) {
            ruleContentOutline = new RuleContentOutlinePage( this );
            ruleContentOutline.update();
        }
        return ruleContentOutline;
    }

    public void doSave(IProgressMonitor monitor) {
        super.doSave( monitor );
        if ( ruleContentOutline != null ) {
        	ruleContentOutline.update();
        }
        dslAdapter = null;
        imports = null;
    }

}
