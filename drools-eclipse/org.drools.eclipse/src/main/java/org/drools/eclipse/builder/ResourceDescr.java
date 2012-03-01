package org.drools.eclipse.builder;

import org.drools.builder.ResourceType;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

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
		} catch (CoreException e) {
			DroolsEclipsePlugin.log( e );
			return null;
		}
		
		return descr;
	}

	public IResource getResource() {
		return resource;
	}

	public ResourceType getType() {
		return type;
	}

	public String getContent() {
		return content;
	}

	public Resource getContentAsDroolsResource() {
		if (droolsResource == null) {
			droolsResource = ResourceFactory.newByteArrayResource(content.getBytes());
		}
		return droolsResource;
	}
	
	@Override
	public String toString() {
		return resource.getName();
	}
}
