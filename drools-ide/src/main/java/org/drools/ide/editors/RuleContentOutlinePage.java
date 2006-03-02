package org.drools.ide.editors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.model.WorkbenchLabelProvider;
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
    private DRLRuleSetEditor     editor;

    //the "root" node
    private PackageTreeNode      packageTreeNode = new PackageTreeNode();
    
    ///////////////////////////////////
    // Patterns that the parser uses
    ///////////////////////////////////
    private static final Pattern rulePattern     = Pattern.compile( "rule\\s*\"?([^\"]+)\"?.*",
                                                                    Pattern.DOTALL );

    private static final Pattern packagePattern  = Pattern.compile( "package\\s*([^\"]+)",
                                                                    Pattern.DOTALL );

    

    public RuleContentOutlinePage(DRLRuleSetEditor editor) {
        super();
        this.editor = editor;
    }

    public void createControl(Composite parent) {
        super.createControl( parent );
        TreeViewer viewer = getTreeViewer();
        viewer.setContentProvider( new ContentProvider() );
        viewer.setLabelProvider( new WorkbenchLabelProvider() );

        if ( fileEditorInput != null ) viewer.setInput( fileEditorInput );
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

    private IFileEditorInput fileEditorInput = null;

    public void setInput(IFileEditorInput input) {
        fileEditorInput = input;
        update();
    }

    /**
     * Updates the outline page.
     */
    public void update() {
        TreeViewer viewer = getTreeViewer();

        if ( viewer != null ) {
            Control control = viewer.getControl();
            if ( control != null && !control.isDisposed() ) {
                control.setRedraw( false );
                viewer.setInput( fileEditorInput );
                viewer.expandAll();
                control.setRedraw( true );
            }
        }
    }

    /**
     * Simple content provider for a primitive understanding of a rule DRL file.
     * The day may come where we hook into an official AST,
     * But that is not this day. This day, we parse ! (apologies to J.R.R. Tolkien).
     */
    class ContentProvider
        implements
        ITreeContentProvider {

        /** 
         * A simple line by line parse of the document, using the precompiled regex to tease out a rule structure.
         * In future, this may hook into the AST from the parser/incremental compiler, thus rendering the regex obsolete.
         */
        protected void parse(IDocument document) {

            String ruleFileContents = document.get();
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
                    offset += st.length() + 1; //+1 for the newline
                }
            } catch ( IOException e ) {
            }
        }

        /*
         * @see IContentProvider#inputChanged(Viewer, Object, Object)
         */
        public void inputChanged(Viewer viewer,
                                 Object oldInput,
                                 Object newInput) {

            packageTreeNode = new PackageTreeNode();

            if ( newInput != null ) {
                IDocument document = editor.getDocumentProvider().getDocument( newInput );
                if ( document != null ) {
                    parse( document );
                }
            }
        }

        public void dispose() {
        }

        public boolean isDeleted(Object element) {
            return false;
        }

        public Object[] getElements(Object element) {
            return new Object[]{packageTreeNode};
        }

        public boolean hasChildren(Object element) {
            boolean hasChildren = element == fileEditorInput || element == packageTreeNode;
            return hasChildren;
        }

        public Object getParent(Object element) {
            if ( element instanceof RuleTreeNode ) return packageTreeNode;
            return null;
        }

        public Object[] getChildren(Object element) {
            if ( element == packageTreeNode ) {
                return packageTreeNode.getChildren( element );
            }

            return new Object[0];
        }
    }
}