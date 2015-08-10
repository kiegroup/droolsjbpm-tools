package org.kie.eclipse.navigator.view.actions.repository;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.egit.core.op.PullOperation;
import org.eclipse.egit.ui.JobFamilies;
import org.eclipse.egit.ui.internal.pull.PullResultDialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.kie.eclipse.IKieConstants;
import org.kie.eclipse.navigator.Activator;
import org.kie.eclipse.navigator.view.content.RepositoryNode;
import org.kie.eclipse.server.IKieRepositoryHandler;
import org.kie.eclipse.server.IKieServerHandler;
import org.kie.eclipse.server.IKieServiceDelegate;
import org.kie.eclipse.utils.PreferencesUtils;

/**
 * UI wrapper for {@link PullOperation}
 */
public class PullOperationUI extends JobChangeAdapter implements IKieConstants {
	private static final IStatus NOT_TRIED_STATUS = new Status(IStatus.ERROR, Activator.getDefault().PLUGIN_ID, "Not tried");

	private final Repository repository;
	private final Shell shell;
	private final String repoName;
	private final RepositoryNode repoNode;
	/**
	 * Holds the number of PullOperationUIs that need to complete before the
	 * pull results can be shown. The number includes this instance of
	 * PullOperationUI and all subtasks spawned by it.
	 */
	private final AtomicInteger tasksToWaitFor = new AtomicInteger(1);

	/** pull results per repository */
	protected final Map<Repository, Object> results = Collections.synchronizedMap(new LinkedHashMap<Repository, Object>());

	private final PullOperation pullOperation;

	/**
	 * @param repositories
	 */
	public PullOperationUI(RepositoryNode container) {
		this.repoNode = container;
		final IKieRepositoryHandler handler = (IKieRepositoryHandler) container.getHandler();
		final IKieServerHandler serverHandler = (IKieServerHandler) handler.getRoot();
		final IKieServiceDelegate delegate = handler.getDelegate();
		this.repository = handler.getRepository();
		this.shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		this.repoName = handler.getName();

		String host = delegate.getServer().getHost();
		int port = delegate.getGitPort();
		String username = delegate.getUsername();
		String password = delegate.getPassword();
		URIish uri = PreferencesUtils.getRepoURI(host, port, username, repoName);
		// LoginDialog dlg = new LoginDialog(shell, uri);
		// dlg.setUsername(username);
		// dlg.setPassword(password);
		// if (dlg.open() != Dialog.OK)
		// return;
		// username = dlg.getUsername();
		// password = dlg.getPassword();
		uri = PreferencesUtils.getRepoURI(host, port, username, repoName);
		final CredentialsProvider credentialsProvider = new KieCredentialsProvider(serverHandler, username, password);

		int timeout = serverHandler.getPreference(PREF_REMOTE_TIMEOUT, 60);
		Set<Repository> repos = new HashSet<Repository>();
		repos.add(repository);
		pullOperation = new PullOperation(repos, timeout);
		pullOperation.setCredentialsProvider(credentialsProvider);
		results.put(repository, NOT_TRIED_STATUS);
	}

	/**
	 * Starts this operation asynchronously
	 */
	public void start() {
		start(this);
	}

	private void start(IJobChangeListener jobChangeListener) {
		// figure out a job name
		String jobName;
		String shortBranchName;
		try {
			shortBranchName = repository.getBranch();
		}
		catch (IOException e) {
			// ignore here
			shortBranchName = ""; //$NON-NLS-1$
		}
		jobName = NLS.bind("Pulling Branch {0} of Repository {1}", shortBranchName, repoName);

		Job job = new WorkspaceJob(jobName) {

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				execute(monitor);
				// we always return OK and handle display of errors on our own
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(Object family) {
				if (JobFamilies.PULL.equals(family))
					return true;
				return super.belongsTo(family);
			}
		};
		job.setRule(null);
		job.setUser(true);
		job.addJobChangeListener(jobChangeListener);
		job.schedule();
	}

	/**
	 * Starts this operation synchronously.
	 *
	 * @param monitor
	 */
	@SuppressWarnings("restriction")
	public void execute(IProgressMonitor monitor) {
		try {
			pullOperation.execute(monitor);
			results.putAll(pullOperation.getResults());
		}
		catch (CoreException e) {
			if (e.getStatus().getSeverity() == IStatus.CANCEL)
				results.putAll(pullOperation.getResults());
			else
				repoNode.handleException((Throwable) e);
		}
	}

	@Override
	public void done(IJobChangeEvent event) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				Map<Repository, Object> res = new LinkedHashMap<Repository, Object>(PullOperationUI.this.results);
				handlePullResults(res);
			}
		});
	}

	/**
	 * Post-process the pull results, allowing the user to deal with uncommitted
	 * changes and re-pull if the initial pull failed because of these changes
	 *
	 * @param resultsMap a copy of the initial pull results
	 * @param shell
	 */
	private void handlePullResults(final Map<Repository, Object> resultsMap) {
		if (tasksToWaitFor.decrementAndGet() == 0 && !results.isEmpty())
			showResults();
	}

	private void showResults() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				showResults(shell);
			}
		});
	}

	private void showResults(final Shell shell) {
		if (this.results.isEmpty())
			// shouldn't really happen, but just in case...
			return;
		Entry<Repository, Object> entry = this.results.entrySet().iterator().next();
		if (entry.getValue() instanceof PullResult)
			new PullResultDialog(shell, entry.getKey(), (PullResult) entry.getValue()).open();
		else {
			IStatus status = (IStatus) entry.getValue();
			if (status == NOT_TRIED_STATUS) {
				MessageDialog.openInformation(shell, "Pull Canceled", "The pull operation was canceled.");
			}
			else if (status.getException() instanceof TransportException) {
				ErrorDialog.openError(shell, "Pull Failed", "Git connection problem.\n"
						+ "\n\nMaybe you are offline or behind a proxy.\n"
						+ "Check your network connection and proxy configuration.", status);
			}
			else
				repoNode.handleException(status.getException());
		}
	}
}
