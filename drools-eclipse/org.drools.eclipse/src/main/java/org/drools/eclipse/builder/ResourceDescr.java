package org.drools.eclipse.builder;

import org.drools.builder.ResourceType;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

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
		return resource.getFullPath().removeFirstSegments(1).toString();
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
