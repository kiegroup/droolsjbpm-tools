package org.drools.eclipse.flow.common.editor.action;
/*
 * Copyright 2005 JBoss Inc
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

import java.util.Map;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.graph.DirectedGraph;
import org.eclipse.draw2d.graph.Node;

/**
 * Action for auto layouting a RuleFlow.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class HorizontalAutoLayoutAction extends VerticalAutoLayoutAction {

    protected DirectedGraph createDirectedGraph(Map<Long, Node> mapping) {
        DirectedGraph graph = super.createDirectedGraph(mapping);
        graph.setDirection(PositionConstants.HORIZONTAL);
        return graph;
    }

}
