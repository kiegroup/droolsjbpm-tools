package org.drools.eclipse.debug;

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.reteoo.ReteooStatefulSession;
import org.drools.runtime.rule.AgendaGroup;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * The Agenda View content provider.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class AgendaViewContentProvider extends DroolsDebugViewContentProvider {

    private DroolsDebugEventHandlerView view;

    public AgendaViewContentProvider(DroolsDebugEventHandlerView view) {
        this.view = view;
    }
    
    protected String getEmptyString() {
    	return "The selected working memory has an empty agenda.";
    }

    public Object[] getChildren(Object obj) {
        try {
            Object[] variables = null;
            if (obj != null && obj instanceof IJavaObject
                    && "org.drools.reteoo.ReteooStatefulSession".equals(
                        ((IJavaObject) obj).getReferenceTypeName())) {
                variables = getAgendaElements((IJavaObject) obj);
            } else if (obj instanceof IVariable) {
            	if (view.isShowLogicalStructure()) {
            		IValue value = getLogicalValue(((IVariable) obj).getValue(), new ArrayList());
                	variables = value.getVariables();
                }
            	if (variables == null) {
                	variables = ((IVariable) obj).getValue().getVariables();
                }
            }
            if (variables == null) {
                return new Object[0];
            } else {
                cache(obj, variables);
                return variables;
            }
        } catch (DebugException e) {
            DroolsEclipsePlugin.log(e);
            return new Object[0];
        }
    }
    
    private void testDebugExpression() {
        ReteooStatefulSession session = null;
        AgendaGroup[] agendaGroups = session.getAgenda().getAgendaGroups();
        String focus = session.getAgenda().getFocusName();
    }
    
    private Object[] getAgendaElements(IJavaObject workingMemoryImpl) throws DebugException {
        List result = new ArrayList();
        IValue agendaGroupObjects = DebugUtil.getValueByExpression("return getAgenda().getAgendaGroups();", workingMemoryImpl);
        // Drools 4 code
        IValue focus = DebugUtil.getValueByExpression("return getAgenda().getFocus();", workingMemoryImpl);
        if (focus == null) {
            // Drools 5 code
            focus = DebugUtil.getValueByExpression("return getAgenda().getFocusName();", workingMemoryImpl);
        }
        if (agendaGroupObjects instanceof IJavaArray) {
	        IJavaArray agendaGroupArray = (IJavaArray) agendaGroupObjects;
	    	IJavaValue[] agendaGroupValueArray = agendaGroupArray.getValues();
	        for (int i = 0; i < agendaGroupValueArray.length; i++) {
	        	IJavaValue agendaGroup = agendaGroupValueArray[i];
	        	String name = "";
			    List activationsResult = new ArrayList();
			    IVariable[] agendaGroupVarArray = agendaGroup.getVariables();
	        	for (int j = 0; j < agendaGroupVarArray.length; j++) {
	        		IVariable agendaGroupVar = agendaGroupVarArray[j];
	        		if ("name".equals(agendaGroupVar.getName())) {
	        			name = agendaGroupVar.getValue().getValueString();
	        			break;
	        		}
	        	}
				IJavaArray activations = (IJavaArray) DebugUtil.getValueByExpression("return getActivations();", agendaGroup);
				IJavaValue[] activationArray = activations.getValues();
	            for (int l = 0; l < activationArray.length; l++) {
	            	IJavaValue activation = activationArray[l];
	            	if (activation.getJavaType() != null) {
	            		activationsResult.add(new VariableWrapper("[" + l + "]", 
	            			new LazyActivationWrapper(activations, activation, workingMemoryImpl)));
	            	}
	            }
	        	boolean active = false;
            	if (agendaGroup.equals(focus)) {
            		active = true;
	            }
            	// because the debug view does not handle spaces well, all spaces
            	// in the agenda group name are replaced with '_'s.
            	name = replaceSpaces(name);
	            result.add(new MyVariableWrapper(name + "[" + (active ? "focus" : "nofocus") + "]", 
            		new ObjectWrapper((IJavaObject) agendaGroup,
        				(IJavaVariable[]) activationsResult.toArray(new IJavaVariable[activationsResult.size()]))));
	        }
        }
        return result.toArray(new IVariable[0]);
    }
    
    private String replaceSpaces(String name) {
    	return name.replace(' ', '_');
    }

    private class LazyActivationWrapper extends ObjectWrapper {
    	
    	private IJavaValue activation;
    	private IJavaValue workingMemoryImpl;
    	
    	public LazyActivationWrapper(IJavaObject object, IJavaValue activation, IJavaObject workingMemoryImpl) {
    		super(object, null);
    		this.activation = activation;
    		this.workingMemoryImpl = workingMemoryImpl;
    	}
    	
    	public IVariable[] getVariables() {
    		IVariable[] result = super.getVariables();
    		if (result == null) {
    			try {
	                List variables = new ArrayList();
	                variables.add(new VariableWrapper("ruleName", (IJavaValue) DebugUtil.getValueByExpression("return getRule().getName();", activation)));
	        		String activationId = null;
				    IVariable[] activationVarArray = activation.getVariables();
		        	for (int j = 0; j < activationVarArray.length; j++) {
		        		IVariable activationVar = activationVarArray[j];
		        		if ("activationNumber".equals(activationVar.getName())) {
		        			activationId = activationVar.getValue().getValueString();
		        			break;
		        		}
		        	}
		        	if (activationId != null) {
			        	IValue objects = DebugUtil.getValueByExpression("return getActivationParameters(" + activationId + ");", workingMemoryImpl);
			        	if (objects instanceof IJavaArray) {
			                IJavaArray array = (IJavaArray) objects;
			                IJavaValue[] javaVals = array.getValues();
			                for ( int k = 0; k < javaVals.length; k++ ) {
			                    IJavaValue mapEntry = javaVals[k];
			                    String key = null;
			                    IJavaValue value = null;
			                    
			                    IVariable[] vars = mapEntry.getVariables();
			                    for ( int j = 0; j < vars.length; j++ ) {
			                        IVariable var = vars[j];
			                        if ("key".equals(var.getName())) {
			                            key = var.getValue().getValueString();
			                        } else if ("value".equals(var.getName())) {
			                            value = (IJavaValue) var.getValue();
			                        }
			                    }
			                    variables.add(new VariableWrapper(key, value));
			                }
			                result = (IJavaVariable[]) variables.toArray(new IJavaVariable[variables.size()]);
			        	}
		        	}
    			} catch (Throwable t) {
    				DroolsEclipsePlugin.log(t);
    			}
        		if (result == null) {
        			result = new IJavaVariable[0];
        		}
                setVariables((IJavaVariable[]) result);
    		}
    		return result;
    	}
    	
    	public boolean hasVariables() {
    		return true;
    	}
    	
    	public String getValueString() throws DebugException {
    		return "Activation";
    	}
    	
    	public String getReferenceTypeName() throws DebugException {
    		return "";
    	}
    }
    
    /**
     * Special VariableWrapper that considers variables with the same name
     * as equal.
     */
    private class MyVariableWrapper extends VariableWrapper {
    	
    	public MyVariableWrapper(String name, IJavaValue value) {
    		super(name, value);
    	}
    	
    	public boolean equals(Object obj) {
            if (obj instanceof VariableWrapper) {
                VariableWrapper var = (VariableWrapper) obj;
                return var.getName().equals(getName());
            }
            return false;
        }

        public int hashCode() {
            return getName().hashCode();
        }
    	
    }
    
}
