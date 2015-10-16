/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.drools.eclipse.builder;

import org.drools.core.command.GetSessionClockCommand;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.kie.api.io.Resource;
import org.kie.internal.io.ResourceFactory;
import org.kie.api.io.ResourceType;

public class ResourceDescr {

	private IResource resource;
	private ResourceType type;
	private String content;
	private Resource droolsResource;
	
	public static ResourceDescr createResourceDescr(IResource resource) {
		if ( !(resource instanceof IFile) ) {
			return null;
		}
		
		IFile file = (IFile) resource;
		ResourceDescr descr = new ResourceDescr();
		descr.resource = resource;
		descr.type = ResourceType.determineResourceType( resource.getName() );
		if (descr.type == null) {
			return null;
		}
		
		try {
			descr.content = new String( Util.getResourceContentsAsCharArray( file ) );
			descr.droolsResource = ResourceFactory.newInputStreamResource( file.getContents() );
		} catch (Exception e) {
			DroolsEclipsePlugin.log( e );
			return null;
		}
		return descr;
	}

	public IResource getResource() {
		return resource;
	}
	
	public String getName() {
		return resource.getName();
	}
	
	public String getSourcePathName() {
		return resource.getProjectRelativePath().toString();
	}

	public ResourceType getType() {
		return type;
	}

	public String getContent() {
		return content;
	}

	public Resource getContentAsDroolsResource() {
		return droolsResource;
	}
	
	@Override
	public String toString() {
		return resource.getName();
	}
}
