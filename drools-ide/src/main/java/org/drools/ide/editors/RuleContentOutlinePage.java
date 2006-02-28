package org.drools.ide.editors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * This is very much nothing more than a stubbed up starting place at this
 * point...
 * 
 * @author "Jeff Brown" <brown_j@ociweb.com>
 */
public class RuleContentOutlinePage extends ContentOutlinePage {

    private IDocumentProvider    ruleDocumentProvider;

    private static final Pattern rulePattern    = Pattern.compile( "rule\\s*\"?([^\"]+)\"?.*",
                                                                   Pattern.DOTALL );
    private static final Pattern packagePattern = Pattern.compile( "package\\s*([^\"]+)",
                                                                   Pattern.DOTALL );

    public RuleContentOutlinePage(IDocumentProvider provider) {
        super();
        ruleDocumentProvider = provider;
    }

    public void createControl(Composite parent) {
        super.createControl( parent );
        TreeViewer viewer = getTreeViewer();
        viewer.setContentProvider( new ContentProvider() );
        viewer.setLabelProvider( new LabelProvider() );

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

        protected List      packageElements = new ArrayList();
        private PackageNode packageNode;

        protected void parse(IDocument document) {

            String ruleFileContents = document.get();
            StringReader stringReader = new StringReader( ruleFileContents );
            BufferedReader bufferedReader = new BufferedReader( stringReader );
            try {
                String st = bufferedReader.readLine();
                while ( st != null ) {
                    Matcher ruleMatcher = rulePattern.matcher( st );

                    if ( ruleMatcher.matches() ) {
                        String rule = ruleMatcher.group( 1 );
                        packageElements.add( new RuleNode( rule ) );
                    }

                    Matcher packageMatcher = packagePattern.matcher( st );
                    if ( packageMatcher.matches() ) {
                        String packageName = packageMatcher.group( 1 );
                        packageNode = new PackageNode( packageName );
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

            packageElements.clear();

            if ( newInput != null ) {
                IDocument document = ruleDocumentProvider.getDocument( newInput );
                if ( document != null ) {
                    parse( document );
                }
            }
        }

        /*
         * @see IContentProvider#dispose
         */
        public void dispose() {
            if ( packageElements != null ) {
                packageElements.clear();
                packageElements = null;
            }
        }

        /*
         * @see IContentProvider#isDeleted(Object)
         */
        public boolean isDeleted(Object element) {
            return false;
        }

        /*
         * @see IStructuredContentProvider#getElements(Object)
         */
        public Object[] getElements(Object element) {
            return new Object[]{packageNode};
        }

        /*
         * @see ITreeContentProvider#hasChildren(Object)
         */
        public boolean hasChildren(Object element) {
            return element == packageNode;
        }

        /*
         * @see ITreeContentProvider#getParent(Object)
         */
        public Object getParent(Object element) {
            if ( element instanceof RuleNode ) return packageNode;
            return null;
        }

        /*
         * @see ITreeContentProvider#getChildren(Object)
         */
        public Object[] getChildren(Object element) {
            if ( element == fileEditorInput ) return packageElements.toArray();
            if ( element == packageNode ) return packageElements.toArray();
            return new Object[0];
        }
    }
}

class PackageNode {
    private final String packageName;

    PackageNode(String packageName) {
        this.packageName = packageName;
    }

    public String toString() {
        return packageName;
    }
}

class RuleNode {
    private final String ruleName;

    RuleNode(String ruleName) {
        this.ruleName = ruleName;
    }

    public String toString() {
        return "Rule: " + ruleName;
    }
}
