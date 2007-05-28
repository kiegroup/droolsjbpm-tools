package org.drools.eclipse.rulebuilder.editors;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.drools.brms.client.modeldriven.SuggestionCompletionEngine;
import org.drools.brms.server.rules.SuggestionCompletionLoader;
import org.drools.brms.server.util.BRXMLPersistence;
import org.drools.eclipse.rulebuilder.RuleBuilderPlugin;
import org.drools.eclipse.util.ProjectClassLoader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class RuleEditor extends FormEditor
    implements
    IResourceChangeListener {

    private BrxmlPage                  brxmlPage;

    private TextEditor                 editor        = new TextEditor();

    private TextEditor                 packageEditor = new TextEditor();

    private SuggestionCompletionEngine completion;

    private SuggestionCompletionLoader loader;

    private FileEditorInput            packageEditorInput;

    public RuleEditor() {
        super();
        ResourcesPlugin.getWorkspace().addResourceChangeListener( this );

    }

    protected FormToolkit createToolkit(Display display) {
        // Create a toolkit that shares colors between editors.
        return new FormToolkit( RuleBuilderPlugin.getDefault().getFormColors( display ) );
    }

    protected void addPages() {
        brxmlPage = new BrxmlPage( this );
        try {
            addPage( brxmlPage );
            addPage( editor,
                     getEditorInput() );

            FileEditorInput existingFile = (FileEditorInput) getEditorInput();
            IPath fullPath = existingFile.getFile().getFullPath();
            IPath packagePath = fullPath.removeLastSegments( 1 ).addTrailingSeparator().append( "rule.package" );

            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile( packagePath );

            IJavaProject javaProject = JavaCore.create( file.getProject() );

            ClassLoader classLoader = ProjectClassLoader.getProjectClassLoader( javaProject );

            loader = new SuggestionCompletionLoader( classLoader );

            if ( !file.exists() ) {
                InputStream is = new ByteArrayInputStream( "// Header file".getBytes() );
                try {
                    file.create( is,
                                 true,
                                 null );
                } catch ( CoreException e ) {
                    // TODO Handle
                    e.printStackTrace();
                }
            }

            packageEditorInput = new FileEditorInput( file );

            reloadCompletionEngine();

            // addPage(packageEditor, packageEditorInput);

            setPageText( 1,
                         "BRXML Preview" );
            // setPageText(2, "Package Description");

        } catch ( PartInitException e ) {
            e.printStackTrace();
        }
    }

    private void reloadCompletionEngine() {
        try {

            String str = "";

            InputStream is = packageEditorInput.getFile().getContents();
            StringBuffer out = new StringBuffer();
            byte[] b = new byte[4096];
            for ( int n; (n = is.read( b )) != -1; ) {
                out.append( new String( b,
                                        0,
                                        n ) );
            }
            str = out.toString();
            System.out.println( "GOT " + str );

            completion = loader.getSuggestionEngine( str,
                                                     new ArrayList(),
                                                     new ArrayList() );

            String[] facts = completion.getFactTypes();
            System.out.println( "NUMBER OF FACTS NOW " + facts.length );
            
            for(int i=0; i<facts.length; i++) {
                System.out.println("F "+facts[i]);
            }

        } catch ( Exception e ) {
            // TODO Handle
            e.printStackTrace();
        }
    }

    public boolean isDirty() {
        return editor.isDirty() || brxmlPage.isDirty() || packageEditor.isDirty();
    }

    protected void pageChange(int newPageIndex) {
        super.pageChange( newPageIndex );

        IDocument document = getInputDocument();

        if ( newPageIndex == 0 ) {
            brxmlPage.setModelXML( document.get() );
            brxmlPage.refresh();
        }
        if ( newPageIndex == 1 ) {
            if ( brxmlPage.isDirty() ) {
                document.set( BRXMLPersistence.getInstance().marshal( brxmlPage.getRuleModel() ) );
            }
        }

    }

    public void doSave(IProgressMonitor monitor) {
        IDocument document = getInputDocument();

        if ( brxmlPage.isDirty() ) {
            document.set( BRXMLPersistence.getInstance().marshal( brxmlPage.getRuleModel() ) );
        }

        editor.doSave( monitor );
        packageEditor.doSave( monitor );

        brxmlPage.getModeller().setDirty( false );

        reloadCompletionEngine();

    }

    private IDocument getInputDocument() {
        IEditorInput input = getEditorInput();
        IDocumentProvider docProvider = DocumentProviderRegistry.getDefault().getDocumentProvider( input );
        IDocument document = docProvider.getDocument( input );
        return document;
    }

    public void doSaveAs() {
        editor.doSaveAs();
        brxmlPage.getModeller().setDirty( false );
        setPageText( 0,
                     editor.getTitle() );
        setInput( editor.getEditorInput() );
    }

    public boolean isSaveAsAllowed() {
        return true;
    }

    public void resourceChanged(final IResourceChangeEvent event) {
        if ( event.getType() == IResourceChangeEvent.PRE_CLOSE ) {
            Display.getDefault().asyncExec( new Runnable() {
                public void run() {
                    IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
                    for ( int i = 0; i < pages.length; i++ ) {
                        if ( ((FileEditorInput) editor.getEditorInput()).getFile().getProject().equals( event.getResource() ) ) {
                            IEditorPart editorPart = pages[i].findEditor( editor.getEditorInput() );
                            pages[i].closeEditor( editorPart,
                                                  true );
                        }
                    }
                }
            } );
        }
    }

    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener( this );
        super.dispose();
    }

    public void dirtyPropertyChanged() {
        firePropertyChange( IEditorPart.PROP_DIRTY );
        brxmlPage.refresh();
    }

    public SuggestionCompletionEngine getCompletionEngine() {
        return completion;
    }

}