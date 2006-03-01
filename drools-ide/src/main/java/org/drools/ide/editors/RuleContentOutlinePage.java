package org.drools.ide.editors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * 
 * @author "Jeff Brown" <brown_j@ociweb.com>
 */
public class RuleContentOutlinePage extends ContentOutlinePage {

    private IDocumentProvider    ruleDocumentProvider;

    private static final Pattern rulePattern     = Pattern.compile( "rule\\s*\"?([^\"]+)\"?.*",
                                                                    Pattern.DOTALL );

    private static final Pattern packagePattern  = Pattern.compile( "package\\s*([^\"]+)",
                                                                    Pattern.DOTALL );

    private PackageTreeNode      packageTreeNode = new PackageTreeNode();

    public RuleContentOutlinePage(IDocumentProvider provider) {
        super();
        ruleDocumentProvider = provider;
    }

    public void createControl(Composite parent) {
        super.createControl( parent );
        TreeViewer viewer = getTreeViewer();
        viewer.setContentProvider( new ContentProvider() );
        viewer.setLabelProvider( new WorkbenchLabelProvider() );

        if ( fileEditorInput != null ) viewer.setInput( fileEditorInput );
        update();
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

    class ContentProvider
        implements
        ITreeContentProvider {

        protected void parse(IDocument document) {

            String ruleFileContents = document.get();
            StringReader stringReader = new StringReader( ruleFileContents );
            BufferedReader bufferedReader = new BufferedReader( stringReader );
            try {
                String st = bufferedReader.readLine();
                while ( st != null ) {
                    Matcher matcher = rulePattern.matcher( st );

                    if ( matcher.matches() ) {
                        String rule = matcher.group( 1 );
                        packageTreeNode.addRule( rule );
                    }
                    matcher = packagePattern.matcher( st );
                    if ( matcher.matches() ) {
                        packageTreeNode.setPackageName( matcher.group( 1 ) );
                    }
                    st = bufferedReader.readLine();
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
                IDocument document = ruleDocumentProvider.getDocument( newInput );
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