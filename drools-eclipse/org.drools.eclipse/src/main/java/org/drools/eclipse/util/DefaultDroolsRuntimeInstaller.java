package org.drools.eclipse.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jgit.fnmatch.FileNameMatcher;

public class DefaultDroolsRuntimeInstaller implements IDroolsRuntimeInstaller {

	@Override
	public String install(IProject project, IProgressMonitor monitor) {
		FileNameMatcher fn;
		return null;
	}
    
	protected File downloadURL(String filename, IProgressMonitor monitor) throws IOException {
		URL url = new URL(filename);
		URLConnection conn = url.openConnection();
		if (conn instanceof HttpURLConnection)
			((HttpURLConnection)conn).setRequestMethod("GET");
		
		File file = null;
		int length = conn.getContentLength();
		final int buffersize = 1024;
    	SubProgressMonitor spm = new SubProgressMonitor(monitor, length/buffersize);
		InputStream istream = conn.getInputStream();
		OutputStream ostream = null;
		try {
			spm.beginTask("Downloading "+url.getFile()+" from "+url.getHost(), length/buffersize);
			file = File.createTempFile(filename, null);
			if (istream!=null) {
				ostream = new FileOutputStream(file);
	
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
		return file;
	}

	private void extractZipFile(File file, IProject project) throws IOException {
		ZipFile zip = new ZipFile(file);
	    InputStream is = null;
	    FileOutputStream fos = null;
		try {
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
			    ZipEntry entry = entries.nextElement();
			    IPath path = project.getLocation().append(entry.getName());
			    File entryFile = new File(path.toString());
			    if (entry.isDirectory()) { // if its a directory, create it
			        entryFile.mkdir();
			        continue;
			    }
			    entryFile.getParentFile().mkdirs();
			    is = zip.getInputStream(entry);
			    fos = new FileOutputStream(entryFile);
			    while (is.available() > 0) {  // write contents of 'is' to 'fos'
			        fos.write(is.read());
			    }
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			zip.close();
			if (fos!=null)
				fos.close();
			if (is!=null)
				is.close();
		}
	}
	
	private void extractJarFile(File file, IProject project) throws IOException {
		JarFile jar = new java.util.jar.JarFile(file);
	    InputStream is = null;
	    FileOutputStream fos = null;
		try {
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
			    JarEntry entry = entries.nextElement();
			    IPath path = project.getLocation().append(entry.getName());
			    File entryFile = new File(path.toString());
			    if (entry.isDirectory()) { // if its a directory, create it
			        entryFile.mkdir();
			        continue;
			    }
			    entryFile.getParentFile().mkdirs();
			    is = jar.getInputStream(entry);
			    fos = new FileOutputStream(entryFile);
			    while (is.available() > 0) {  // write contents of 'is' to 'fos'
			        fos.write(is.read());
			    }
			}
		}
		finally {
			jar.close();
			if (fos!=null)
				fos.close();
			if (is!=null)
				is.close();
		}
	}
	
}
