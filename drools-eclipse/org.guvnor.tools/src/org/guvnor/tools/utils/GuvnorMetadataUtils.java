package org.guvnor.tools.utils;

import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * A set of utilities for dealing with (local) Guvnor metadata.
 * @author jgraham
 */
public class GuvnorMetadataUtils {
	/**
	 * Finds the local Guvnor metadata file associated with a given resource.
	 * @param resource The resource to locate metadata for.
	 * @return The metadata for the given resource, null if metadata is not found.
	 */
	public static IFile findGuvnorMetadata(IResource resource) {
		IFile res = null;
		IPath dir = resource.getFullPath().removeLastSegments(1);
		IPath mdpath = dir.append(".guvnorinfo").append("." + resource.getName());
		IResource mdResource = resource.getWorkspace().getRoot().findMember(mdpath);
		if (mdResource != null 
		   && mdResource.exists() 
		   && mdResource instanceof IFile) {
			res = (IFile)mdResource;
		}
		return res;
	}
	
	public static GuvnorMetadataProps getGuvnorMetadata(IResource resource) throws Exception {
		IFile mdFile = findGuvnorMetadata(resource);
		if (mdFile == null) {
			return null;
		}
		Properties props = new Properties();
		props.load(mdFile.getContents());
		return new GuvnorMetadataProps(props.getProperty("filename"),
				                       props.getProperty("repository"),
				                       props.getProperty("fullpath"),
				                       props.getProperty("lastmodified"));
	}
}