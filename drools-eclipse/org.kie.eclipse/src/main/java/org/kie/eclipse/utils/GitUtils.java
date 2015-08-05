package org.kie.eclipse.utils;

import java.io.File;
import java.util.Set;

import org.eclipse.egit.core.RepositoryUtil;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.util.FS;

public class GitUtils {
	
	private GitUtils() {
	}

	public static RepositoryUtil getRepositoryUtil() {
		return org.eclipse.egit.ui.Activator.getDefault().getRepositoryUtil();
	}

	public static void findGitDirsRecursive(File repoRoot, Set<File> gitDirs, boolean lookForNestedRepositories) {
	
		if (!repoRoot.exists() || !repoRoot.isDirectory()) {
			return;
		}
		File[] children = repoRoot.listFiles();
		
		// simply ignore null
		if (children == null)
			return;
	
		for (File child : children) {
			if (!child.isDirectory())
				continue;
	
			if (FileKey.isGitRepository(child, FS.DETECTED)) {
				gitDirs.add(child);
			}
			else if (FileKey.isGitRepository(new File(child,
					Constants.DOT_GIT), FS.DETECTED)) {
				gitDirs.add(new File(child, Constants.DOT_GIT));
			}
			else if (lookForNestedRepositories) {
				findGitDirsRecursive(child, gitDirs, lookForNestedRepositories);
			}
		}
	}
}
