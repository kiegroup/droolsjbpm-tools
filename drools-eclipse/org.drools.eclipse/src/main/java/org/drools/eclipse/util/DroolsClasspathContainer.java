package org.drools.eclipse.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.drools.eclipse.preferences.DroolsRuntimesBlock.DroolsRuntime;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.ClasspathAccessRule;
import org.eclipse.jdt.internal.core.ClasspathEntry;

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
        return "Drools Library";
    }

    public int getKind() {
        return 1;
    }

    public IPath getPath() {
        return path;
    }

    private IClasspathEntry[] createDroolsLibraryEntries(IJavaProject project) {
    	int internalAPI = DroolsEclipsePlugin.getDefault()
    		.getPluginPreferences().getInt(IDroolsConstants.INTERNAL_API);
        List jarNames = getJarNames(project);
        List list = new ArrayList();
        for (int i = 0; i < jarNames.size(); i++) {
        	String jarName = (String) jarNames.get(i);
        	Path path = new Path(jarName);
        	if (internalAPI != 0) {
		        if (jarName.contains("drools-api")) {
		        	list.add(JavaCore.newLibraryEntry(path, path, null));
		        } else {
		        	IAccessRule[] accessRules = new IAccessRule[1];
		            accessRules[0] = new ClasspathAccessRule(new Path("**"), internalAPI);
		            list.add(JavaCore.newLibraryEntry(
		                path, path, null, accessRules, ClasspathEntry.NO_EXTRA_ATTRIBUTES, false));
		        }
        	}
        }
        // also add jdt core jar from eclipse itself
//        String pluginRootString = Platform.getInstallLocation().getURL().getPath() + "plugins/";
//        File pluginRoot = new Path(pluginRootString).toFile();
//        File[] files = pluginRoot.listFiles();
//        for (int i = 0; i < files.length; i++) {
//	        if (files[i].getAbsolutePath().indexOf("org.eclipse.jdt.core_3.4") > -1) {
//	        	Path path = new Path(files[i].getAbsolutePath());
//	        	list.add(JavaCore.newLibraryEntry(path, path, null));
//	        	break;
//	        }
//        }
        return (IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]);
    }

    private List getJarNames(IJavaProject project) {
        String s = DroolsRuntimeManager.getDroolsRuntimePath(project.getProject());
        List list = new ArrayList();
        if (s != null) {
	        File file = (new Path(s)).toFile();
	        addJarNames(file, list);
        }
        return list;
    }

    private void addJarNames(File file, List list) {
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
        	if (files[i].isDirectory() && "lib".equals(files[i].getName())) {
            	addJarNames(files[i], list);
            } else if (files[i].getPath().endsWith(".jar")) {
                list.add(files[i].getAbsolutePath());
            }
        }
    }

}