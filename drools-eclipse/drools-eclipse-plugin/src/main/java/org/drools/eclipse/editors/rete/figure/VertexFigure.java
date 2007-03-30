package org.drools.eclipse.editors.rete.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * Figure representing BaseVertex
 *
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
