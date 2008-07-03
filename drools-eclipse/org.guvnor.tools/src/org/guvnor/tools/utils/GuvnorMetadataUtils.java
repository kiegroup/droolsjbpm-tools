package org.guvnor.tools.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;

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
	
	public static boolean isGuvnorControlledResource(IResource resource) {
		return findGuvnorMetadata(resource) != null;
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
	
	public static void setGuvnorMetadataProps(File mdFile, 
			                                 GuvnorMetadataProps mdProps) throws Exception {
		FileOutputStream fos = new FileOutputStream(mdFile);
		Properties props = new Properties();
		props.put("repository", 	mdProps.getRepository());
		props.put("fullpath", 		mdProps.getFullpath());
		props.put("filename", 		mdProps.getFilename());
		props.put("lastmodified", 	mdProps.getVersion());
		props.store(fos, null);
		fos.flush();
		fos.close();	
	}
	
	public static IPath createGuvnorMetadataLocation(String rootPath) throws Exception {
		IPath path = new Path(rootPath + File.separator + ".guvnorinfo");
		if (!path.toFile().exists()) {
			if (!path.toFile().mkdir()) {
				throw new Exception("Could not create directory " + path.toOSString());
			}
		}
		return path;
	}
	
	public static File getGuvnorMetadataFile(String path, String fname) {
		return new File(path + File.separator + "." + fname);
	}
	
	public static IFile getGuvnorControlledResource(IResource resource) throws Exception {
		if (resource instanceof IFile) {
			Properties props = new Properties();
			props.load(((IFile)resource).getContents());
			return (IFile)ResourcesPlugin.getWorkspace().
								getRoot().findMember(resource.getFullPath().
										removeLastSegments(2).append(props.getProperty("filename")));
		} else {
			return null;
		}
	}
	
	public static boolean isGuvnorMetadata(IResource resource) {
		return resource.getFullPath().removeLastSegments(1).
							toOSString().endsWith(".guvnorinfo");
	}
	
	public static void markCurrentGuvnorResource(IResource resource) throws CoreException {
		resource.setPersistentProperty(generateQualifiedName("version"), "current");
	}
	
	public static void markExpiredGuvnorResource(IResource resource) throws CoreException {
		resource.setPersistentProperty(generateQualifiedName("version"), "expired");
	}
	
	public static String getGuvnorResourceProperty(IResource resource) throws CoreException {
		return resource.getPersistentProperty(generateQualifiedName("version"));
	}
	
	public static boolean isGuvnorResourceCurrent(IResource resource) throws CoreException {
		String ver = resource.getPersistentProperty(generateQualifiedName("version"));
		if (ver == null) {
			return false;
		}
		return ver.equals("current");
	}
	
	private static QualifiedName generateQualifiedName(String attr) {
		return new QualifiedName("org.guvnor.tools", attr); 
	}
}