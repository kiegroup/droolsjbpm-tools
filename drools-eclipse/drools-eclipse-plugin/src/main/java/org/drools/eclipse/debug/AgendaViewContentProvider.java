package org.drools.eclipse.debug;

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
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
                    && "org.drools.reteoo.ReteooWorkingMemory".equals(
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
    
    private Object[] getAgendaElements(IJavaObject workingMemoryImpl) throws DebugException {
        List result = new ArrayList();
        IValue agendaGroupObjects = DebugUtil.getValueByExpression("return getAgenda().getAgendaGroups();", workingMemoryImpl);
        IValue focus = DebugUtil.getValueByExpression("return getAgenda().getFocus();", workingMemoryImpl);
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
	            		activationsResult.add(new VariableWrapper("[" + l + "]", activation));
	            	}
	            }
	        	boolean active = false;
            	if (agendaGroup.equals(focus)) {
            		active = true;
	            }
            	// because the debug view does not handle spaces well, all spaces
            	// in the agenda group name are replaced with '_'s.
            	name = replaceSpaces(name);
	            result.add(new VariableWrapper(name + "[" + (active ? "focus" : "nofocus") + "]", 
	            		new ObjectWrapper((IJavaObject) agendaGroup,
	        				(IJavaVariable[]) activationsResult.toArray(new IJavaVariable[activationsResult.size()]))));
	        }
        }
        return result.toArray(new IVariable[0]);
    }
    
    private String replaceSpaces(String name) {
    	return name.replace(' ', '_');
    }
}
