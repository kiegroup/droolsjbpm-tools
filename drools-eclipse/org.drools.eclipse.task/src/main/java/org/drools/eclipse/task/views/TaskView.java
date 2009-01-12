package org.drools.eclipse.task.views;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.drools.eclipse.task.Activator;
import org.drools.eclipse.task.preferences.DroolsTaskConstants;
import org.drools.process.workitem.wsht.BlockingAddTaskResponseHandler;
import org.drools.task.Status;
import org.drools.task.User;
import org.drools.task.query.TaskSummary;
import org.drools.task.service.MinaTaskClient;
import org.drools.task.service.TaskClientHandler;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class TaskView extends ViewPart {
	
	private static final String NAME_COLUMN = "Name";
	private static final String STATUS_COLUMN = "Status";
	private static final String OWNER_COLUMN = "Owner";
	private static final String CREATED_COLUMN = "Created";
	private static final String COMMENT_COLUMN = "Comment";

	private static String[] columnNames = new String[] { 
		NAME_COLUMN, 
		STATUS_COLUMN,
		OWNER_COLUMN,
		CREATED_COLUMN,
		COMMENT_COLUMN
	};
	
	private static Map<Status, String> STATUSSES;
	static {
		STATUSSES = new HashMap<Status, String>();
		STATUSSES.put(Status.Created, "Created");
		STATUSSES.put(Status.Completed, "Completed");
		STATUSSES.put(Status.Error, "Error");
		STATUSSES.put(Status.Exited, "Exited");
		STATUSSES.put(Status.Failed, "Failed");
		STATUSSES.put(Status.InProgress, "InProgress");
		STATUSSES.put(Status.Obsolete, "Obsolete");
		STATUSSES.put(Status.Ready, "Ready");
		STATUSSES.put(Status.Reserved, "Reserved");
		STATUSSES.put(Status.Suspended, "Suspended");
	}
	
	private String ipAddress = "127.0.0.1";
	private int port = 9123;
	private String language = "en-UK";
	
	private Text userNameText;
	private Table table;
	private TableViewer tableViewer;
	private Action refreshAction;
	private Button claimButton;
	private Button startButton;
	private Button stopButton;
	private Button releaseButton;
	private Button suspendButton;
	private Button resumeButton;
	private Button skipButton;
	private Button completeButton;
	private Button failButton;
	
	private MinaTaskClient client;

	private class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		@SuppressWarnings({ "unchecked" })
		public Object[] getElements(Object parent) {
			if (parent instanceof List) {
				List<TaskSummary> tasks = (List<TaskSummary>) parent;
				return tasks.toArray();
			}
			return new String[0];
		}
	}
	
	private class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			if (obj instanceof TaskSummary) {
				TaskSummary taskSummary = (TaskSummary) obj;
				switch (index) {
					case 0: 
						return taskSummary.getName();
					case 1:
						Status status = taskSummary.getStatus(); 
						return status == null ? null : STATUSSES.get(status);
					case 2:
						User user = taskSummary.getActualOwner();
						if (user == null) {
							return null;
						}
						return user.getId();
					case 3:
						return DateFormat.getDateTimeInstance().format(
							taskSummary.getCreatedOn());
					case 4:
						return taskSummary.getDescription();
					default:
						throw new IllegalArgumentException(
							"Unknown column index: " + index);
				}
			}
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return null;
		}
	}
	
	public TaskView() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
    	ipAddress = preferenceStore.getString(DroolsTaskConstants.SERVER_IP_ADDRESS);
    	port = preferenceStore.getInt(DroolsTaskConstants.SERVER_PORT);
    	language = preferenceStore.getString(DroolsTaskConstants.LANGUAGE);
    	preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (DroolsTaskConstants.SERVER_IP_ADDRESS.equals(event.getProperty())) {
					ipAddress = (String) event.getNewValue();
				} else if (DroolsTaskConstants.SERVER_PORT.equals(event.getProperty())) {
					port = (Integer) event.getNewValue();
				} else if (DroolsTaskConstants.LANGUAGE.equals(event.getProperty())) {
					language = (String) event.getNewValue();
				}
			}
    	});
	}
	
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(11, false));
		Label userNameLabel = new Label(parent, SWT.NONE);
		userNameLabel.setText("UserId");
		userNameText = new Text(parent, SWT.NONE);
		GridData layoutData = new GridData();
		layoutData.horizontalSpan = 8;
		layoutData.minimumWidth = 120;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL_HORIZONTAL;
		userNameText.setLayoutData(layoutData);
		Button refresh = new Button(parent, SWT.PUSH | SWT.CENTER);
		refresh.setText("Refresh");
		refresh.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				refresh();
			}
		});
		Button create = new Button(parent, SWT.PUSH | SWT.CENTER);
		create.setText("Create");
		create.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createTask();
			}
		});
		createTable(parent);
		createTableViewer();
		tableViewer.setContentProvider(new ViewContentProvider());
		tableViewer.setLabelProvider(new ViewLabelProvider());
		tableViewer.setInput(getViewSite());
		createButtons(parent);
		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}

	/**
	 * Create the Table
	 */
	private void createTable(Composite parent) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
			| SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
		table = new Table(parent, style);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 11;
		table.setLayoutData(gridData);		
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		// 1st column = task name
		TableColumn column = new TableColumn(table, SWT.LEFT, 0);		
		column.setText("Name");
		column.setWidth(300);
		// 2nd column = task status
		column = new TableColumn(table, SWT.LEFT, 1);
		column.setText("Status");
		column.setWidth(100);
		// 3rd column = task owner
		column = new TableColumn(table, SWT.LEFT, 2);
		column.setText("Owner");
		column.setWidth(100);
		// 4th column = expiration time 
		column = new TableColumn(table, SWT.CENTER, 3);
		column.setText("Comment");
		column.setWidth(120);
		// 5th column = created 
		column = new TableColumn(table, SWT.CENTER, 3);
		column.setText("Created On");
		column.setWidth(120);
	}

	private void createTableViewer() {
		tableViewer = new TableViewer(table);
		tableViewer.setUseHashlookup(true);
		tableViewer.setColumnProperties(columnNames);
	}
	
	private void createButtons(Composite parent) {
		claimButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		claimButton.setText("Claim");
		claimButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				claim();
			}
		});
		claimButton.setEnabled(false);
		startButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		startButton.setText("Start");
		startButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				start();
			}
		});
		startButton.setEnabled(false);
		stopButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		stopButton.setText("Stop");
		stopButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				stop();
			}
		});
		stopButton.setEnabled(false);
		releaseButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		releaseButton.setText("Release");
		releaseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				release();
			}
		});
		releaseButton.setEnabled(false);
		suspendButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		suspendButton.setText("Suspend");
		suspendButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				suspend();
			}
		});
		suspendButton.setEnabled(false);
		resumeButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		resumeButton.setText("Resume");
		resumeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resume();
			}
		});
		resumeButton.setEnabled(false);
		skipButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		skipButton.setText("Skip");
		skipButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				skip();
			}
		});
		skipButton.setEnabled(false);
		completeButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		completeButton.setText("Complete");
		completeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				complete();
			}
		});
		completeButton.setEnabled(false);
		failButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		failButton.setText("Fail");
		failButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fail();
			}
		});
		failButton.setEnabled(false);
		GridData layoutData = new GridData();
		layoutData.horizontalSpan = 2;
		layoutData.horizontalAlignment = SWT.BEGINNING;
		failButton.setLayoutData(layoutData);
		table.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				System.out.println(e);
			}
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});
	}
	
	private void updateButtons() {
		boolean selected = table.getSelectionCount() == 1;
		TaskSummary task = getSelectedTask();
		String userId = getUserId();
		claimButton.setEnabled(selected && Status.Created.equals(task.getStatus()));
		startButton.setEnabled(selected && 
			(Status.Ready.equals(task.getStatus()) ||
				(Status.Reserved.equals(task.getStatus())
					&& userId.equals(task.getActualOwner().getId()))));
		stopButton.setEnabled(selected && Status.InProgress.equals(task.getStatus())
			&& userId.equals(task.getActualOwner().getId()));
		releaseButton.setEnabled(selected && 
			(Status.Reserved.equals(task.getStatus()) || Status.InProgress.equals(task.getStatus()))
				&& userId.equals(task.getActualOwner().getId()));
		suspendButton.setEnabled(selected && 
			(Status.Ready.equals(task.getStatus()) || 
				((Status.Reserved.equals(task.getStatus()) || Status.InProgress.equals(task.getStatus()))
					&& userId.equals(task.getActualOwner().getId()))));
		// TODO only actual owner if previousStatus = reserved or inProgress
		resumeButton.setEnabled(selected && Status.Suspended.equals(task.getStatus()));
		// TODO only initiator if state Created
		skipButton.setEnabled(selected && task.isSkipable() &&
			(Status.Created.equals(task.getStatus()) && Status.Ready.equals(task.getStatus()) || 
				((Status.Reserved.equals(task.getStatus()) || Status.InProgress.equals(task.getStatus())) 
					&& userId.equals(task.getActualOwner().getId()))));
		completeButton.setEnabled(selected && Status.InProgress.equals(task.getStatus())
			&& userId.equals(task.getActualOwner().getId()));
		failButton.setEnabled(selected && Status.InProgress.equals(task.getStatus())
			&& userId.equals(task.getActualOwner().getId()));
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				TaskView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(tableViewer.getControl());
		tableViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(refreshAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(refreshAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
	}

	private void makeActions() {
		refreshAction = new Action() {
			public void run() {
				refresh();
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Refresh the task list");
		refreshAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(
			tableViewer.getControl().getShell(), "Task View", message);
	}

	public void setFocus() {
		tableViewer.getControl().setFocus();
	}
	
	private void refresh() {
		String userId = getUserId();
		if (userId == null) {
			return;
		}
		
		MinaTaskClient client = getTaskClient();
		if (client == null) {
			return;
		}
		
		try {
			BlockingTaskSummaryResponseHandler responseHandler = new BlockingTaskSummaryResponseHandler();
			client.getTasksAssignedAsPotentialOwner(userId, language, responseHandler);
	        List<TaskSummary> tasks = responseHandler.getResults();
	        tableViewer.setInput(tasks);
	        tableViewer.refresh();
	        tableViewer.setSelection(null);
	        updateButtons();
		} catch (TimeoutException e) {
			showMessage("Could not connect to task server, refresh first.");
			client.disconnect();
			this.client = null;
	        tableViewer.setInput(new ArrayList<TaskSummary>());
	        tableViewer.refresh();
	        tableViewer.setSelection(null);
		}
	}
	
	private void createTask() {
		NewTaskDialog dialog = new NewTaskDialog(getSite().getShell());
		int result = dialog.open();
		if (result == Dialog.OK) {
			MinaTaskClient client = getTaskClient();
			if (client == null) {
				return;
			}
			BlockingAddTaskResponseHandler responseHandler = new BlockingAddTaskResponseHandler();
			client.addTask(dialog.getTask(), dialog.getContent(), responseHandler);
			responseHandler.waitTillDone(5000);
			refresh();
		}
	}
	
	public void claim() {
		MinaTaskClient client = getTaskClient();
		if (client == null) {
			return;
		}
		
		String userId = getUserId();
		if (userId == null) {
			return;
		}
		
		TaskSummary taskSummary = getSelectedTask();
		if (taskSummary == null) {
			return;
		}
		
		BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
		client.claim(taskSummary.getId(), userId, responseHandler);
		responseHandler.waitTillDone(3000);
        refresh();
	}
	
	public void start() {
		MinaTaskClient client = getTaskClient();
		if (client == null) {
			return;
		}
		
		String userId = getUserId();
		if (userId == null) {
			return;
		}
		
		TaskSummary taskSummary = getSelectedTask();
		if (taskSummary == null) {
			return;
		}
		
		BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
		client.start(taskSummary.getId(), userId, responseHandler);
		responseHandler.waitTillDone(3000);
        refresh();
	}
	
	public void stop() {
		MinaTaskClient client = getTaskClient();
		if (client == null) {
			return;
		}
		
		String userId = getUserId();
		if (userId == null) {
			return;
		}
		
		TaskSummary taskSummary = getSelectedTask();
		if (taskSummary == null) {
			return;
		}
		
		BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
		client.stop(taskSummary.getId(), userId, responseHandler);
		responseHandler.waitTillDone(3000);
        refresh();
	}
	
	public void release() {
		MinaTaskClient client = getTaskClient();
		if (client == null) {
			return;
		}
		
		String userId = getUserId();
		if (userId == null) {
			return;
		}
		
		TaskSummary taskSummary = getSelectedTask();
		if (taskSummary == null) {
			return;
		}
		
		BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
		client.release(taskSummary.getId(), userId, responseHandler);
		responseHandler.waitTillDone(3000);
        refresh();
	}
	
	public void suspend() {
		MinaTaskClient client = getTaskClient();
		if (client == null) {
			return;
		}
		
		String userId = getUserId();
		if (userId == null) {
			return;
		}
		
		TaskSummary taskSummary = getSelectedTask();
		if (taskSummary == null) {
			return;
		}
		
		BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
		client.suspend(taskSummary.getId(), userId, responseHandler);
		responseHandler.waitTillDone(3000);
        refresh();
	}
	
	public void resume() {
		MinaTaskClient client = getTaskClient();
		if (client == null) {
			return;
		}
		
		String userId = getUserId();
		if (userId == null) {
			return;
		}
		
		TaskSummary taskSummary = getSelectedTask();
		if (taskSummary == null) {
			return;
		}
		
		BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
		client.resume(taskSummary.getId(), userId, responseHandler);
		responseHandler.waitTillDone(3000);
        refresh();
	}
	
	public void skip() {
		MinaTaskClient client = getTaskClient();
		if (client == null) {
			return;
		}
		
		String userId = getUserId();
		if (userId == null) {
			return;
		}
		
		TaskSummary taskSummary = getSelectedTask();
		if (taskSummary == null) {
			return;
		}
		
		BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
		client.skip(taskSummary.getId(), userId, responseHandler);
		responseHandler.waitTillDone(3000);
        refresh();
	}
	
	public void complete() {
		MinaTaskClient client = getTaskClient();
		if (client == null) {
			return;
		}
		
		String userId = getUserId();
		if (userId == null) {
			return;
		}
		
		TaskSummary taskSummary = getSelectedTask();
		if (taskSummary == null) {
			return;
		}
		
		BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
		client.complete(taskSummary.getId(), userId, null, responseHandler);
		responseHandler.waitTillDone(3000);
        refresh();
	}
	
	public void fail() {
		MinaTaskClient client = getTaskClient();
		if (client == null) {
			return;
		}
		
		String userId = getUserId();
		if (userId == null) {
			return;
		}
		
		TaskSummary taskSummary = getSelectedTask();
		if (taskSummary == null) {
			return;
		}
		
		BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
		client.fail(taskSummary.getId(), userId, null, responseHandler);
		responseHandler.waitTillDone(3000);
        refresh();
	}
	
	private MinaTaskClient getTaskClient() {
		if (client == null) {
			client = new MinaTaskClient(
				"org.drools.eclipse.task.views.TaskView", new TaskClientHandler());
			NioSocketConnector connector = new NioSocketConnector();
			SocketAddress address = new InetSocketAddress(ipAddress, port);
			boolean connected = client.connect(connector, address);
			if (!connected) {
				showMessage("Could not connect to task server: " + ipAddress + " [port " + port + "]");
				client = null;
			}
		}
		return client;
	}
	
	public void dispose() {
		if (client != null) {
			client.disconnect();
		}
		super.dispose();
	}
	
	private String getUserId() {
		return userNameText.getText();
	}
	
	private TaskSummary getSelectedTask() {
		ISelection selection = tableViewer.getSelection();
		if (selection instanceof StructuredSelection) {
			Object selected = ((StructuredSelection) selection).getFirstElement();
			if (selected instanceof TaskSummary) {
				return (TaskSummary) selected;
			}
		}
		return null;
	}
	
}