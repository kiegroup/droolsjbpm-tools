package org.drools.eclipse.wizard.knowledgebase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.core.filebuffers.manipulation.ContainerCreator;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.ResourceWorkingSetFilter;
import org.eclipse.ui.dialogs.FileSystemElement;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceNavigator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.util.Util;

public class NewKnowledgeBaseWizardPage extends WizardPage {
    private IWorkbench               workbench;
    private IStructuredSelection     selection;
    private IJavaProject             project;
    private IFolder                  folder;

    //    private Text                 projectName;
    private Text                     kbaseName;
    private Text                     kbasePkg;                                                                  ;

    private IWorkingSet              workingSet;
    //private FilePatternFilter        patternFilter    = new FilePatternFilter();
    private ResourceWorkingSetFilter workingSetFilter  = new ResourceWorkingSetFilter();

    private ResourceManager          resourceManager;

    private static final int         validKBaseNameBit = 1;
    private static final int         validKBasePkgBit  = 2;
    private static final int         validFolderBit    = 4;

    private static final int         validPageMask     = validKBaseNameBit ^ validKBasePkgBit ^ validFolderBit;
    
    private int                      currentPageMask   = 0;

    public NewKnowledgeBaseWizardPage(IWorkbench workbench,
                                      IStructuredSelection selection) {
        super( "extendedNewKnowledgeBasePage" );
        setTitle( "New Knowledge Baset" );
        setDescription( "Create a new Knowledge Base" );
        this.workbench = workbench;
        this.selection = selection;

        resourceManager = new LocalResourceManager( JFaceResources.getResources() );
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite( parent, SWT.NULL );
        composite.setFont( parent.getFont() );
        composite.setLayout( new GridLayout() );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        createControls( composite );
        setPageComplete( true );
        // Show description on opening
        setErrorMessage( null );
        setMessage( null );
        setControl( composite );
    }

    private void createControls(Composite parent) {
        createProjectSelector( parent );
        createKnowledgeBaseName( parent );
        createKnowledgeBasePkgName( parent );
    }

    private void createProjectSelector(Composite container) {
        ResourceNavigator nav = new ResourceNavigator();

        TreeViewer viewer = new TreeViewer( container, SWT.SINGLE |
                                                       SWT.H_SCROLL |
                                                       SWT.V_SCROLL |
                                                       SWT.BORDER );

        viewer.setUseHashlookup( true );
        viewer.setContentProvider( new WorkbenchContentProvider() );
        viewer.setLabelProvider( new DecoratingLabelProvider( new WorkbenchLabelProvider(),
                                                              IDEWorkbenchPlugin.getDefault().getWorkbench().getDecoratorManager().getLabelDecorator() ) );

        viewer.setInput( ResourcesPlugin.getWorkspace().getRoot() );

        GridData data = new GridData( GridData.FILL_BOTH );
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.heightHint = 400;
        data.widthHint = 300;
        viewer.getControl().setLayoutData( data );

        if ( selection.getFirstElement() instanceof IFolder ) {
            IResource res = (IResource) selection.getFirstElement();
            try {
                if ( res.getProject().hasNature( JavaCore.NATURE_ID ) ) {
                    project = JavaCore.create( res.getProject() );
                    folder = (IFolder) res;
                    viewer.setSelection( selection, true );
                }
            } catch ( CoreException e ) {
                // swallow as project selection will just be null, and user can select a project manually.
            }
        }

        viewer.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if ( selection.getFirstElement() instanceof IFolder ) {
                    IResource res = (IResource) selection.getFirstElement();
                    try {
                        if ( res.getProject().hasNature( JavaCore.NATURE_ID ) ) {
                            project = JavaCore.create( res.getProject() );
                            folder = (IFolder) res;
                        }
                    } catch ( CoreException e ) {
                        // swallow as project selection will just be null, and user can select a project manually.
                    }
                }
            }
        } );      
    }

    /**
     * Returns a content provider for <code>FileSystemElement</code>s that returns 
     * only files as children.
     */
    private ITreeContentProvider getFileProvider() {
        return new WorkbenchContentProvider() {
            public Object[] getChildren(Object o) {
                if ( o instanceof FileSystemElement ) {
                    return ((FileSystemElement) o).getFiles().getChildren( o );
                }
                return new Object[0];
            }
        };
    }

    /**
     * Returns a content provider for <code>FileSystemElement</code>s that returns 
     * only folders as children.
     */
    private ITreeContentProvider getFolderProvider() {
        return new WorkbenchContentProvider() {
            public Object[] getChildren(Object o) {
                if ( o instanceof FileSystemElement ) {
                    return ((FileSystemElement) o).getFolders().getChildren( o );
                }
                return new Object[0];
            }
        };
    }

    public final Image getImage(Object element) {
        //obtain the base image by querying the element
        IWorkbenchAdapter adapter = getAdapter( element );
        if ( adapter == null ) {
            return null;
        }

        ImageDescriptor descriptor = adapter.getImageDescriptor( element );
        if ( descriptor == null ) {
            return null;
        }

        //add any annotations to the image descriptor
        return (Image) resourceManager.get( descriptor );
    }

    protected final IWorkbenchAdapter getAdapter(Object o) {
        return (IWorkbenchAdapter) Util.getAdapter( o, IWorkbenchAdapter.class );
    }

    private void createKnowledgeBaseName(Composite container) {
        //package name
        Label pack = new Label( container, SWT.NONE );
        pack.setText( "Knowledge Base Name:" );
        pack.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING ) );
        pack.setFont( this.getFont() );

        kbaseName = new Text( container, SWT.BORDER );
        kbaseName.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        kbaseName.setToolTipText( "Knowledeg Base requires a name" );
        kbaseName.setFont( this.getFont() );
        kbaseName.addModifyListener( new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if ( validKBaseName() ) {
                    setPageComplete( currentPageMask == validPageMask );
                } else {
                    setPageComplete( false );   
                }
            }
        } );
    }

    private void createKnowledgeBasePkgName(Composite container) {
        //package name
        Label pack = new Label( container, SWT.NONE );
        pack.setText( "Package:" );
        pack.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING ) );
        pack.setFont( this.getFont() );

        kbasePkg = new Text( container, SWT.BORDER );
        kbasePkg.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        kbasePkg.setToolTipText( "Package required" );
        kbasePkg.setFont( this.getFont() );
        kbasePkg.addModifyListener( new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if ( validPackageName() ) {
                    setPageComplete( currentPageMask == validPageMask );
                } else {
                    setPageComplete( false );   
                }
            }
        } );
    }

    protected boolean validPage() {
        return validKBaseName() && validPackageName();
    }

    private boolean validKBaseName() {
        if ( project == null ) {
            currentPageMask = currentPageMask ^ validFolderBit;
            setErrorMessage( "You must select a folder in a JavaProject" );
            return false;
        }

        if ( this.kbaseName.getText() == null || kbaseName.getText().equals( "" ) ) {
            currentPageMask = currentPageMask ^ validKBaseNameBit;
            setErrorMessage( "You must provide a Knowledeg Base name" );
            return false;
        } else if ( kbaseNameExists() ) {
            currentPageMask = currentPageMask ^ validKBaseNameBit;
            setErrorMessage( "The Knowledge Base name " + kbaseName.getText() + " already exists" );
            return false;
        }

        currentPageMask = currentPageMask | validFolderBit;
        currentPageMask = currentPageMask | validKBaseNameBit;
        setErrorMessage( null );
        return true;
    }

    private boolean kbaseNameExists() {
        IFile ifile = project.getProject().getFile( "kbasePaths.properties" );

        Properties props = new Properties();
        if ( ifile.exists() ) {
            InputStream is = null;
            try {
                is = ifile.getContents();
                props.load( is );
            } catch ( IOException e ) {
                DroolsEclipsePlugin.log( e );
            } catch ( CoreException e ) {
                DroolsEclipsePlugin.log( e );
            } finally {
                if ( is != null ) {
                    try {
                        is.close();
                    } catch ( IOException e ) {
                        DroolsEclipsePlugin.log( e );
                    }
                }
            }
        } else {
            return false;
        }

        if ( props.containsKey( "kbase." + kbaseName.getText() ) ) {
            return true;
        } else {
            return false;
        }
    }

    private boolean validPackageName() {
        if ( this.kbasePkg.getText() == null || kbasePkg.getText().equals( "" ) ) {
            currentPageMask = currentPageMask ^ validKBasePkgBit;
            setErrorMessage( "You must provide a Package name for the KBase Provider" );
            return false;
        }

        currentPageMask = currentPageMask | validKBasePkgBit;
        setErrorMessage( null );
        return true;
    }

    public boolean finish() {
        if ( !validPage() ) {
            return false;
        }

        IFolder targetFolder = folder.getFolder( kbaseName.getText() );
        
        try {
            createFolder( targetFolder );
        } catch ( CoreException e2 ) {
            DroolsEclipsePlugin.log( e2 );
        }

        try {
            updateKBasePaths( kbaseName.getText(), targetFolder.getProjectRelativePath().toString() );
            updateKprojectProperties( kbaseName.getText() );
            createKBaseProperties( kbaseName.getText(), targetFolder );
        } catch ( IOException e1 ) {
            DroolsEclipsePlugin.log( e1 );
        } catch ( CoreException e1 ) {
            DroolsEclipsePlugin.log( e1 );
        }

        IPackageFragmentRoot root = project.getPackageFragmentRoot( targetFolder );

        IClasspathEntry[] oldEntries;
        try {
            oldEntries = project.getRawClasspath();
            IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
            System.arraycopy( oldEntries, 0, newEntries, 0, oldEntries.length );

            newEntries[oldEntries.length] = JavaCore.newSourceEntry( root.getPath() );

            project.setRawClasspath( newEntries, null );
        } catch ( JavaModelException e ) {
            DroolsEclipsePlugin.log( e );
            return false;
        }

        try {
            generateProducer( kbaseName.getText(), kbasePkg.getText(), targetFolder );
        } catch ( IOException e ) {
            e.printStackTrace();
            return false;
        } catch ( CoreException e ) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void generateProducer(String kbaseName,
                                 String kbasePkg,
                                 IFolder ifolder) throws IOException,
                                                 CoreException {
        String s = GenerateKBaseProducer.generateProducer( kbasePkg, kbaseName );

        ifolder = ifolder.getFolder( new Path( kbasePkg.replace( '.', '/' ) ) );
        createFolder( ifolder );
        IFile ifile = ifolder.getFile( kbaseName + "Producer.java" );
        if ( ifile.exists() ) {
            ifile.setContents( new ByteArrayInputStream( s.getBytes() ), true, true, null );
        } else {
            ifile.create( new ByteArrayInputStream( s.getBytes() ), true, null );
        }

        s = GenerateKBaseProducer.generateQualifier( kbasePkg, kbaseName );
        ifile = ifolder.getFile( kbaseName + ".java" );
        if ( ifile.exists() ) {
            ifile.setContents( new ByteArrayInputStream( s.getBytes() ), true, true, null );
        } else {
            ifile.create( new ByteArrayInputStream( s.getBytes() ), true, null );
        }
    }

    public void updateKBasePaths(String kbaseName,
                                 String kbasePath) throws IOException,
                                                  CoreException {
        IFile ifile = project.getProject().getFile( "kbasePaths.properties" );

        Properties props = new Properties();
        if ( ifile.exists() ) {
            InputStream is = null;
            try {
                is = ifile.getContents();
                props.load( is );
            } finally {
                if ( is != null ) {
                    is.close();
                }
            }
        }

        props.setProperty( "kbase." + kbaseName, kbasePath );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.store( baos, null );
        baos.close();

        if ( ifile.exists() ) {
            ifile.setContents( new ByteArrayInputStream( baos.toByteArray() ), true, true, null );
        } else {
            ifile.create( new ByteArrayInputStream( baos.toByteArray() ), true, null );
        }
    }

    public void updateKprojectProperties(String kbaseName) throws IOException,
                                                          CoreException {
        IFolder ifolder = project.getProject().getFolder( "src" );
        IFile ifile = ifolder.getFile( "kproject.properties" );

        Properties props = new Properties();
        if ( ifile.exists() ) {
            InputStream is = null;
            try {
                is = ifile.getContents();
                props.load( is );
            } finally {
                if ( is != null ) {
                    is.close();
                }
            }
        }

        String kbaseEntries = props.getProperty( "kbaseEntries", "" );

        if ( kbaseEntries.length() == 0 ) {
            kbaseEntries = kbaseName;
        } else {
            kbaseEntries = kbaseEntries + ", " + kbaseName;
        }

        props.setProperty( "kbaseEntries", kbaseEntries );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.store( baos, null );
        baos.close();

        if ( ifile.exists() ) {
            ifile.setContents( new ByteArrayInputStream( baos.toByteArray() ), true, true, null );
        } else {
            ifile.create( new ByteArrayInputStream( baos.toByteArray() ), true, null );
        }
    }

    public void createKBaseProperties(String kbaseName,
                                      IFolder kbaseFolder) throws IOException,
                                                          CoreException {
        IFile ifile = kbaseFolder.getFile( kbaseName + ".properties" );

        Properties props = new Properties();
        if ( ifile.exists() ) {
            InputStream is = null;
            try {
                is = ifile.getContents();
                props.load( is );
            } finally {
                if ( is != null ) {
                    is.close();
                }
            }
        }

        String files = props.getProperty( "files", "" );

        props.setProperty( "files", files );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.store( baos, null );
        baos.close();

        if ( ifile.exists() ) {
            ifile.setContents( new ByteArrayInputStream( baos.toByteArray() ), true, true, null );
        } else {
            ifile.create( new ByteArrayInputStream( baos.toByteArray() ), true, null );
        }
    }

    public static void createFolder(IFolder folder) throws CoreException {
        if ( !folder.exists() ) {
            if ( folder.getParent() instanceof IFolder ) {
                createFolder( (IFolder) folder.getParent() );
            }
            folder.create( true, true, null );
        }
    }

}
