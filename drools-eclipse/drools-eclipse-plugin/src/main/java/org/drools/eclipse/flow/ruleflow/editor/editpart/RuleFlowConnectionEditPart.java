package org.drools.eclipse.flow.ruleflow.editor.editpart;
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

import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementConnectionFactory;
import org.drools.eclipse.flow.common.editor.editpart.ElementConnectionEditPart;
import org.drools.eclipse.flow.ruleflow.core.ConnectionWrapperFactory;
import org.drools.ruleflow.core.Connection;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.PointList;

/**
 * EditPart for an element connection.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class RuleFlowConnectionEditPart extends ElementConnectionEditPart {
    
	protected ElementConnectionFactory getDefaultElementConnectionFactory() {
		return new ConnectionWrapperFactory(Connection.TYPE_NORMAL);
	}
	
    protected IFigure createFigure() {
        PolylineConnection result = (PolylineConnection) super.createFigure();
        if (((ElementConnection) getModel()).getType() == Connection.TYPE_ABORT) {
	        PolygonDecoration decoration = new PolygonDecoration();
	        PointList decorationPointList = new PointList();
	        decorationPointList.addPoint(0,0);
	        decorationPointList.addPoint(-1,1);
	        decorationPointList.addPoint(-2,0);
	        decorationPointList.addPoint(-1,-1);
	        decoration.setTemplate(decorationPointList);
	        result.setSourceDecoration(decoration);
        }
        return result;
    }
}
