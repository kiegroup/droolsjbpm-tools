package org.drools.eclipse.editors.rete;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.drools.eclipse.editors.DRLRuleEditor2;
import org.drools.eclipse.editors.ZoomInAction2;
import org.drools.eclipse.editors.ZoomOutAction2;
import org.eclipse.core.filebuffers.manipulation.ContainerCreator;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class ZoomControlTest extends TestCase {

    private IFile                         fFile1;
    private IFile                         fFile2;

    private final static IProgressMonitor NULL_MONITOR     = new NullProgressMonitor();

    private static final String           ORIGINAL_CONTENT = "package test\nrule \"a\"\nend\nrule \"b\"\nend";

    public ZoomControlTest(String name) {
        super( name );
    }

    private String getOriginalContent() {
        return ORIGINAL_CONTENT;
    }

    /*
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        IFolder folder = createFolder( "ZoomControlTestProject/multipleEditorTest/" );
        fFile1 = createFile( folder,
                             "myfile1.drl",
                             getOriginalContent() );
        fFile2 = createFile( folder,
                             "myfile2.drl",
                             getOriginalContent() );
    }

    /*
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        deleteProject( "ZoomControlTestProject" );
        fFile1 = null;
        fFile2 = null;
    }

    public void testMultipleEditors() throws PartInitException {

        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();

        DRLRuleEditor2 part1 = (DRLRuleEditor2) IDE.openEditor( page,
                                                                fFile1 );
        DRLRuleEditor2 part2 = (DRLRuleEditor2) IDE.openEditor( page,
                                                                fFile2 );

        checkVisibility( part1,
                         part2,
                         false );

        // Editor1 active
        page.activate( part1 );
        checkVisibility( part1,
                         part2,
                         false );
// TODO
//        part1.setActivePage( 1 );
//        checkVisibility( part1,
//                         part2,
//                         true );
//
//        part1.setActivePage( 0 );
//        checkVisibility( part1,
//                         part2,
//                         false );
//
//        part1.setActivePage( 1 );
//        checkVisibility( part1,
//                         part2,
//                         true );
//
//        // Editor2 active
//        page.activate( part2 );
//        part2.setActivePage( 0 );
//        checkVisibility( part1,
//                         part2,
//                         false );
//
//        part2.setActivePage( 1 );
//        checkVisibility( part1,
//                         part2,
//                         true );
//
//        // Editor1 active
//        page.activate( part1 );
//        checkVisibility( part1,
//                         part2,
//                         true );
//
//        // Editor2 active
//        page.activate( part2 );
//        checkVisibility( part1,
//                         part2,
//                         true );
//
//        part2.setActivePage( 0 );
//        checkVisibility( part1,
//                         part2,
//                         false );
//
//        // Editor1 active
//        page.activate( part1 );
//        checkVisibility( part1,
//                         part2,
//                         true );
//        part2.setActivePage( 0 );
//        checkVisibility( part1,
//                         part2,
//                         false );

    }

    public void testSecondEditorAfterFirst() throws PartInitException {

        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();

        DRLRuleEditor2 part1 = (DRLRuleEditor2) IDE.openEditor( page,
                                                                fFile1 );
// TODO
//        // Editor1 active
//        page.activate( part1 );
//        part1.setActivePage( 1 );
//        checkVisibility( part1,
//                         null,
//                         true );
//
//        DRLRuleEditor2 part2 = (DRLRuleEditor2) IDE.openEditor( page,
//                                                                fFile2 );
//        page.activate( part2 );
//        checkVisibility( part1,
//                         part2,
//                         false );

    }

    private void checkVisibility(DRLRuleEditor2 part1,
                                 DRLRuleEditor2 part2,
                                 boolean enabled) {
        if ( part1 != null ) {
            checkVisibility( part1,
                             enabled );
        }

        if ( part2 != null ) {
            checkVisibility( part2,
                             enabled );
        }
    }

    private void checkVisibility(DRLRuleEditor2 editor,
                                 boolean enabled) {
        ZoomInAction2 zoomIn = (ZoomInAction2) editor.getAdapter( ZoomInAction2.class );
        ZoomOutAction2 zoomOut = (ZoomOutAction2) editor.getAdapter( ZoomOutAction2.class );
        ZoomComboContributionItem zitem = (ZoomComboContributionItem) editor.getAdapter( ZoomComboContributionItem.class );

        assertEquals( enabled,
                      zoomIn.isEnabled() );
        assertEquals( enabled,
                      zoomOut.isEnabled() );
        assertEquals( enabled,
                      zitem.getZoomManager() != null );

    }

    private IFile createFile(IFolder folder,
                             String name,
                             String contents) throws CoreException {
        IFile file = folder.getFile( name );
        InputStream inputStream = new ByteArrayInputStream( contents.getBytes() );
        file.create( inputStream,
                     true,
                     NULL_MONITOR );
        return file;
    }

    private IFolder createFolder(String portableFolderPath) throws CoreException {
        ContainerCreator creator = new ContainerCreator( ResourcesPlugin.getWorkspace(),
                                                         new Path( portableFolderPath ) );
        IContainer container = creator.createContainer( NULL_MONITOR );
        return (IFolder) container;
    }

    private void deleteProject(String projectName) throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject( projectName );
        if ( project.exists() ) project.delete( true,
                                                true,
                                                NULL_MONITOR );
    }

}
