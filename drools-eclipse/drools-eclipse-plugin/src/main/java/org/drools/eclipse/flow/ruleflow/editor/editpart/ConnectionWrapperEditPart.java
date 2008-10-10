package org.drools.eclipse.flow.ruleflow.editor.editpart;

import org.drools.eclipse.flow.common.editor.core.ElementConnectionFactory;
import org.drools.eclipse.flow.common.editor.editpart.ElementConnectionEditPart;
import org.drools.eclipse.flow.ruleflow.core.ConnectionWrapper;
import org.drools.eclipse.flow.ruleflow.core.ConnectionWrapperFactory;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionEndpointLocator;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

public class ConnectionWrapperEditPart extends ElementConnectionEditPart {
	
	private Label sourceLabel;

	protected ElementConnectionFactory getDefaultElementConnectionFactory() {
    	return new ConnectionWrapperFactory();
    }
	
	public ConnectionWrapper getConnectionWrapper() {
		return (ConnectionWrapper) getModel();
	}
	
    protected IFigure createFigure() {
        Connection result = (Connection) super.createFigure();
        // add connection label
        String label = (String) getConnectionWrapper().getConnection().getMetaData("label");
        if (label != null) {
	        ConnectionEndpointLocator sourceEndpointLocator = new ConnectionEndpointLocator(result, true);
	        sourceEndpointLocator.setVDistance(15);
	        sourceLabel = new Label(label);
	        result.add(sourceLabel, sourceEndpointLocator);
        }
        return result;
    }
    
    protected void refreshLabel() {
    	super.refreshLabel();
    	String label = (String) getConnectionWrapper().getConnection().getMetaData("label");
        if (sourceLabel != null) {
        	sourceLabel.setText(label == null ? "" : label);
        } else if (label != null) {
        	Connection connection = (Connection) getFigure();
        	ConnectionEndpointLocator endpointLocator = new ConnectionEndpointLocator(connection, true);
	        endpointLocator.setVDistance(15);
	        sourceLabel = new Label(label);
	        connection.add(sourceLabel, endpointLocator);
        }
    }
    
}
