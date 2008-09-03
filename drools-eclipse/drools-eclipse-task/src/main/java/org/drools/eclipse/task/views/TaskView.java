package org.drools.eclipse.task.views;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.drools.task.Status;
import org.drools.task.User;
import org.drools.task.query.TaskSummary;
import org.drools.task.service.MinaTaskClient;
import org.drools.task.service.TaskClientHandler;
import org.drools.task.service.TaskClientHandler.TaskSummaryResponseHandler;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
	private static final String COMMENT_COLUMN = "Comment";

	private static String[] columnNames = new String[] { 
		NAME_COLUMN, 
		STATUS_COLUMN,
		OWNER_COLUMN,
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
		STATUSSES.put(Status.Obselete, "Obsolete");
		STATUSSES.put(Status.Ready, "Ready");
		STATUSSES.put(Status.Reserved, "Reserved");
		STATUSSES.put(Status.Suspended, "Suspended");
	}
	
	private String ipAddress = "127.0.0.1";
	private int port = 9123;
	
	private Text userNameText;
	private Table table;
	private TableViewer tableViewer;
	private Action refreshAction;
	private Action doubleClickAction;

	private class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		@SuppressWarnings("unchecked")
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
						return user.getDisplayName();
					case 3:
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
	
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(4, false));
		Label userNameLabel = new Label(parent, SWT.NONE);
		userNameLabel.setText("UserName");
		userNameText = new Text(parent, SWT.NONE);
		GridData layoutData = new GridData();
		layoutData.horizontalSpan = 2;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL_HORIZONTAL;
		Button add = new Button(parent, SWT.PUSH | SWT.CENTER);
		add.setText("Refresh");
		add.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				refresh();
			}
		});
		userNameText.setLayoutData(layoutData);
		createTable(parent);
		createTableViewer();
		tableViewer.setContentProvider(new ViewContentProvider());
		tableViewer.setLabelProvider(new ViewLabelProvider());
		tableViewer.setInput(getViewSite());
		createButtons(parent);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
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
		gridData.horizontalSpan = 4;
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
		column.setText("Expiration");
		column.setWidth(120);
	}

	private void createTableViewer() {
		tableViewer = new TableViewer(table);
		tableViewer.setUseHashlookup(true);
		tableViewer.setColumnProperties(columnNames);
	}
	
	private void createButtons(Composite parent) {
		Button add = new Button(parent, SWT.PUSH | SWT.CENTER);
		add.setText("Refresh");

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.widthHint = 80;
		add.setLayoutData(gridData);
		add.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				refresh();
			}
		});
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
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = tableViewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on " + obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			tableViewer.getControl().getShell(), "Task View", message);
	}

	public void setFocus() {
		tableViewer.getControl().setFocus();
	}
	
	private void refresh() {
		MinaTaskClient client = new MinaTaskClient(
			"org.drools.eclipse.task.views.TaskView", new TaskClientHandler());
		NioSocketConnector connector = new NioSocketConnector();
		SocketAddress address = new InetSocketAddress(ipAddress, port);
		boolean connected = client.connect(connector, address);
		if (!connected) {
			showMessage("Could not connect to task server: " + ipAddress + " [port " + port + "]");
			return;
		}
		
		BlockingTaskSummaryResponseHandler responseHandler = new BlockingTaskSummaryResponseHandler();
		Long userId = null;
		try {
			userId = new Long(userNameText.getText());
		} catch (NumberFormatException e) {
			showMessage("Could not convert user id, should be a long value.");
			return;
		}
        client.getTasksAssignedAsPotentialOwner(userId, "en-UK", responseHandler);
        List<TaskSummary> tasks = responseHandler.getResults();
        client.disconnect();
        
        System.out.println("<<<<<< Tasks");
        for (TaskSummary task: tasks) {
        	System.out.println(task);
        }
        System.out.println("Tasks >>>>>>");
        
        tableViewer.setInput(tasks);
        tableViewer.refresh();
	}
	
	private class BlockingTaskSummaryResponseHandler implements TaskSummaryResponseHandler {
        private volatile List<TaskSummary> results;
		public void execute(List<TaskSummary> results) {
            this.results = results;
		}
		public List<TaskSummary> getResults() {
			int retryCounter = 0;
			while (results == null && retryCounter < 5 ) {
				try {
					Thread.sleep(1000);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				retryCounter++;
			}
			if (results == null) {
				throw new RuntimeException("Timeout : unable to retrieve results");
			}
			return results;
		}
	}
	
}