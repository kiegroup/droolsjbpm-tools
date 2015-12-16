/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.eclipse.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.ui.PreferenceConstants;

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
	
	public static boolean deleteFolder(File file) {
		File[] flist = null;
		if (file == null) {
			return false;
		}
		if (file.isFile()) {
			return file.delete();
		}
		if (!file.isDirectory()) {
			return false;
		}
		flist = file.listFiles();
		if (flist != null && flist.length > 0) {
			for (File f : flist) {
				if (!deleteFolder(f)) {
					return false;
				}
			}
		}

		return file.delete();
	}
	
	public static void extractJarFile(java.io.File jarFile, IProject project, IProgressMonitor monitor)
			throws IOException, CoreException {
		JarFile jar = new java.util.jar.JarFile(jarFile);
	    InputStream is = null;
		try {
//			System.out.println("Jar: "+jar.getName());
//			for (Entry<Object, Object> e : jar.getManifest().getMainAttributes().entrySet()) {
//				System.out.println("  "+e.getKey() + "=" + e.getValue());
//			}
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

	public static IProject getProjectHandle(String name) {
	    return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}

	public static void addJavaBuilder(IJavaProject project, IProgressMonitor monitor) throws CoreException {
	    IProjectDescription description = project.getProject().getDescription();
	    ICommand[] commands = description.getBuildSpec();
	    ICommand[] newCommands = new ICommand[commands.length + 1];
	    System.arraycopy(commands, 0, newCommands, 0, commands.length);
	
	    ICommand javaCommand = description.newCommand();
	    javaCommand.setBuilderName("org.eclipse.jdt.core.javabuilder");
	    newCommands[commands.length] = javaCommand;
	    
	    description.setBuildSpec(newCommands);
	    project.getProject().setDescription(description, monitor);
	}

	public static void addMavenBuilder(IJavaProject project, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
	
		ICommand mavenCommand = description.newCommand();
		mavenCommand.setBuilderName("org.eclipse.m2e.core.maven2Builder");
		newCommands[commands.length] = mavenCommand;
	
		description.setBuildSpec(newCommands);
		project.getProject().setDescription(description, monitor);
	}

	public static void addJavaNature(IProjectDescription projectDescription) {
	    List<String> list = new ArrayList<String>();
	    list.addAll(Arrays.asList(projectDescription.getNatureIds()));
	    list.add("org.eclipse.jdt.core.javanature");
	    projectDescription.setNatureIds((String[]) list.toArray(new String[list.size()]));
	}

	public static void addMavenNature(IProjectDescription projectDescription) {
	    List<String> list = new ArrayList<String>();
	    list.addAll(Arrays.asList(projectDescription.getNatureIds()));
	    list.add("org.eclipse.m2e.core.maven2Nature");
	    projectDescription.setNatureIds((String[]) list.toArray(new String[list.size()]));
	}
    
    public static void createMavenArtifacts(IJavaProject project, String groupId, String artifactId, String version, IProgressMonitor monitor) {
        try {
        	String projectName = project.getProject().getName();
        	if (groupId==null || groupId.isEmpty())
        		groupId = projectName + ".group.id";
            if (artifactId==null || artifactId.isEmpty())
            	artifactId = projectName + ".id";
            if (version==null || version.isEmpty())
            	version = "1.0";
			createProjectFile(project, monitor, generatePomProperties(groupId, artifactId, version), "src/main/resources/META-INF/maven", "pom.properties");
            createProjectFile(project, monitor, generatePom(groupId, artifactId, version), null, "pom.xml");
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
    }
    
    public static void createKJarArtifacts(IJavaProject project, IProgressMonitor monitor) {
        try {
        	createProjectFile(project, monitor, generateKModule(), "src/main/resources/META-INF", "kmodule.xml");
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
    }

    public static void createGitIgnore(IJavaProject project, IProgressMonitor monitor) {
        try {
        	createProjectFile(project, monitor, generateGitIGnore(), null, ".gitignore");
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
    }
    
    public static InputStream generateGitIGnore() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(".gitignore\n");
    	sb.append(".classpath\n");
    	sb.append(".project\n");
    	sb.append(".settings/\n");
        return new ByteArrayInputStream(sb.toString().getBytes());
    }
    
    public static InputStream generateKModule() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    	sb.append("<kmodule xmlns=\"http://jboss.org/kie/6.0.0/kmodule\">\n");
    	
//    	sb.append("    <kbase name=\"rules\" packages=\"rules\">\n");
//    	sb.append("        <ksession name=\"ksession-rules\"/>\n");
//    	sb.append("    </kbase>\n");
//
//    	sb.append("    <kbase name=\"dtables\" packages=\"dtables\">\n");
//    	sb.append("        <ksession name=\"ksession-dtables\"/>\n");
//    	sb.append("    </kbase>\n");
//
//    	sb.append("    <kbase name=\"process\" packages=\"process\">\n");
//    	sb.append("        <ksession name=\"ksession-process\"/>\n");
//    	sb.append("    </kbase>\n");
        
        sb.append("</kmodule>\n");
    	
        return new ByteArrayInputStream(sb.toString().getBytes());
    }

	public static InputStream generatePomProperties(String groupId, String artifactId, String version) {
		String pom = "groupId=" + groupId + "\n" + "artifactId=" + artifactId
				+ "\n" + "version=" + version + "\n";
		return new ByteArrayInputStream(pom.getBytes());
	}

	public static InputStream generatePom(String groupId, String artifactId, String version) {
	    String pom =
	            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
	            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
	            "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
	            "  <modelVersion>4.0.0</modelVersion>\n" +
	            "\n" +
	            "  <groupId>" + groupId + "</groupId>\n" +
	            "  <artifactId>" + artifactId + "</artifactId>\n" +
	            "  <version>" + version + "</version>\n" +
	            "</project>\n";
	    return new ByteArrayInputStream(pom.getBytes());
	}

    public static void createOutputLocation(IJavaProject project, String folderName, IProgressMonitor monitor)
            throws JavaModelException, CoreException {
        IFolder folder = createFolder(project, folderName, monitor);
        IPath path = folder.getFullPath();
        JavaModelManager.getJavaModelManager().getPerProjectInfo(project.getProject(), true);
        project.setRawClasspath(new IClasspathEntry[0], monitor);
        project.setOutputLocation(path, monitor);
    }
    
	public static void addFolderToClasspath(IJavaProject project, String folderName, boolean create, IProgressMonitor monitor) throws JavaModelException, CoreException {
	    List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
	    list.addAll(Arrays.asList(project.getRawClasspath()));
	    IFolder folder = project.getProject().getFolder(folderName);
	    if (create || folder.exists()) {
	    	if (create)
	    		createFolder(folder, monitor);
		    IPackageFragmentRoot ipackagefragmentroot = project.getPackageFragmentRoot(folder);
		    list.add(JavaCore.newSourceEntry(ipackagefragmentroot.getPath()));
	    	project.setRawClasspath((IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]), null);
	    }
	}

	public static void addJRELibraries(IJavaProject project, IProgressMonitor monitor) throws JavaModelException {
	    List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
	    list.addAll(Arrays.asList(project.getRawClasspath()));
	    list.addAll(Arrays.asList(PreferenceConstants.getDefaultJRELibrary()));
	    project.setRawClasspath((IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]), monitor);
	}

	public static void addMavenLibraries(IJavaProject project, IProgressMonitor monitor)
	        throws JavaModelException {
		List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		list.addAll(Arrays.asList(project.getRawClasspath()));
		list.add(JavaCore.newContainerEntry(new Path("org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER")));
		project.setRawClasspath((IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]), monitor);
	}

	public static void addJUnitLibrary(IJavaProject project, IProgressMonitor monitor)
			throws JavaModelException {
		List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		list.addAll(Arrays.asList(project.getRawClasspath()));
		list.add(JavaCore.newContainerEntry(new Path("org.eclipse.jdt.junit.JUNIT_CONTAINER/4")));
		project.setRawClasspath((IClasspathEntry[]) list
		    .toArray(new IClasspathEntry[list.size()]), monitor);
	}

	public static IFolder createFolder(IJavaProject project, String folderName, IProgressMonitor monitor) throws CoreException {
		IFolder folder = project.getProject().getFolder(folderName);
		createFolder(folder, monitor);
		return folder;
	}

	public static void createFolder(IFolder folder, IProgressMonitor monitor) throws CoreException {
	    IContainer container = folder.getParent();
	    if (container != null && !container.exists()
	            && (container instanceof IFolder))
	        createFolder((IFolder) container, monitor);
	    if (!folder.exists()) {
	        folder.create(true, true, monitor);
	    }
	}

	public static void createProjectFile(IJavaProject project, IProgressMonitor monitor, InputStream inputstream, String folderName, String fileName) throws CoreException {
	    IFile file;
	    if (folderName == null) {
	        file = project.getProject().getFile(fileName);
	    } else {
	        IFolder folder = project.getProject().getFolder(folderName);
	        file = folder.getFile(fileName);
	    }
	
	    if (!file.exists()) {
	    	if (file.getParent() instanceof IFolder)
	    		createFolder((IFolder)file.getParent(), monitor);
	        file.create(inputstream, true, monitor);
	    } else {
	        file.setContents(inputstream, true, false, monitor);
	    }
	}

	public static byte[] readStream(InputStream inputstream) throws IOException {
	    byte bytes[] = (byte[]) null;
	    int i = 0;
	    byte tempBytes[] = new byte[1024];
	    for (int j = inputstream.read(tempBytes); j != -1; j = inputstream.read(tempBytes)) {
	        byte tempBytes2[] = new byte[i + j];
	        if (i > 0) {
	            System.arraycopy(bytes, 0, tempBytes2, 0, i);
	        }
	        System.arraycopy(tempBytes, 0, tempBytes2, i, j);
	        bytes = tempBytes2;
	        i += j;
	    }
	
	    return bytes;
	}
	
}
