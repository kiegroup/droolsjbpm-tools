package org.drools.eclipse.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * To detect the DRL files in the project
 * @author Lucas Amador
 *
 */
public class DRLProjectDetector {

	private List<IFile> resources;

	public DRLProjectDetector() {
		resources = new ArrayList<IFile>();
	}

	public List<IFile> detect(IProject project) throws CoreException {
		detect(project.members());
		return resources;
	}

	private void detect(IResource[] members) throws CoreException {
		for (int i = 0; i < members.length; i++) {
			if (members[i] instanceof IFolder) {
				IFolder folder = (IFolder)members[i];
				if (!folder.isDerived())
					detect(((IFolder)members[i]).members());
			}
			if (members[i] instanceof IFile) {
				IFile file = (IFile)members[i];
				if (file.getFileExtension().equalsIgnoreCase("drl"))
					if (file.isAccessible() && !file.isReadOnly() && !file.isDerived())
						resources.add(file);
			}
		}
	}

}
