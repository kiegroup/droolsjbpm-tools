package org.guvnor.tools.tests;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
import org.jboss.tools.test.util.TestProjectProvider;

public class GuvnorJunitTests extends TestCase {
	
	TestProjectProvider provider;
	IProject project;
	
	public GuvnorJunitTests() {
		
	}
	
	public void setUp() throws Exception {
		provider = new TestProjectProvider("org.guvnor.tools.test.junit", "guvnorTestProj", "guvnorTestProj", true); 
		project = provider.getProject();
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
	}
	
	public void tearDown() throws Exception{
		
	}
	
	
	public void testJBIDE5265() throws Exception{
		IFile meta = project.getFile("guvnorMetadata");
		GuvnorMetadataUtils.loadGuvnorMetadata(meta);
		
		meta.delete(true, null);
		project.refreshLocal(0, null);
		
		assertFalse("The metadata file can not be deleted", meta.exists());
		
	}

}
