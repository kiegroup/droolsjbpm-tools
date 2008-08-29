package org.guvnor.tools.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.guvnor.tools.Activator;
import org.guvnor.tools.views.model.ResourceHistoryEntry;

/**
 * A set of utilities for dealing with (local) Guvnor metadata.
 * @author jgraham
 */
public class GuvnorMetadataUtils {
	/**
	 * Finds the local Guvnor metadata file associated with a given resource.
	 * @param resource The resource to locate metadata for.
	 * @return The metadata file for the given resource, null if metadata is not found.
	 */
	public static IFile findGuvnorMetadata(IResource resource) {
		IFile res = null;
		IPath dir = resource.getFullPath().removeLastSegments(1);
		IPath mdpath = dir.append(".guvnorinfo").append("." + resource.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		IResource mdResource = resource.getWorkspace().getRoot().findMember(mdpath);
		if (mdResource != null 
		   && mdResource.exists() 
		   && mdResource instanceof IFile) {
			res = (IFile)mdResource;
		}
		return res;
	}
	
	/**
	 * Finds the local Guvnor metadata file associated with a given resource.
	 * @param resource The resource path to locate metadata for.
	 * @return The metadata file for the given resource, null if metadata is not found.
	 */
	public static IFile findGuvnorMetadata(IPath resource) {
		IFile res = null;
		IPath dir = resource.removeLastSegments(1);
		IPath mdpath = dir.append(".guvnorinfo").append("." + resource.lastSegment()); //$NON-NLS-1$ //$NON-NLS-2$
		IResource mdResource = Activator.getDefault().getWorkspace().
											getRoot().findMember(mdpath);
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
	
	public static GuvnorMetadataProps loadGuvnorMetadata(IFile mdFile) throws Exception {
		Properties props = new Properties();
		props.load(mdFile.getContents());
		return new GuvnorMetadataProps(props.getProperty("filename"), //$NON-NLS-1$
				                       props.getProperty("repository"), //$NON-NLS-1$
				                       props.getProperty("fullpath"), //$NON-NLS-1$
				                       props.getProperty("lastmodified"), //$NON-NLS-1$
				                       props.getProperty("revision")); //$NON-NLS-1$
	}
	
	public static GuvnorMetadataProps getGuvnorMetadata(IResource resource) throws Exception {
		IFile mdFile = findGuvnorMetadata(resource);
		if (mdFile == null) {
			return null;
		}
		return loadGuvnorMetadata(mdFile);
	}
	
	public static void writeGuvnorMetadataProps(File mdFile, 
			                                 GuvnorMetadataProps mdProps) throws Exception {
		FileOutputStream fos = new FileOutputStream(mdFile);
		Properties props = new Properties();
		props.put("repository", 	mdProps.getRepository()); //$NON-NLS-1$
		props.put("fullpath", 		mdProps.getFullpath()); //$NON-NLS-1$
		props.put("filename", 		mdProps.getFilename()); //$NON-NLS-1$
		props.put("lastmodified", 	mdProps.getVersion()); //$NON-NLS-1$
		props.put("revision",		mdProps.getRevision()); //$NON-NLS-1$
		props.store(fos, null);
		fos.flush();
		fos.close();	
	}
	
	public static void setGuvnorMetadataProps(IPath controlledFile,
			                                 GuvnorMetadataProps mdProps) throws Exception {
		IWorkspaceRoot root = Activator.getDefault().getWorkspace().getRoot();
		IFolder mdFolder = root.getFolder(
							controlledFile.removeLastSegments(1).append(".guvnorinfo")); //$NON-NLS-1$
		if (!mdFolder.exists()) {
			mdFolder.create(true, true, null);
		}
		IFile mdFile = root.getFile(
						mdFolder.getFullPath().append("." + controlledFile.lastSegment())); //$NON-NLS-1$
		Properties props = new Properties();
		if (!mdFile.exists()) {
			mdFile.create(new ByteArrayInputStream(new byte[] {}), true, null);
		} else {
			props.load(mdFile.getContents());
		}
		if (mdProps.getRepository() != null) {
			props.put("repository", mdProps.getRepository()); //$NON-NLS-1$
		}
		if (mdProps.getFullpath() != null) {
			props.put("fullpath", mdProps.getFullpath()); //$NON-NLS-1$
		}
		if (mdProps.getFilename() != null) {
			props.put("filename", mdProps.getFilename()); //$NON-NLS-1$
		}
		if (mdProps.getVersion() != null) {
			props.put("lastmodified", mdProps.getVersion()); //$NON-NLS-1$
		}
		if (mdProps.getRevision() != null) {
			props.put("revision", mdProps.getRevision()); //$NON-NLS-1$
		}
		OutputStream os = new FileOutputStream(
							new File(mdFile.getLocation().toOSString()));
		props.store(os, null);
		os.flush();
		os.close();
		mdFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
	}
	
	public static IPath createGuvnorMetadataLocation(String rootPath) throws Exception {
		IPath path = new Path(rootPath + File.separator + ".guvnorinfo"); //$NON-NLS-1$
		if (!path.toFile().exists()) {
			if (!path.toFile().mkdir()) {
				throw new Exception("Could not create directory " + path.toOSString()); //$NON-NLS-1$
			}
		}
		return path;
	}
	
	public static File getGuvnorMetadataFile(String path, String fname) {
		return new File(path + File.separator + "." + fname); //$NON-NLS-1$
	}
	
	public static IFile getGuvnorControlledResource(IResource resource) throws Exception {
		IFile res = null;
		if (resource instanceof IFile) {
			Properties props = new Properties();
			props.load(((IFile)resource).getContents());
			if (props.getProperty("filename") != null) { //$NON-NLS-1$
				res = (IFile)Activator.getDefault().getWorkspace().
								getRoot().findMember(resource.getFullPath().
										removeLastSegments(2).append(props.getProperty("filename"))); //$NON-NLS-1$
			}
		}
		return res;
	}
	
	public static boolean isGuvnorMetadata(IResource resource) {
		return resource.getFullPath().removeLastSegments(1).
							toOSString().endsWith(".guvnorinfo"); //$NON-NLS-1$
	}
	
	public static void markCurrentGuvnorResource(IResource resource) throws CoreException {
		resource.setPersistentProperty(generateQualifiedName("version"), "current"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static void markExpiredGuvnorResource(IResource resource) throws CoreException {
		resource.setPersistentProperty(generateQualifiedName("version"), "expired"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static String getGuvnorResourceProperty(IResource resource) throws CoreException {
		return resource.getPersistentProperty(generateQualifiedName("version")); //$NON-NLS-1$
	}
	
	public static boolean isGuvnorResourceCurrent(IResource resource) throws CoreException {
		String ver = resource.getPersistentProperty(generateQualifiedName("version")); //$NON-NLS-1$
		if (ver == null) {
			return false;
		}
		return ver.equals("current"); //$NON-NLS-1$
	}
	
	private static QualifiedName generateQualifiedName(String attr) {
		return new QualifiedName("org.guvnor.tools", attr);  //$NON-NLS-1$
	}
	
	public static ResourceHistoryEntry[] parseHistoryProperties(Properties entryProps) {
		ResourceHistoryEntry[] entries = new ResourceHistoryEntry[entryProps.size()];
		Enumeration<Object> en = entryProps.keys();
		for (int i = 0; i < entryProps.size(); i++) {
			String oneRevision = (String)en.nextElement();
			String val = entryProps.getProperty(oneRevision);
			StringTokenizer tokens = new StringTokenizer(val, ","); //$NON-NLS-1$
			String verDate = null;
			String author = null;
			String comment = null;
			try {
				verDate = tokens.nextToken();
				author = tokens.nextToken();
				comment = tokens.nextToken();
			} catch (NoSuchElementException e) {
				// Don't care if some fields are missing
			}
			entries[i] = new ResourceHistoryEntry(oneRevision, verDate, author, comment);
		}
		return entries;
	}
}