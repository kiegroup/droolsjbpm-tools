package org.drools.ide.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;

public class DroolsClasspathContainer implements IClasspathContainer {

    IClasspathEntry droolsLibraryEntries[];
    IPath path;
    IJavaProject javaProject;

    public DroolsClasspathContainer(IJavaProject project, IPath path) {
        javaProject = null;
        javaProject = project;
        this.path = path;
    }

    public IClasspathEntry[] getClasspathEntries() {
        if (droolsLibraryEntries == null) {
            droolsLibraryEntries = createDroolsLibraryEntries(javaProject);
        }
        return droolsLibraryEntries;
    }

    public String getDescription() {
        return "Drools Library [3.0]";
    }

    public int getKind() {
        return 1;
    }

    public IPath getPath() {
        return path;
    }

    private IClasspathEntry[] createDroolsLibraryEntries(IJavaProject project) {
        List jarNames = getJarNames();
        List list = new ArrayList();
        for (int i = 0; i < jarNames.size(); i++) {
            Path path = new Path((String) jarNames.get(i));
            list.add(JavaCore.newLibraryEntry(
                path, path, null));
        }
        // also add jdt core jar from eclipse itself
        String pluginRootString = Platform.getInstallLocation().getURL().getPath() + "plugins/";
        File pluginRoot = new Path(pluginRootString).toFile();
        File[] files = pluginRoot.listFiles();
        for (int i = 0; i < files.length; i++) {
	        if (files[i].getAbsolutePath().contains("org.eclipse.jdt.core_")) {
	        	Path path = new Path(files[i].getAbsolutePath());
	        	list.add(JavaCore.newLibraryEntry(path, path, null));
	        }
        }
        return (IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]);
    }

    private List getJarNames() {
        String s = getDroolsLocation();
        List list = new ArrayList();
        File file = (new Path(s)).toFile();
        addJarNames(file, list);
        return list;
    }

    private void addJarNames(File file, List list) {
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
	        if (files[i].isDirectory() && files[i].getName().equals("lib")) {
	            File[] jarFiles = files[i].listFiles();
	            for (int j = 0; j < jarFiles.length; j++) {
	                if (jarFiles[j].getPath().endsWith(".jar")) {
	                    list.add(jarFiles[j].getAbsolutePath());
	                }
	            }
            }
        }
    }

    private String getDroolsLocation() {
        try {
            return FileLocator.toFileURL(Platform.getBundle("org.drools.ide")
                .getEntry("/")).getFile().toString();
        } catch (IOException e) {
            DroolsIDEPlugin.log(e);
        }
        return null;
    }
}