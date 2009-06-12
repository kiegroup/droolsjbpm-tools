package org.drools.eclipse.util;

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.preferences.IDroolsConstants;
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
        String[] jarNames = getJarNames(project);
        List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
        if (jarNames != null) {
	        for (int i = 0; i < jarNames.length; i++) {
	        	Path path = new Path(jarNames[i]);
	        	if (internalAPI != 0) {
			        if (jarNames[i].contains("drools-api")) {
			        	list.add(JavaCore.newLibraryEntry(path, path, null));
			        } else {
			        	IAccessRule[] accessRules = new IAccessRule[1];
			            accessRules[0] = new ClasspathAccessRule(new Path("**"), internalAPI);
			            list.add(JavaCore.newLibraryEntry(
			                path, path, null, accessRules, ClasspathEntry.NO_EXTRA_ATTRIBUTES, false));
			        }
	        	}
	        }
        }
        return (IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]);
    }

    private String[] getJarNames(IJavaProject project) {
    	return DroolsRuntimeManager.getDroolsRuntimeJars(project.getProject());
    }

}