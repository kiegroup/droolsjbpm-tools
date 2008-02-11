package org.drools.eclipse.flow.ruleflow.editor.editpart;

import org.drools.eclipse.flow.common.editor.core.ElementConnectionFactory;
import org.drools.eclipse.flow.common.editor.editpart.ElementConnectionEditPart;
import org.drools.eclipse.flow.ruleflow.core.ConnectionWrapperFactory;

public class ConnectionWrapperEditPart extends ElementConnectionEditPart {

	protected ElementConnectionFactory getDefaultElementConnectionFactory() {
    	return new ConnectionWrapperFactory();
    }
	
}
