package org.kie.eclipse.utils;

import java.io.File;
import java.net.URISyntaxException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;
import org.kie.eclipse.IKieConstants;
import org.kie.eclipse.server.IKieRepositoryHandler;
import org.kie.eclipse.server.IKieServerHandler;
import org.kie.eclipse.server.IKieServiceDelegate;

public class PreferencesUtils implements IKieConstants {

	public PreferencesUtils() {
	}

    public static String getRepoRoot(final IKieRepositoryHandler repository) {
        final IKieServerHandler server = (IKieServerHandler) repository.getRoot();

        final boolean useDefaultGitPath = server.getPreference(IKieConstants.PREF_USE_DEFAULT_GIT_PATH, false);
        final String defaultRepoRoot = getDefaultRepositoryDir();

        if (useDefaultGitPath) {
            return defaultRepoRoot;
        }

        final String repoRoot = defaultRepoRoot + File.separator + server.getPreferenceName(null).replace(IKieConstants.PREF_PATH_SEPARATOR.charAt(0), File.separator.charAt(0));
        return server.getPreference(IKieConstants.PREF_GIT_REPO_PATH, repoRoot);
    }

    public static String getRepoPath(final IKieRepositoryHandler repository) {
        return getRepoRoot(repository) + File.separator + repository.getName();
    }

    public static URIish getRepoURI(final IKieRepositoryHandler repository) {
        final IKieServiceDelegate delegate = repository.getDelegate();

        final String host = delegate.getServer().getHost();
        final int port = delegate.getGitPort();
        final String username = delegate.getUsername();
        final String potentialUsername = username == null || username.isEmpty() ? "" : username + "@";
        final String repoPath = repository.getParent().getName() + File.separator + repository.getName();

        try {
            // URI is in the form: ssh://admin@localhost:8001/spaceName/projectName
            return new URIish("ssh://" + potentialUsername + host + ":" + port + "/" + repoPath);
        } catch (final URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    //
    //
	// FIXME: As soon as I find out how to get the default git repository directory root
	// from EGit, this needs to go away - there's a bunch of code here that was copied
	// directly from the EGit RepositoryUtil class
	private static final String core_defaultRepositoryDir = "core_defaultRepositoryDir"; //$NON-NLS-1$
	private static final String deprecated_defaultRespositoryDir = "default_repository_dir"; //$NON-NLS-1$
	private static final String egitPluginId = "org.eclipse.egit.core"; //$NON-NLS-1$
	private static final String deprecatedEgitPreferences = "org.eclipse.egit.ui"; //$NON-NLS-1$

	public static String getDefaultRepositoryDir() {
		String key = core_defaultRepositoryDir;
		String dir = getDeprecatedRepoRootPreference();
		IEclipsePreferences p = InstanceScope.INSTANCE.getNode(egitPluginId);
		if (dir == null) {
			dir = p.get(key, getDefaultDefaultRepositoryDir());
		}

		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		String result;
		try {
			result = manager.performStringSubstitution(dir);
		} catch (CoreException e) {
			result = ""; //$NON-NLS-1$
		}
		if (result == null || result.isEmpty()) {
			result = ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toOSString();
		}
		return result;
	}

	private static String getDefaultDefaultRepositoryDir() {
		return new File(FS.DETECTED.userHome(), "git").getPath(); //$NON-NLS-1$
	}

	private static String getDeprecatedRepoRootPreference() {
		IEclipsePreferences p = InstanceScope.INSTANCE.getNode(deprecatedEgitPreferences);
		String value = p.get(deprecated_defaultRespositoryDir, null);
		if (value != null && value.isEmpty()) {
			value = null;
		}
		return value;
	}
}
