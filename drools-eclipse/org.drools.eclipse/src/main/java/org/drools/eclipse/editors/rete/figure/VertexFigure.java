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

package org.drools.eclipse.editors.rete.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * Figure representing BaseVertex
 */
public class VertexFigure extends Figure {

    final private Color backgroundColor;
    final private Color borderColor;

    /**
     * Initializing Figure
     * 
     * @param backgroundColor background color
     * @param borderColor border color
     */
    public VertexFigure(Color backgroundColor,
                        Color borderColor) {
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
    }

    /**
     * Painting antialiased vertex
     */
    public void paint(Graphics g) {
        g.setAntialias( SWT.ON );
        Rectangle r = getBounds().getCopy();
        g.translate( r.getLocation() );
        g.setBackgroundColor( backgroundColor );
        g.setForegroundColor( borderColor );
        g.fillArc( 0,
                   0,
                   15,
                   15,
                   0,
                   360 );
        g.drawArc( 0,
                   0,
                   14,
                   14,
                   0,
                   360 );
        super.paint( g );
    }

}
