/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.guvnor.tools.tests;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
//import org.jboss.tools.test.util.TestProjectProvider;

public class GuvnorJunitTest extends TestCase {
	
//	TestProjectProvider provider;
	IProject project;
	
	public GuvnorJunitTest() {
		
	}
	
	public void setUp() throws Exception {
//		provider = new TestProjectProvider("org.guvnor.tools.test.junit", "guvnorTestProj", "guvnorTestProj", true);
//		project = provider.getProject();
//		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
	}
	
	public void tearDown() throws Exception{
		
	}

    public void testNothing() {}

    // TODO this test is disabled because guvnor tools cannot depends on jbosstools (because it's the other way around)
	public void disabled_testJBIDE5265() throws Exception{
		IFile meta = project.getFile("guvnorMetadata");
		GuvnorMetadataUtils.loadGuvnorMetadata(meta);
		
		meta.delete(true, null);
		project.refreshLocal(0, null);
		
		assertFalse("The metadata file can not be deleted", meta.exists());
		
	}

}
