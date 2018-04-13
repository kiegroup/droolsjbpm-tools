/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.eclipse.flow.ruleflow.editor.editpart;

import org.drools.eclipse.flow.common.editor.core.ElementConnectionFactory;
import org.drools.eclipse.flow.common.editor.editpart.ElementConnectionEditPart;
import org.drools.eclipse.flow.ruleflow.core.ConnectionWrapper;
import org.drools.eclipse.flow.ruleflow.core.ConnectionWrapperFactory;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionEndpointLocator;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.jbpm.workflow.core.node.Split;

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
        String label = (String) getConnectionWrapper().getConnection().getMetaData().get("label");
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
        String label = (String) getConnectionWrapper().getConnection().getMetaData().get("label");
        if (sourceLabel != null) {
            sourceLabel.setText(label == null ? "" : label);
        } else if (label != null) {
            Connection connection = (Connection) getFigure();
            ConnectionEndpointLocator endpointLocator = new ConnectionEndpointLocator(connection, true);
            endpointLocator.setVDistance(15);
            sourceLabel = new Label(label);
            connection.add(sourceLabel, endpointLocator);
        }
        if (sourceLabel != null && getConnectionWrapper().getConnection().getFrom() instanceof Split) {
            Split split = (Split)getConnectionWrapper().getConnection().getFrom();
            if (split.getType() == Split.TYPE_OR ||  split.getType() == Split.TYPE_XOR) {
                String constraint = split.getConstraint(getConnectionWrapper().getConnection()).getConstraint();
                if (constraint == null || constraint.length() == 0) {
                    sourceLabel.setToolTip(null);
                } else {
                    if (sourceLabel.getToolTip() == null) {
                        Label tooltip = new Label();
                        sourceLabel.setToolTip(tooltip);
                    }
                    ((Label) sourceLabel.getToolTip()).setText(constraint);                                       
                }
            }
        }
    }
    
}
