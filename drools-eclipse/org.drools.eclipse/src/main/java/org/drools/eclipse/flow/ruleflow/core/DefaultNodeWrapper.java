/*
 * Copyright 2010 JBoss Inc
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

package org.drools.eclipse.flow.ruleflow.core;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.NodeExtension;

public class DefaultNodeWrapper extends AbstractNodeWrapper {

    private static final long serialVersionUID = 510l;
    
    public DefaultNodeWrapper(String nodeClassName) {
        try {
            Class<?> nodeClass = Class.forName(nodeClassName);
            Node node = (Node) nodeClass.newInstance();
            if (node instanceof NodeExtension) {
                NodeExtension nodeExtension = (NodeExtension) node;
                node.setName(nodeExtension.getDefaultName());
            }
            setNode(node);
        } catch (Throwable t) {
            DroolsEclipsePlugin.log(t);
        }
    }

    public boolean acceptsIncomingConnection(ElementConnection connection, ElementWrapper source) {
        return super.acceptsIncomingConnection(connection, source)
            && getIncomingConnections().isEmpty();
    }

    public boolean acceptsOutgoingConnection(ElementConnection connection, ElementWrapper target) {
        return super.acceptsOutgoingConnection(connection, target)
            && getOutgoingConnections().isEmpty();
    }

}
