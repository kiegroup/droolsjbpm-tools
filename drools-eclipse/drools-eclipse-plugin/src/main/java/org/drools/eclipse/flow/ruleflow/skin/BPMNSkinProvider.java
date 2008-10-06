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
import org.drools.eclipse.flow.ruleflow.editor.editpart.JoinEditPart.JoinFigureInterface;
import org.drools.eclipse.flow.ruleflow.editor.editpart.SplitEditPart.SplitFigureInterface;
import org.drools.eclipse.flow.ruleflow.editor.editpart.WorkItemEditPart.WorkItemFigureInterface;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn.BPMNActionNodeFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn.BPMNCompositeNodeFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn.BPMNEndNodeFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn.BPMNEventNodeFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn.BPMNFaultNodeFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn.BPMNJoinFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn.BPMNMilestoneFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn.BPMNRuleSetNodeFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn.BPMNSplitFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn.BPMNStartNodeFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn.BPMNSubFlowFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn.BPMNTimerNodeFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn.BPMNWorkItemNodeFigure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.jface.resource.ImageDescriptor;

public class BPMNSkinProvider implements SkinProvider {

    public PaletteContainer createComponentsDrawer() {

        PaletteDrawer drawer = new PaletteDrawer("Components", null);

        List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

        CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
            "Start Event",
            "Create a new Start",
            StartNodeWrapper.class,
            new SimpleFactory(StartNodeWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/start_empty.png")),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/start_empty.png"))
        );
        entries.add(combined);
        
        combined = new CombinedTemplateCreationEntry(
            "End Event",
            "Create a new End",
            EndNodeWrapper.class,
            new SimpleFactory(EndNodeWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/end_terminate.png")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/end_terminate.png"))
        );
        entries.add(combined);
                
        combined = new CombinedTemplateCreationEntry(
            "Rule Task",
            "Create a new RuleFlowGroup",
            RuleSetNodeWrapper.class,
            new SimpleFactory(RuleSetNodeWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/Activity.gif")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/Activity.gif"))
        );
        entries.add(combined);
            
        combined = new CombinedTemplateCreationEntry(
            "Gateway [diverge]",
            "Create a new Split",
            SplitWrapper.class,
            new SimpleFactory(SplitWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/gateway_complex.png")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/gateway_complex.png"))
        );
        entries.add(combined);
                    
        combined = new CombinedTemplateCreationEntry(
            "Gateway [converge]",
            "Create a new Join",
            JoinWrapper.class,
            new SimpleFactory(JoinWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/gateway_complex.png")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/gateway_complex.png"))
        );
        entries.add(combined);
                        
        combined = new CombinedTemplateCreationEntry(
            "Wait Task",
            "Create a new Event Wait",
            MilestoneWrapper.class,
            new SimpleFactory(MilestoneWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/Activity.gif")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/Activity.gif"))
        );
        entries.add(combined);
                            
        combined = new CombinedTemplateCreationEntry(
            "Reusable Sub-Process",
            "Create a new SubFlow",
            SubProcessWrapper.class,
            new SimpleFactory(SubProcessWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/sub_process.png")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/sub_process.png"))
        );
        entries.add(combined);
                                
        combined = new CombinedTemplateCreationEntry(
            "Script Task",
            "Create a new Action",
            ActionWrapper.class,
            new SimpleFactory(ActionWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/Activity.gif")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/Activity.gif"))
        );
        entries.add(combined);
                      
        combined = new CombinedTemplateCreationEntry(
            "Timer Event",
            "Create a new Timer",
            TimerWrapper.class,
            new SimpleFactory(TimerWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/intermediate_timer.png")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/intermediate_timer.png"))
        );
        entries.add(combined);
                      
        combined = new CombinedTemplateCreationEntry(
            "Error Event",
            "Create a new Fault",
            FaultNodeWrapper.class,
            new SimpleFactory(FaultNodeWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/intermediate_error_10.png")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/intermediate_error_10.png"))
        );
        entries.add(combined);
                          
        combined = new CombinedTemplateCreationEntry(
		    "Message Event",
		    "Create a new Event Node",
		    EventNodeWrapper.class,
		    new SimpleFactory(EventNodeWrapper.class),
		    ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/intermediate_message.png")), 
		    ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/intermediate_empty.png"))
		);
		entries.add(combined);
	                    
        combined = new CombinedTemplateCreationEntry(
            "User Task",
            "Create a new Human Task",
            HumanTaskNodeWrapper.class,
            new SimpleFactory(HumanTaskNodeWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/task.png")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/task.png"))
        );
        entries.add(combined);
                              
        combined = new CombinedTemplateCreationEntry(
            "Embedded Sub-Process",
            "Create a new Composite Node",
            CompositeContextNodeWrapper.class,
            new SimpleFactory(CompositeContextNodeWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/sub_process_expanded.png")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/sub_process_expanded.png"))
        );
        entries.add(combined);
                          
	    combined = new CombinedTemplateCreationEntry(
		    "Multiple Instances",
		    "Create a new ForEach Node",
		    ForEachNodeWrapper.class,
		    new SimpleFactory(ForEachNodeWrapper.class),
		    ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/sub_process_expanded.png")), 
		    ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/sub_process_expanded.png"))
		);
		entries.add(combined);
                    
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

	public WorkItemFigureInterface createWorkItemFigure() {
		return new BPMNWorkItemNodeFigure();
	}

	public IFigure createCompositeNodeFigure() {
		return new BPMNCompositeNodeFigure();
	}

}
