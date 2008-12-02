package org.drools.eclipse.flow.ruleflow.skin;

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.common.editor.core.ElementConnectionFactory;
import org.drools.eclipse.flow.ruleflow.core.ActionWrapper;
import org.drools.eclipse.flow.ruleflow.core.CompositeContextNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.ConnectionWrapper;
import org.drools.eclipse.flow.ruleflow.core.ConnectionWrapperFactory;
import org.drools.eclipse.flow.ruleflow.core.EndNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.EventNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.FaultNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.ForEachNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.HumanTaskNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.JoinWrapper;
import org.drools.eclipse.flow.ruleflow.core.MilestoneWrapper;
import org.drools.eclipse.flow.ruleflow.core.RuleSetNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.SplitWrapper;
import org.drools.eclipse.flow.ruleflow.core.StartNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.SubProcessWrapper;
import org.drools.eclipse.flow.ruleflow.core.TimerWrapper;
import org.drools.eclipse.flow.ruleflow.editor.editpart.ActionEditPart;
import org.drools.eclipse.flow.ruleflow.editor.editpart.CompositeNodeEditPart;
import org.drools.eclipse.flow.ruleflow.editor.editpart.EndNodeEditPart;
import org.drools.eclipse.flow.ruleflow.editor.editpart.EventNodeEditPart;
import org.drools.eclipse.flow.ruleflow.editor.editpart.FaultNodeEditPart;
import org.drools.eclipse.flow.ruleflow.editor.editpart.JoinEditPart;
import org.drools.eclipse.flow.ruleflow.editor.editpart.MilestoneEditPart;
import org.drools.eclipse.flow.ruleflow.editor.editpart.RuleSetNodeEditPart;
import org.drools.eclipse.flow.ruleflow.editor.editpart.SplitEditPart;
import org.drools.eclipse.flow.ruleflow.editor.editpart.StartNodeEditPart;
import org.drools.eclipse.flow.ruleflow.editor.editpart.SubFlowEditPart;
import org.drools.eclipse.flow.ruleflow.editor.editpart.TimerEditPart;
import org.drools.eclipse.flow.ruleflow.editor.editpart.WorkItemEditPart;
import org.drools.eclipse.flow.ruleflow.editor.editpart.JoinEditPart.JoinFigureInterface;
import org.drools.eclipse.flow.ruleflow.editor.editpart.SplitEditPart.SplitFigureInterface;
import org.drools.eclipse.flow.ruleflow.editor.editpart.WorkItemEditPart.WorkItemFigureInterface;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.jface.resource.ImageDescriptor;

public class DefaultSkinProvider implements SkinProvider {
	
    public PaletteContainer createComponentsDrawer() {
    	
    	String flowNodes = DroolsEclipsePlugin.getDefault().getPluginPreferences().getString(IDroolsConstants.FLOW_NODES);

        PaletteDrawer drawer = new PaletteDrawer("Components", null);

        List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

        CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
            "Start",
            "Create a new Start",
            StartNodeWrapper.class,
            new SimpleFactory(StartNodeWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/process_start.gif")),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/process_start.gif"))
        );
        entries.add(combined);
        
        combined = new CombinedTemplateCreationEntry(
            "End",
            "Create a new End",
            EndNodeWrapper.class,
            new SimpleFactory(EndNodeWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/process_stop.gif")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/process_stop.gif"))
        );
        entries.add(combined);
                
        if (flowNodes.charAt(0) == '1') {
	        combined = new CombinedTemplateCreationEntry(
	            "RuleFlowGroup",
	            "Create a new RuleFlowGroup",
	            RuleSetNodeWrapper.class,
	            new SimpleFactory(RuleSetNodeWrapper.class),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/activity.gif")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/activity.gif"))
	        );
	        entries.add(combined);
        }
            
        if (flowNodes.charAt(1) == '1') {
        	combined = new CombinedTemplateCreationEntry(
	            "Split",
	            "Create a new Split",
	            SplitWrapper.class,
	            new SimpleFactory(SplitWrapper.class),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/split.gif")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/split.gif"))
	        );
	        entries.add(combined);
        }
                    
        if (flowNodes.charAt(2) == '1') {
	        combined = new CombinedTemplateCreationEntry(
	            "Join",
	            "Create a new Join",
	            JoinWrapper.class,
	            new SimpleFactory(JoinWrapper.class),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/join.gif")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/join.gif"))
	        );
	        entries.add(combined);
        }
                        
        if (flowNodes.charAt(3) == '1') {
	        combined = new CombinedTemplateCreationEntry(
	            "Event Wait",
	            "Create a new Event Wait",
	            MilestoneWrapper.class,
	            new SimpleFactory(MilestoneWrapper.class),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/question.gif")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/question.gif"))
	        );
	        entries.add(combined);
        }
                            
        if (flowNodes.charAt(4) == '1') {
	        combined = new CombinedTemplateCreationEntry(
	            "SubFlow",
	            "Create a new SubFlow",
	            SubProcessWrapper.class,
	            new SimpleFactory(SubProcessWrapper.class),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/process.gif")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/process.gif"))
	        );
	        entries.add(combined);
        }
                                
        if (flowNodes.charAt(5) == '1') {
        	combined = new CombinedTemplateCreationEntry(
	            "Action",
	            "Create a new Action",
	            ActionWrapper.class,
	            new SimpleFactory(ActionWrapper.class),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/action.gif")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/action.gif"))
	        );
	        entries.add(combined);
        }
                      
        if (flowNodes.charAt(6) == '1') {
	        combined = new CombinedTemplateCreationEntry(
	            "Timer",
	            "Create a new Timer",
	            TimerWrapper.class,
	            new SimpleFactory(TimerWrapper.class),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/timer.gif")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/timer.gif"))
	        );
	        entries.add(combined);
        }
                      
        if (flowNodes.charAt(7) == '1') {
	        combined = new CombinedTemplateCreationEntry(
	            "Fault",
	            "Create a new Fault",
	            FaultNodeWrapper.class,
	            new SimpleFactory(FaultNodeWrapper.class),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/fault.gif")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/fault.gif"))
	        );
	        entries.add(combined);
        }
                          
        if (flowNodes.charAt(8) == '1') {
        	combined = new CombinedTemplateCreationEntry(
			    "Event",
			    "Create a new Event Node",
			    EventNodeWrapper.class,
			    new SimpleFactory(EventNodeWrapper.class),
			    ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/event.gif")), 
			    ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/event.gif"))
			);
			entries.add(combined);
        }
	                    
        if (flowNodes.charAt(9) == '1') {
        	combined = new CombinedTemplateCreationEntry(
	            "Human Task",
	            "Create a new Human Task",
	            HumanTaskNodeWrapper.class,
	            new SimpleFactory(HumanTaskNodeWrapper.class),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/human_task.gif")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/human_task.gif"))
	        );
	        entries.add(combined);
        }
                              
        if (flowNodes.charAt(10) == '1') {
	        combined = new CombinedTemplateCreationEntry(
	            "Composite",
	            "Create a new Composite Node",
	            CompositeContextNodeWrapper.class,
	            new SimpleFactory(CompositeContextNodeWrapper.class),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/composite.gif")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/composite.gif"))
	        );
	        entries.add(combined);
        }
                          
        if (flowNodes.charAt(11) == '1') {
		    combined = new CombinedTemplateCreationEntry(
			    "For Each",
			    "Create a new ForEach Node",
			    ForEachNodeWrapper.class,
			    new SimpleFactory(ForEachNodeWrapper.class),
			    ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/composite.gif")), 
			    ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/composite.gif"))
			);
			entries.add(combined);
        }
                    
        drawer.addAll(entries);
        return drawer;
    }
    
    public PaletteEntry createConnectionEntry() {
    	final ElementConnectionFactory normalConnectionFactory = new ConnectionWrapperFactory();
        PaletteEntry tool = new ConnectionCreationToolEntry(
    		"Connection Creation",
            "Creating connections",
            new CreationFactory() {
                public Object getNewObject() {
                	return normalConnectionFactory.createElementConnection();
                }
                public Object getObjectType() {
                	return ConnectionWrapper.class;
                }
            },
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/connection.gif")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/connection.gif"))
        );
    	return tool;
    }

	public IFigure createActionNodeFigure() {
		return new ActionEditPart.ActionNodeFigure();
	}

	public IFigure createEndNodeFigure() {
		return new EndNodeEditPart.EndNodeFigure();
	}

	public IFigure createEventNodeFigure() {
		return new EventNodeEditPart.EventNodeFigure();
	}

	public IFigure createFaultNodeFigure() {
		return new FaultNodeEditPart.FaultNodeFigure();
	}

	public JoinFigureInterface createJoinFigure() {
		return new JoinEditPart.JoinFigure();
	}

	public SplitFigureInterface createSplitFigure() {
		return new SplitEditPart.SplitFigure();
	}

	public IFigure createStartNodeFigure() {
		return new StartNodeEditPart.StartNodeFigure();
	}

	public IFigure createTimerNodeFigure() {
		return new TimerEditPart.TimerNodeFigure();
	}

	public IFigure createMilestoneFigure() {
		return new MilestoneEditPart.MilestoneFigure();
	}

	public IFigure createRuleSetNodeFigure() {
		return new RuleSetNodeEditPart.RuleSetNodeFigure();
	}

	public IFigure createSubFlowFigure() {
		return new SubFlowEditPart.SubFlowNodeFigure();
	}

	public WorkItemFigureInterface createWorkItemFigure() {
		return new WorkItemEditPart.WorkItemFigure();
	}

	public IFigure createCompositeNodeFigure() {
		return new CompositeNodeEditPart.CompositeNodeFigure();
	}

}
