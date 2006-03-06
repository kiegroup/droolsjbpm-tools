package org.drools.ide.editors.outline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.ide.editors.DRLRuleEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * Simple outline view of a DRL file. At present this is not wired in with the Parser, so it is fault
 * tolerant of incorrect syntax. 
 * Should provide navigation assistance in large rule files.
 * 
 * @author "Jeff Brown" <brown_j@ociweb.com>
 */
public class RuleContentOutlinePage extends ContentOutlinePage {

    //the editor that this outline view is linked to.
    private DRLRuleEditor       editor;

    //the "root" node
    private final RuleFileTreeNode ruleFileTreeNode    = new RuleFileTreeNode();

    ///////////////////////////////////
    // Patterns that the parser uses
    ///////////////////////////////////
    private static final Pattern   rulePattern         = Pattern.compile( "rule\\s+\"?([^\"]+)\"?.*",
                                                                          Pattern.DOTALL );

    private static final Pattern   packagePattern      = Pattern.compile( "package\\s+([^\"]+)",
                                                                          Pattern.DOTALL );

    private static final Pattern   functionNamePattern = Pattern.compile( "function\\s+([^\\s\\(]+).*",
                                                                          Pattern.DOTALL );

    public RuleContentOutlinePage(DRLRuleEditor editor) {
        super();
        this.editor = editor;
    }

    public void createControl(Composite parent) {
        super.createControl( parent );
        TreeViewer viewer = getTreeViewer();
        viewer.setContentProvider( new WorkbenchContentProvider() );
        viewer.setLabelProvider( new WorkbenchLabelProvider() );

        viewer.setInput( ruleFileTreeNode );
        update();

        //add the listener for navigation of the rule document.
        super.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                Object selectionObj = event.getSelection();
                if ( selectionObj != null && selectionObj instanceof StructuredSelection ) {
                    StructuredSelection sel = (StructuredSelection) selectionObj;
                    OutlineNode node = (OutlineNode) sel.getFirstElement();
                    if ( node != null ) {
                        editor.selectAndReveal( node.getOffset(),
                                                node.getLength() );
                    }
                }
            }
        } );
    }

    /**
     * Updates the outline page.
     */
    public void update() {
        TreeViewer viewer = getTreeViewer();

        if ( viewer != null ) {
            Control control = viewer.getControl();
            if ( control != null && !control.isDisposed() ) {
                PackageTreeNode packageTreeNode = createPackageTreeNode();
                ruleFileTreeNode.setPackageTreeNode( packageTreeNode );
                viewer.refresh();
                control.setRedraw( false );
                viewer.expandAll();
                control.setRedraw( true );
            }
        }
    }

    /**
     * 
     * @return a PackageTreeNode representing the current state of the 
     * document, populated with all of the package's child elements
     */
    private PackageTreeNode createPackageTreeNode() {
        PackageTreeNode packageTreeNode = new PackageTreeNode();
        populatePackageTreeNode( packageTreeNode );
        return packageTreeNode;
    }

    /**
     * populates the PackageTreeNode with all of its child elements
     * 
     * @param packageTreeNode the node to populate
     */
    private void populatePackageTreeNode(PackageTreeNode packageTreeNode) {
        String ruleFileContents = getRuleFileContents();
        StringReader stringReader = new StringReader( ruleFileContents );
        BufferedReader bufferedReader = new BufferedReader( stringReader );
        try {
            int offset = 0;
            String st;
            while ( (st = bufferedReader.readLine()) != null ) {

                Matcher matcher = rulePattern.matcher( st );

                if ( matcher.matches() ) {
                    String rule = matcher.group( 1 );
                    packageTreeNode.addRule( rule,
                                             offset,
                                             st.length() );
                }
                matcher = packagePattern.matcher( st );
                if ( matcher.matches() ) {
                    String packageName = matcher.group( 1 );
                    packageTreeNode.setPackageName( packageName );
                    packageTreeNode.setOffset( offset );
                    packageTreeNode.setLength( st.length() );
                }
                matcher = functionNamePattern.matcher( st );
                if ( matcher.matches() ) {
                    String functionName = matcher.group( 1 );
                    packageTreeNode.addFunction( functionName + "()",
                                                 offset,
                                                 st.length() );
                }

                offset += st.length() + 1; //+1 for the newline
            }
        } catch ( IOException e ) {
        }
    }

    /**
     * 
     * @return the current contents of the document
     */
    private String getRuleFileContents() {
        IDocumentProvider documentProvider = editor.getDocumentProvider();
        IEditorInput editorInput = editor.getEditorInput();
        IDocument document = documentProvider.getDocument( editorInput );
        String ruleFileContents = document.get();
        return ruleFileContents;
    }
}