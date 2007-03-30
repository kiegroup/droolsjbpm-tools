package org.drools.eclipse.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class DroolsClasspathContainerInitializer extends ClasspathContainerInitializer {

    public void initialize(IPath ipath, IJavaProject project) throws CoreException {
        DroolsClasspathContainer container =
            new DroolsClasspathContainer(project, ipath);
        JavaCore.setClasspathContainer(ipath, new IJavaProject[] { project },
            new IClasspathContainer[] { container }, null);
    }
}
