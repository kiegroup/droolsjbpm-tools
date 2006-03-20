package org.drools.ide.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

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
        if (file.isDirectory()) {
            File afile[] = file.listFiles();
            for (int i = 0; i < afile.length; i++)
                addJarNames(afile[i], list);

        }
        if (file.getPath().endsWith(".jar")) {
            list.add(file.getAbsolutePath());
        }
    }

    private String getDroolsLocation() {
        try {
            return Platform.asLocalURL(Platform.getBundle("org.drools.ide")
                .getEntry("/")).getFile().toString();
        } catch (IOException e) {
            DroolsIDEPlugin.log(e);
        }
        return null;
    }
}