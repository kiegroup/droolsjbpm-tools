package org.drools.eclipse.editors.rete.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.swt.SWT;

/**
 * ConnectionFigure between two Vertices. 
 *
 */
public class ConnectionFigure extends PolylineConnection {

    /**
     * Painting antialiased connector
     */
    public void paint(Graphics g) {
        g.setAntialias( SWT.ON );
        super.paint( g );
    }
}
