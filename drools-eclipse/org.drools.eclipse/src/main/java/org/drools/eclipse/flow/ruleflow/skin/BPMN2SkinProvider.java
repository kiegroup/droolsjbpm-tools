package org.drools.eclipse.flow.ruleflow.skin;

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.common.editor.core.ElementConnectionFactory;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.eclipse.flow.common.editor.editpart.figure.ElementContainerFigure;
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
import org.drools.eclipse.flow.ruleflow.core.RuleSetNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.SplitWrapper;
import org.drools.eclipse.flow.ruleflow.core.StartNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.StateNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.SubProcessWrapper;
import org.drools.eclipse.flow.ruleflow.core.TimerWrapper;
import org.drools.eclipse.flow.ruleflow.editor.editpart.JoinEditPart.JoinFigureInterface;
import org.drools.eclipse.flow.ruleflow.editor.editpart.SplitEditPart.SplitFigureInterface;
import org.drools.eclipse.flow.ruleflow.editor.editpart.WorkItemEditPart.WorkItemFigureInterface;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2.BPMNActionNodeFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2.BPMNCompositeNodeFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2.BPMNEndNodeFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2.BPMNEventNodeFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2.BPMNFaultNodeFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2.BPMNJoinFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2.BPMNMilestoneFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2.BPMNRuleSetNodeFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2.BPMNSplitFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2.BPMNStartNodeFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2.BPMNStateFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2.BPMNSubFlowFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2.BPMNTimerNodeFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2.BPMNWorkItemNodeFigure;
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

public class BPMN2SkinProvider implements SkinProvider {

    public PaletteContainer createComponentsDrawer() {

    	String flowNodes = DroolsEclipsePlugin.getDefault().getPluginPreferences().getString(IDroolsConstants.FLOW_NODES);

    	PaletteDrawer drawer = new PaletteDrawer("Components", null);

        List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

        CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
            "Start Event",
            "Create a new Start Event",
            StartNodeWrapper.class,
            new SimpleWrapperFactory(StartNodeWrapper.class, "Start"),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn2/start_empty.png")),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn2/start_empty.png"))
        );
        entries.add(combined);
        
        combined = new CombinedTemplateCreationEntry(
            "End Event",
            "Create a new End Event",
            EndNodeWrapper.class,
            new SimpleWrapperFactory(EndNodeWrapper.class, "End"),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn2/end_terminate.png")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn2/end_terminate.png"))
        );
        entries.add(combined);
                
        if (flowNodes.charAt(0) == '1') {
	        combined = new CombinedTemplateCreationEntry(
	            "Rule Task",
	            "Create a new Rule Task",
	            RuleSetNodeWrapper.class,
	            new SimpleWrapperFactory(RuleSetNodeWrapper.class, "Rule"),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/Activity.gif")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/Activity.gif"))
	        );
	        entries.add(combined);
        }
        
        if (flowNodes.charAt(1) == '1') {
	        combined = new CombinedTemplateCreationEntry(
	            "Gateway [diverge]",
	            "Create a new Gateway [diverge]",
	            SplitWrapper.class,
	            new SimpleWrapperFactory(SplitWrapper.class, "Gateway"),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn2/gateway_exclusive.png")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn2/gateway_exclusive.png"))
	        );
	        entries.add(combined);
        }
                    
        if (flowNodes.charAt(2) == '1') {
        	combined = new CombinedTemplateCreationEntry(
	            "Gateway [converge]",
	            "Create a new Gateway [converge]",
	            JoinWrapper.class,
	            new SimpleWrapperFactory(JoinWrapper.class, "Gateway"),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn2/gateway_exclusive.png")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn2/gateway_exclusive.png"))
	        );
	        entries.add(combined);
        }
                        
        if (flowNodes.charAt(3) == '1') {
	        combined = new CombinedTemplateCreationEntry(
	            "Wait Task",
	            "Create a new Wait Task",
	            StateNodeWrapper.class,
	            new SimpleWrapperFactory(StateNodeWrapper.class, "Wait"),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/Activity.gif")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/Activity.gif"))
	        );
	        entries.add(combined);
        }
                            
        if (flowNodes.charAt(4) == '1') {
	        combined = new CombinedTemplateCreationEntry(
	            "Reusable Sub-Process",
	            "Create a new Reusable Sub-Process",
	            SubProcessWrapper.class,
	            new SimpleWrapperFactory(SubProcessWrapper.class, "Sub-Process"),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/sub_process.png")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/sub_process.png"))
	        );
	        entries.add(combined);
        }
        
        if (flowNodes.charAt(5) == '1') {
	        combined = new CombinedTemplateCreationEntry(
	            "Script Task",
	            "Create a new Script Task",
	            ActionWrapper.class,
	            new SimpleWrapperFactory(ActionWrapper.class, "Script"),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/Activity.gif")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/Activity.gif"))
	        );
	        entries.add(combined);
        }
                      
        if (flowNodes.charAt(6) == '1') {
	        combined = new CombinedTemplateCreationEntry(
	            "Timer Event",
	            "Create a new Timer Event",
	            TimerWrapper.class,
	            new SimpleWrapperFactory(TimerWrapper.class, "Timer"),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/intermediate_timer.png")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/intermediate_timer.png"))
	        );
	        entries.add(combined);
        }
                      
        if (flowNodes.charAt(7) == '1') {
	    	combined = new CombinedTemplateCreationEntry(
	            "Error Event",
	            "Create a new Error Event",
	            FaultNodeWrapper.class,
	            new SimpleWrapperFactory(FaultNodeWrapper.class, "Error"),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/intermediate_error_10.png")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/intermediate_error_10.png"))
	        );
	        entries.add(combined);
        }
                          
        if (flowNodes.charAt(8) == '1') {
	        combined = new CombinedTemplateCreationEntry(
			    "Message Event",
			    "Create a new Message Event",
			    EventNodeWrapper.class,
			    new SimpleWrapperFactory(EventNodeWrapper.class, "Message"),
			    ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/intermediate_message.png")), 
			    ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/intermediate_empty.png"))
			);
			entries.add(combined);
        }
	                    
        if (flowNodes.charAt(9) == '1') {
	        combined = new CombinedTemplateCreationEntry(
	            "User Task",
	            "Create a new User Task",
	            HumanTaskNodeWrapper.class,
	            new SimpleWrapperFactory(HumanTaskNodeWrapper.class, "User Task"),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/task.png")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/task.png"))
	        );
	        entries.add(combined);
        }
                              
        if (flowNodes.charAt(10) == '1') {
	        combined = new CombinedTemplateCreationEntry(
	            "Embedded Sub-Process",
	            "Create a new Embedded Sub-Process",
	            CompositeContextNodeWrapper.class,
	            new SimpleWrapperFactory(CompositeContextNodeWrapper.class, "Sub-Process"),
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/sub_process_expanded.png")), 
	            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/sub_process_expanded.png"))
	        );
	        entries.add(combined);
        }
                          
        if (flowNodes.charAt(11) == '1') {
		    combined = new CombinedTemplateCreationEntry(
			    "Multiple Instances",
			    "Create a new Multiple Instances",
			    ForEachNodeWrapper.class,
			    new SimpleWrapperFactory(ForEachNodeWrapper.class, "Multiple Instances"),
			    ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/sub_process_expanded.png")), 
			    ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/sub_process_expanded.png"))
			);
			entries.add(combined);
        }
                    
        drawer.addAll(entries);
        return drawer;
    }
    
    public PaletteEntry createConnectionEntry() {
    	final ElementConnectionFactory normalConnectionFactory = new ConnectionWrapperFactory();
        PaletteEntry tool = new ConnectionCreationToolEntry(
    		"Sequence Flow",
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
		return new BPMNActionNodeFigure();
	}

	public IFigure createEndNodeFigure() {
		return new BPMNEndNodeFigure();
	}

	public IFigure createEventNodeFigure() {
		return new BPMNEventNodeFigure();
	}

	public IFigure createFaultNodeFigure() {
		return new BPMNFaultNodeFigure();
	}

	public JoinFigureInterface createJoinFigure() {
		return new BPMNJoinFigure();
	}

	public SplitFigureInterface createSplitFigure() {
		return new BPMNSplitFigure();
	}

	public IFigure createStartNodeFigure() {
		return new BPMNStartNodeFigure();
	}

	public IFigure createTimerNodeFigure() {
		return new BPMNTimerNodeFigure();
	}

	public IFigure createMilestoneFigure() {
		return new BPMNMilestoneFigure();
	}

	public IFigure createRuleSetNodeFigure() {
		return new BPMNRuleSetNodeFigure();
	}

	public IFigure createSubFlowFigure() {
		return new BPMNSubFlowFigure();
	}

	public IFigure createStateFigure() {
		return new BPMNStateFigure();
	}

	public WorkItemFigureInterface createWorkItemFigure() {
		return new BPMNWorkItemNodeFigure();
	}

	public IFigure createCompositeNodeFigure() {
		return new BPMNCompositeNodeFigure();
	}

	public IFigure createForEachNodeFigure() {
		return new ElementContainerFigure();
	}
	
	private static class SimpleWrapperFactory extends SimpleFactory {

		private String name;
		
		public SimpleWrapperFactory(Class<?> clazz, String name) {
			super(clazz);
			this.name = name;
		}
		
		public Object getNewObject() {
			ElementWrapper wrapper = (ElementWrapper) super.getNewObject();
			wrapper.setName(name);
			return wrapper;
		}
		
	}

}
