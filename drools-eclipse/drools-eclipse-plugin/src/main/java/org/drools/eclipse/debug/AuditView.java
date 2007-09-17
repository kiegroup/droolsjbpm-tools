package org.drools.eclipse.debug;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.audit.event.ActivationLogEvent;
import org.drools.audit.event.LogEvent;
import org.drools.audit.event.ObjectLogEvent;
import org.drools.audit.event.RuleBaseLogEvent;
import org.drools.audit.event.RuleFlowGroupLogEvent;
import org.drools.audit.event.RuleFlowLogEvent;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DroolsPluginImages;
import org.drools.eclipse.debug.actions.DeleteLogAction;
import org.drools.eclipse.debug.actions.OpenLogAction;
import org.drools.eclipse.debug.actions.RefreshLogAction;
import org.drools.eclipse.debug.actions.ShowEventCauseAction;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;

import com.thoughtworks.xstream.XStream;

public class AuditView extends AbstractDebugView {
	
	private static final String LOG_FILE_NAME = "LogFileName";
	private static final String CAUSE_EVENT_COLOR = "CauseEventColor";
	
	private String logFileName;
	private IAction deleteAction;
	private IAction refreshAction;
	
    protected Viewer createViewer(Composite parent) {
		final TreeViewer variablesViewer = new TreeViewer(parent);
		variablesViewer.setContentProvider(new AuditViewContentProvider());
        variablesViewer.setLabelProvider(new AuditLabelProvider());
        variablesViewer.setUseHashlookup(true);
        variablesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				getViewer().refresh();
			}
        });
        return variablesViewer;
    }
    
    public void setLogFile(String logFileName) {
    	this.logFileName = logFileName;
    	refresh();
		deleteAction.setEnabled(logFileName != null);
        refreshAction.setEnabled(logFileName != null);
    }
    
    public void refresh() {
    	if (logFileName == null) {
    		getViewer().setInput(null);
    		return;
    	}
		try {
			XStream xstream = new XStream();
			ObjectInputStream in = xstream.createObjectInputStream(
				new FileReader(logFileName));
			getViewer().setInput(createEventList((List) in.readObject()));
			// TODO: this is necessary because otherwise, the show cause action
			// cannot find the cause event if it hasn't been shown yet
			((TreeViewer) getViewer()).expandAll();
		} catch (FileNotFoundException e) {
			setLogFile(null);
			getViewer().setInput(null);
		} catch (Throwable t) {
			DroolsEclipsePlugin.log(t);
			getViewer().setInput(null);
		}
    }
    
    protected List createEventList(List logEvents) {
		Iterator iterator = logEvents.iterator();
		List events = new ArrayList();
		Event currentBeforeActivationEvent = null;
		Event currentBeforePackageEvent = null;
		List newActivations = new ArrayList();
		Map activationMap = new HashMap();
		Map objectMap = new HashMap();
		while (iterator.hasNext()) {
			LogEvent inEvent = (LogEvent) iterator.next();
			Event event = new Event(inEvent.getType());
			switch (inEvent.getType()) {
				case LogEvent.INSERTED:
					ObjectLogEvent inObjectEvent = (ObjectLogEvent) inEvent;
					event.setString("Object inserted (" + inObjectEvent.getFactId() + "): " + inObjectEvent.getObjectToString());
					if (currentBeforeActivationEvent != null) {
						currentBeforeActivationEvent.addSubEvent(event);
					} else {
						events.add(event);
					}
					event.addSubEvents(newActivations);
					newActivations.clear();
					objectMap.put(new Long(((ObjectLogEvent) inEvent).getFactId()), event);
					break;
				case LogEvent.UPDATED:
					inObjectEvent = (ObjectLogEvent) inEvent;
					event.setString("Object updated (" + inObjectEvent.getFactId() + "): " + inObjectEvent.getObjectToString());
					if (currentBeforeActivationEvent != null) {
						currentBeforeActivationEvent.addSubEvent(event);
					} else {
						events.add(event);
					}
					event.addSubEvents(newActivations);
					newActivations.clear();
					Event assertEvent = (Event) objectMap.get(new Long(((ObjectLogEvent) inEvent).getFactId()));
					if (assertEvent != null) {
						event.setCauseEvent(assertEvent);
					}
					break;
				case LogEvent.RETRACTED:
					inObjectEvent = (ObjectLogEvent) inEvent;
					event.setString("Object removed (" + inObjectEvent.getFactId() + "): " + inObjectEvent.getObjectToString());
					if (currentBeforeActivationEvent != null) {
						currentBeforeActivationEvent.addSubEvent(event);
					} else {
						events.add(event);
					}
					event.addSubEvents(newActivations);
					newActivations.clear();
					assertEvent = (Event) objectMap.get(new Long(((ObjectLogEvent) inEvent).getFactId()));
					if (assertEvent != null) {
						event.setCauseEvent(assertEvent);
					}
					break;
				case LogEvent.ACTIVATION_CREATED:
					ActivationLogEvent inActivationEvent = (ActivationLogEvent) inEvent;
					event.setString("Activation created: Rule " + inActivationEvent.getRule() + " " + inActivationEvent.getDeclarations());
					newActivations.add(event);
					activationMap.put(((ActivationLogEvent) inEvent).getActivationId(), event);
					break;
				case LogEvent.ACTIVATION_CANCELLED:
					inActivationEvent = (ActivationLogEvent) inEvent;
					event.setString("Activation cancelled: Rule " + inActivationEvent.getRule() + " " + inActivationEvent.getDeclarations());
					newActivations.add(event);
					event.setCauseEvent((Event) activationMap.get(((ActivationLogEvent) inEvent).getActivationId()));
					break;
				case LogEvent.BEFORE_ACTIVATION_FIRE:
					inActivationEvent = (ActivationLogEvent) inEvent;
					event.setString("Activation executed: Rule " + inActivationEvent.getRule() + " " + inActivationEvent.getDeclarations());
					events.add(event);
					currentBeforeActivationEvent = event;
					event.setCauseEvent((Event) activationMap.get(((ActivationLogEvent) inEvent).getActivationId()));
					break;
				case LogEvent.AFTER_ACTIVATION_FIRE:
					currentBeforeActivationEvent = null;
					break;
				case LogEvent.RULEFLOW_CREATED:
					RuleFlowLogEvent inRuleFlowEvent = (RuleFlowLogEvent) inEvent;
					event.setString("RuleFlow started: " + inRuleFlowEvent.getProcessName() + "[" + inRuleFlowEvent.getProcessId() + "]");
					if (currentBeforeActivationEvent != null) {
						currentBeforeActivationEvent.addSubEvent(event);
					} else {
						events.add(event);
					}
					break;
				case LogEvent.RULEFLOW_COMPLETED:
					inRuleFlowEvent = (RuleFlowLogEvent) inEvent;
					event.setString("RuleFlow completed: " + inRuleFlowEvent.getProcessName() + "[" + inRuleFlowEvent.getProcessId() + "]");
					if (currentBeforeActivationEvent != null) {
						currentBeforeActivationEvent.addSubEvent(event);
					} else {
						events.add(event);
					}
					break;
				case LogEvent.RULEFLOW_GROUP_ACTIVATED:
					RuleFlowGroupLogEvent inRuleFlowGroupEvent = (RuleFlowGroupLogEvent) inEvent;
					event.setString("RuleFlowGroup activated: " + inRuleFlowGroupEvent.getGroupName() + "[size=" + inRuleFlowGroupEvent.getSize() + "]");
					if (currentBeforeActivationEvent != null) {
						currentBeforeActivationEvent.addSubEvent(event);
					} else {
						events.add(event);
					}
					break;
				case LogEvent.RULEFLOW_GROUP_DEACTIVATED:
					inRuleFlowGroupEvent = (RuleFlowGroupLogEvent) inEvent;
					event.setString("RuleFlowGroup deactivated: " + inRuleFlowGroupEvent.getGroupName() + "[size=" + inRuleFlowGroupEvent.getSize() + "]");
					if (currentBeforeActivationEvent != null) {
						currentBeforeActivationEvent.addSubEvent(event);
					} else {
						events.add(event);
					}
					break;
				case LogEvent.BEFORE_PACKAGE_ADDED:
					RuleBaseLogEvent ruleBaseEvent = (RuleBaseLogEvent) inEvent;
					event.setString("Package added: " + ruleBaseEvent.getPackageName());
					if (currentBeforeActivationEvent != null) {
						currentBeforeActivationEvent.addSubEvent(event);
					} else {
						events.add(event);
					}
					currentBeforePackageEvent = event;
					break;
				case LogEvent.AFTER_PACKAGE_ADDED:
					currentBeforePackageEvent = null;
					break;
				case LogEvent.BEFORE_PACKAGE_REMOVED:
					ruleBaseEvent = (RuleBaseLogEvent) inEvent;
					event.setString("Package removed: " + ruleBaseEvent.getPackageName());
					if (currentBeforeActivationEvent != null) {
						currentBeforeActivationEvent.addSubEvent(event);
					} else {
						events.add(event);
					}
					currentBeforePackageEvent = event;
					break;
				case LogEvent.AFTER_PACKAGE_REMOVED:
					currentBeforePackageEvent = null;
					break;
				case LogEvent.AFTER_RULE_ADDED:
					ruleBaseEvent = (RuleBaseLogEvent) inEvent;
					event.setString("Rule added: " + ruleBaseEvent.getRuleName());
					if (currentBeforePackageEvent != null) {
						currentBeforePackageEvent.addSubEvent(event);
					} else if (currentBeforeActivationEvent != null) {
						currentBeforeActivationEvent.addSubEvent(event);
					} else {
						events.add(event);
					}
					event.addSubEvents(newActivations);
					newActivations.clear();
					break;
				case LogEvent.AFTER_RULE_REMOVED:
					ruleBaseEvent = (RuleBaseLogEvent) inEvent;
					event.setString("Rule removed: " + ruleBaseEvent.getRuleName());
					if (currentBeforePackageEvent != null) {
						currentBeforePackageEvent.addSubEvent(event);
					} else if (currentBeforeActivationEvent != null) {
						currentBeforeActivationEvent.addSubEvent(event);
					} else {
						events.add(event);
					}
					event.addSubEvents(newActivations);
					newActivations.clear();
					break;
			}
		}
		return events;
    }
    
    public void deleteLog() {
    	if (logFileName != null) {
    		File file = new File(logFileName);
    		try {
    			file.delete();
    			// TODO delete file cause this doesn't seem to work
    			setLogFile(null);
    			refresh();
    		} catch (Throwable t) {
    			t.printStackTrace();
    			DroolsEclipsePlugin.log(t);
    		}
    	}
    }
    
	protected void becomesVisible() {
		refresh();
	}
    
    protected String getHelpContextId() {
        return null;
    }
    
    public Event getSelectedEvent() {
    	ISelection selection = getViewer().getSelection();
    	if (selection instanceof IStructuredSelection) {
    		Object selected = ((IStructuredSelection) selection).getFirstElement();
    		if (selected instanceof Event) {
    			return (Event) selected;
    		}
    	}
    	return null;
    }
    
    public void showEvent(Event event) {
    	((TreeViewer) getViewer()).reveal(event);
    }

    protected void fillContextMenu(IMenuManager menu) {
    	Event selected = getSelectedEvent();
    	if (selected != null) {
			Event causeEvent = selected.getCauseEvent();
			if (causeEvent != null) {
				menu.add(getAction("ShowEventCause"));
			}
    	}
		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    }

	protected void createActions() {
        deleteAction = new DeleteLogAction(this);
        setAction("ClearLog", deleteAction);
        deleteAction.setEnabled(logFileName != null);
        refreshAction = new RefreshLogAction(this);
        setAction("RefreshLog", refreshAction);
        refreshAction.setEnabled(logFileName != null);
        IAction action = new OpenLogAction(this);
        setAction("OpenLog", action);
		action = new ShowEventCauseAction(this);
		setAction("ShowEventCause", action);
    }

    protected void configureToolBar(IToolBarManager tbm) {
        tbm.add(getAction("OpenLog"));
        tbm.add(getAction("RefreshLog"));
        tbm.add(getAction("ClearLog"));
    }
    
    public void saveState(IMemento memento) {
    	memento.putString(LOG_FILE_NAME, logFileName);
    }
    
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        if (memento != null) {
        	logFileName = memento.getString(LOG_FILE_NAME);
        }
    }
    
	public class Event {
		
		private String toString;
		private int type;
		private List subEvents = new ArrayList();
		private Event causeEvent;
		
		public Event(int type) {
			this.type = type;
		}
		
		public void setString(String toString) {
			this.toString = toString;
		}
		
		public String toString() {
			return toString;
		}
		
		public int getType() {
			return type;
		}
		
		public void addSubEvent(Event subEvent) {
			subEvents.add(subEvent);
		}
		
		public void addSubEvents(Collection subEvents) {
			this.subEvents.addAll(subEvents);
		}
		
		public Object[] getSubEvents() {
			return subEvents.toArray();
		}
		
		public boolean hasSubEvents() {
			return !subEvents.isEmpty();
		}
		
		public void setCauseEvent(Event causeEvent) {
			this.causeEvent = causeEvent;
		}
		
		public Event getCauseEvent() {
			return causeEvent;
		}
	}
	
	public class AuditLabelProvider extends LabelProvider implements IColorProvider {

		public Color getForeground(Object element) {
			return null;
		}

		public Color getBackground(Object element) {
			Event selected = getSelectedEvent();
			if (selected != null) {
				if (element.equals(selected.getCauseEvent())) {
			        Color color = DroolsEclipsePlugin.getDefault().getColor(CAUSE_EVENT_COLOR);
			        if (color == null) {
			        	color = new Color(getControl().getDisplay(), 0, 255, 0); 
		        		DroolsEclipsePlugin.getDefault().setColor(CAUSE_EVENT_COLOR, color);
			        }
			        return color;
				}
			}
			return null;
		}
		
	    public Image getImage(Object element) {
	    	if (element instanceof Event) {
	    		int type = ((Event) element).getType();
	    		switch (type) {
	    			case LogEvent.INSERTED: return DroolsPluginImages.getImage(DroolsPluginImages.INSERT);
	    			case LogEvent.UPDATED: return DroolsPluginImages.getImage(DroolsPluginImages.UPDATE);
	    			case LogEvent.RETRACTED: return DroolsPluginImages.getImage(DroolsPluginImages.RETRACT);
	    			case LogEvent.ACTIVATION_CREATED: return DroolsPluginImages.getImage(DroolsPluginImages.CREATE_ACTIVATION);
	    			case LogEvent.ACTIVATION_CANCELLED: return DroolsPluginImages.getImage(DroolsPluginImages.CANCEL_ACTIVATION);
	    			case LogEvent.BEFORE_ACTIVATION_FIRE: return DroolsPluginImages.getImage(DroolsPluginImages.EXECUTE_ACTIVATION);
	    			case LogEvent.RULEFLOW_CREATED: return DroolsPluginImages.getImage(DroolsPluginImages.RULEFLOW);
	    			case LogEvent.RULEFLOW_COMPLETED: return DroolsPluginImages.getImage(DroolsPluginImages.RULEFLOW);
	    			case LogEvent.RULEFLOW_GROUP_ACTIVATED: return DroolsPluginImages.getImage(DroolsPluginImages.RULEFLOW);
	    			case LogEvent.RULEFLOW_GROUP_DEACTIVATED: return DroolsPluginImages.getImage(DroolsPluginImages.RULEFLOW);
	    			case LogEvent.BEFORE_PACKAGE_ADDED: return DroolsPluginImages.getImage(DroolsPluginImages.DROOLS);
	    			case LogEvent.BEFORE_PACKAGE_REMOVED: return DroolsPluginImages.getImage(DroolsPluginImages.DROOLS);
	    			case LogEvent.AFTER_RULE_ADDED: return DroolsPluginImages.getImage(DroolsPluginImages.DROOLS);
	    			case LogEvent.AFTER_RULE_REMOVED: return DroolsPluginImages.getImage(DroolsPluginImages.DROOLS);
	    		}
	    		return null;
	    	}
	        return null;
	    }
	}
}
