package org.drools.eclipse.flow.ruleflow.core;

import org.drools.definition.process.Node;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.WorkItemDefinitions;
import org.drools.process.core.Work;
import org.drools.process.core.WorkDefinition;
import org.drools.process.core.impl.WorkDefinitionImpl;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.DynamicNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.EventNode;
import org.jbpm.workflow.core.node.FaultNode;
import org.jbpm.workflow.core.node.ForEachNode;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.MilestoneNode;
import org.jbpm.workflow.core.node.RuleSetNode;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.StateNode;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.jbpm.workflow.core.node.TimerNode;
import org.jbpm.workflow.core.node.WorkItemNode;

public class NodeWrapperFactory {
	
	private static NodeWrapperFactory factory = null;
	
	public static final NodeWrapperFactory INSTANCE = init();
	
	protected NodeWrapperFactory() {		
	}
	
	public static NodeWrapperFactory init() {
		if (factory != null) {
			return factory;
		} else {
			factory = getNodeWrapperFactory();
		}
		return factory;
	}
	
	protected StartNodeWrapper getStartNodeWrapper() {
		return new StartNodeWrapper();
	}
	
	protected EndNodeWrapper getEndNodeWrapper() {
		return new EndNodeWrapper();
	}
	
	protected ActionWrapper getActionWrapper() {
		return new ActionWrapper();
	}
	
	protected RuleSetNodeWrapper getRuleSetNodeWrapper() {
		return new RuleSetNodeWrapper();
	}
	
	protected SubProcessWrapper getSubProcessWrapper() {
		return new SubProcessWrapper();
	}
	
	protected ForEachNodeWrapper getForEachNodeWrapper() {
		return new ForEachNodeWrapper();
	}
	
	protected DynamicNodeWrapper getDynamicNodeWrapper() {
		return new DynamicNodeWrapper();
	}
	
	protected StateNodeWrapper getStateNodeWrapper()  {
		return new StateNodeWrapper();
	}
	
	protected CompositeContextNodeWrapper getCompositeContextNodeWrapper() {
		return new CompositeContextNodeWrapper();
	}
	
	protected JoinWrapper getJoinWrapper() {
		return new JoinWrapper();
	}
	
	protected SplitWrapper getSplitWrapper() {
		return new SplitWrapper();
	}
	
	protected MilestoneWrapper getMilestoneWrapper() {
		return new MilestoneWrapper();
	}
	
	protected FaultNodeWrapper getFaultNodeWrapper() {
		return new FaultNodeWrapper();
	}
	
	protected TimerWrapper getTimerWrapper() {
		return new TimerWrapper();
	}
	
	protected HumanTaskNodeWrapper getHumanTaskNodeWrapper() {
		return new HumanTaskNodeWrapper();
	}
	
	protected WorkItemWrapper getWorkItemWrapper() {
		return new WorkItemWrapper();
	}
	
	protected EventNodeWrapper getEventNodeWrapper() {
		return new EventNodeWrapper();
	}
	
	public NodeWrapper getNodeWrapper(Node node,IJavaProject project) {
        if (node instanceof StartNode) {
            return getStartNodeWrapper();
        } else if (node instanceof EndNode) {
            return getEndNodeWrapper();
        } else if (node instanceof ActionNode) {
            return getActionWrapper();
        } else if (node instanceof RuleSetNode) {
            return getRuleSetNodeWrapper();
        } else if (node instanceof SubProcessNode) {
            return getSubProcessWrapper();
        } else if (node instanceof ForEachNode) {
            return getForEachNodeWrapper();
        } else if (node instanceof DynamicNode) {
            return getDynamicNodeWrapper();
        } else if (node instanceof StateNode) {
            return getStateNodeWrapper();
        } else if (node instanceof CompositeContextNode) {
            return getCompositeContextNodeWrapper();
        } else if (node instanceof Join) {
            return getJoinWrapper();
        } else if (node instanceof Split) {
            return getSplitWrapper();
        } else if (node instanceof MilestoneNode) {
            return getMilestoneWrapper();
        } else if (node instanceof FaultNode) {
            return getFaultNodeWrapper();
        } else if (node instanceof TimerNode) {
            return getTimerWrapper();
        } else if (node instanceof HumanTaskNode) {
            return getHumanTaskNodeWrapper();
        } else if (node instanceof WorkItemNode) {
            WorkItemWrapper workItemWrapper = getWorkItemWrapper();
            Work work = ((WorkItemNode) node).getWork();
            if (work != null && work.getName() != null) {
                try {
                    WorkDefinition workDefinition =
                        WorkItemDefinitions.getWorkDefinitions(project)
                            .get(work.getName());
                    if (workDefinition == null) {
    //                    DroolsEclipsePlugin.log(
    //                        new IllegalArgumentException("Could not find work definition for work " + work.getName()));
                        workDefinition = new WorkDefinitionImpl();
                        ((WorkDefinitionImpl) workDefinition).setName(work.getName());
                    }
                    workItemWrapper.setWorkDefinition(workDefinition);
                } catch (Throwable t) {
                    // an error might be thrown when parsing the work definitions,
                    // but this should already be displayed to the user
                }
            }
            return workItemWrapper;
        } else if (node instanceof EventNode) {
            return new EventNodeWrapper();
        }
        throw new IllegalArgumentException(
            "Could not find node wrapper for node " + node);
    }    
    
    /**
     * @param process
     * @return
     */
    private static NodeWrapperFactory getNodeWrapperFactory() {
    	NodeWrapperFactory factory = null;
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] extensions = reg
				.getConfigurationElementsFor("org.drools.eclipse.nodeWapperFactoryExtension");
		for (IConfigurationElement element : extensions) {
			try {
				factory = (NodeWrapperFactory) element
						.createExecutableExtension("className");
			} catch (CoreException e) {
				DroolsEclipsePlugin.log(e);
			}
		}
		return (factory == null) ? new NodeWrapperFactory() : factory;
    }

}
