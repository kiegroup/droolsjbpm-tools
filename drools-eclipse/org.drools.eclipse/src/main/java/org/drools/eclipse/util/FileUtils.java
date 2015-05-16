package org.drools.eclipse.util;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

public class FileUtils {
	
	private FileUtils() { }

	public static String readFile(IFile file) throws CoreException {
	    InputStream inputStream = file.getContents();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
	    StringBuilder sb = new StringBuilder();
	    try {
	        char[] buf = new char[1024];
	        int numRead=0;
	        while((numRead=reader.read(buf)) != -1){
	        	sb.append(buf, 0, numRead);
	        }
	    } catch (IOException e) {
	        return null;
	    } finally {
	        try {
	        	if (reader != null) {
	        		reader.close();
	        	}
	        	if (inputStream != null) {
	        		inputStream.close();
	        	}
	        }
	        catch (IOException e) {
	            // Nothing
	        }
	    }
	    return sb.toString();
	}

	public static void mkdirs(IResource resource, IProgressMonitor monitor) throws CoreException {
		IContainer parent = resource.getParent();
		if (!parent.exists()) {
			mkdirs(resource.getParent(), monitor);
		}
		if (resource instanceof IFolder && !resource.exists())
			((IFolder)resource).create(true, true, monitor);
	}

	public static void extractJarFile(java.io.File jarFile, IProject project, IProgressMonitor monitor)
			throws IOException, CoreException {
		JarFile jar = new java.util.jar.JarFile(jarFile);
	    InputStream is = null;
		try {
			Enumeration<JarEntry> enumEntries = jar.entries();
			while (enumEntries.hasMoreElements()) {
			    JarEntry entry = enumEntries.nextElement();
			    if (entry.isDirectory()) {
		    		IFolder folder = project.getFolder(entry.getName());
			    	if (!folder.exists()) {
			    		folder.create(true, true, monitor);
			    	}
			    }
			    else {
				    IFile file = project.getFile(entry.getName());
				    mkdirs(file, monitor);
				    is = jar.getInputStream(entry);
				    if (file.exists())
				    	file.setContents(is, true, false, monitor);
				    else
				    	file.create(is, true, monitor);
			    }
			}
		}
		finally {
			jar.close();
			if (is!=null)
				is.close();
		}
	}

	public static java.io.File downloadFile(URL url, IProgressMonitor monitor) throws IOException {
		URLConnection conn = url.openConnection();
		if (conn instanceof HttpURLConnection)
			((HttpURLConnection)conn).setRequestMethod("GET");
		
		java.io.File jarFile = null;
		int length = conn.getContentLength();
		final int buffersize = 1024;
		SubProgressMonitor spm = new SubProgressMonitor(monitor, length/buffersize);
		InputStream istream = conn.getInputStream();
		OutputStream ostream = null;
		try {
			spm.beginTask("Downloading "+url.getFile()+" from "+url.getHost(), length/buffersize);
			jarFile = java.io.File.createTempFile(url.getFile(), null);
			if (istream!=null) {
				ostream = new FileOutputStream(jarFile);
	
				int read = 0;
				byte[] bytes = new byte[buffersize];
	
				while ((read = istream.read(bytes)) != -1) {
					ostream.write(bytes, 0, read);
					spm.worked(1);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (istream!=null)
				istream.close();
			if (ostream!=null) {
				ostream.flush();
				ostream.close();
			}
			spm.done();
		}
		return jarFile;
	}

}
