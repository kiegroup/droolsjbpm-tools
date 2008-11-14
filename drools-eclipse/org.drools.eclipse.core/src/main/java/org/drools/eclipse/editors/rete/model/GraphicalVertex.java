package org.drools.eclipse.editors.rete.model;

import java.util.ArrayList;
import java.util.List;

import org.drools.spi.Constraint;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Visual vertex representation for ReteGraph.
 * 
 * Base impl for all rete vertices.
 * 
 * Can be connected with another BaseVertex with a Connector.
 * 
 * @author Ahti Kitsik
 * 
 */
abstract public class GraphicalVertex extends ModelElement {

    private static final String          VERTEX_NAME             = "BaseVertex ";

    private static final String          WIDTH                   = "Width";

    private static final String          HEIGHT                  = "Height";

    private static IPropertyDescriptor[] descriptors;

    /** ID for the Height property value (used for by the corresponding property descriptor). */
    private static final String          HEIGHT_PROP             = VERTEX_NAME + "." + HEIGHT;

    /** ID for the Width property value (used for by the corresponding property descriptor). */
    private static final String          WIDTH_PROP              = VERTEX_NAME + "." + WIDTH;

    /** Property ID to use when the location of this shape is modified. */
    public static final String           LOCATION_PROP           = VERTEX_NAME + ".Location";

    /** Property ID to use then the size of this shape is modified. */
    public static final String           SIZE_PROP               = VERTEX_NAME + ".Size";

    /** Property ID to use when the list of outgoing connections is modified. */
    public static final String           SOURCE_CONNECTIONS_PROP = VERTEX_NAME + ".SourceConn";

    /** Property ID to use when the list of incoming connections is modified. */
    public static final String           TARGET_CONNECTIONS_PROP = VERTEX_NAME + ".TargetConn";

    /** ID for the X property value (used for by the corresponding property descriptor).  */
    private static final String          XPOS_PROP               = VERTEX_NAME + ".xPos";

    /** ID for the Y property value (used for by the corresponding property descriptor).  */
    private static final String          YPOS_PROP               = VERTEX_NAME + ".yPos";

    /*
     * Initializes the property descriptors array.
     * @see #getPropertyDescriptors()
     * @see #getPropertyValue(Object)
     * @see #setPropertyValue(Object, Object)
     */
    static {
        descriptors = new IPropertyDescriptor[]{new TextPropertyDescriptor( XPOS_PROP,
                                                                            "X" ), // id and description pair
            new TextPropertyDescriptor( YPOS_PROP,
                                        "Y" ), new TextPropertyDescriptor( WIDTH_PROP,
                                                                           WIDTH ), new TextPropertyDescriptor( HEIGHT_PROP,
                                                                                                                HEIGHT ),};
    } // static

    /** Location of this vertex. */
    private Point                        location                = new Point( 0,
                                                                              0 );
    /** Size of this vertex. */
    private final static Dimension       size                    = new Dimension( 16,
                                                                                  16 );
    /** List of outgoing Connections. */
    private List                         sourceConnections       = new ArrayList();
    /** List of incoming Connections. */
    private List                         targetConnections       = new ArrayList();

    /**
     * HTML formatted representation of this node
     * 
     * @return #getHtml
     */
    abstract public String getHtml();

    /**
     * Color used for filling vertex figure
     * 
     * @return color
     */
    abstract public Color getFillColor();

    /**
     * Add an incoming or outgoing connection to this vertex.
     * @param conn a non-null connection instance
     * @throws IllegalArgumentException if the connection is null or has not distinct endpoints
     */
    public void addConnection(Connection conn) {
        if ( conn == null || conn.getSource() == conn.getTarget() ) {
            throw new IllegalArgumentException();
        }
        if ( conn.getSource() == this ) {
            sourceConnections.add( conn );
            firePropertyChange( SOURCE_CONNECTIONS_PROP,
                                null,
                                conn );
        } else if ( conn.getTarget() == this ) {
            targetConnections.add( conn );
            firePropertyChange( TARGET_CONNECTIONS_PROP,
                                null,
                                conn );
        }
    }

    /**
     * Return the Location of this vertex.
     * 
     * @return a non-null copy of location instance
     */
    public Point getLocation() {
        return location.getCopy();
    }

    /**
     * Returns an array of IPropertyDescriptors for this vertex.
     * 
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return descriptors;
    }

    /**
     * Return the property value for the given propertyId, or null.
     */
    public Object getPropertyValue(Object propertyId) {
        if ( XPOS_PROP.equals( propertyId ) ) {
            return Integer.toString( location.x );
        }
        if ( YPOS_PROP.equals( propertyId ) ) {
            return Integer.toString( location.y );
        }
        if ( HEIGHT_PROP.equals( propertyId ) ) {
            return Integer.toString( size.height );
        }
        if ( WIDTH_PROP.equals( propertyId ) ) {
            return Integer.toString( size.width );
        }
        return null;
    }

    /**
     * Return the Size of this vertex.
     * @return a non-null copy of Dimension instance
     */
    public Dimension getSize() {
        return size.getCopy();
    }

    /**
     * Return a List of outgoing Connections.
     */
    public List getSourceConnections() {
        return new ArrayList( sourceConnections );
    }

    /**
     * Return a List of incoming Connections.
     */
    public List getTargetConnections() {
        return new ArrayList( targetConnections );
    }

    /**
     * Remove an incoming or outgoing connection from this vertex.
     * 
     * @param conn a non-null connection instance
     * @throws IllegalArgumentException if the parameter is null
     */
    public void removeConnection(Connection conn) {
        if ( conn == null ) {
            throw new IllegalArgumentException();
        }
        if ( conn.getSource() == this ) {
            sourceConnections.remove( conn );
            firePropertyChange( SOURCE_CONNECTIONS_PROP,
                                null,
                                conn );
        } else if ( conn.getTarget() == this ) {
            targetConnections.remove( conn );
            firePropertyChange( TARGET_CONNECTIONS_PROP,
                                null,
                                conn );
        }
    }

    /**
     * Set the Location of this vertex.
     * @param newLocation a non-null Point instance
     * @throws IllegalArgumentException if the parameter is null
     */
    public void setLocation(Point newLocation) {
        if ( newLocation == null ) {
            throw new IllegalArgumentException();
        }
        location.setLocation( newLocation );
        firePropertyChange( LOCATION_PROP,
                            null,
                            location );
    }

    /**
     * Set the property value for the given property id.
     */
    public void setPropertyValue(Object propertyId,
                                 Object value) {
        if ( XPOS_PROP.equals( propertyId ) ) {
            int x = Integer.parseInt( (String) value );
            setLocation( new Point( x,
                                    location.y ) );
        } else if ( YPOS_PROP.equals( propertyId ) ) {
            int y = Integer.parseInt( (String) value );
            setLocation( new Point( location.x,
                                    y ) );
        } else if ( HEIGHT_PROP.equals( propertyId ) ) {
            int height = Integer.parseInt( (String) value );
            setSize( new Dimension( size.width,
                                    height ) );
        } else if ( WIDTH_PROP.equals( propertyId ) ) {
            int width = Integer.parseInt( (String) value );
            setSize( new Dimension( width,
                                    size.height ) );
        }
    }

    /**
     * Set the Size of this vertex.
     * Will not update the size if newSize is null.
     * @param newSize a non-null Dimension instance or null
     */
    public void setSize(Dimension newSize) {
        if ( newSize != null ) {
            size.setSize( newSize );
            firePropertyChange( SIZE_PROP,
                                null,
                                size );
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return VERTEX_NAME + hashCode();
    }

    /**
     * Color used for borders
     * 
     * @return draw color
     */
    public Color getDrawColor() {
        return ColorConstants.black;
    }

    /**
     * Constructs constraints string
     * 
     * @param constraints array of constraints 
     * @return html-formatted constraints representation
     */
    public static String dumpConstraints(final Constraint[] constraints) {
        if ( constraints == null ) {
            return null;
        }
        final StringBuffer buffer = new StringBuffer();
        for ( int i = 0, length = constraints.length; i < length; i++ ) {
            buffer.append( constraints[i].toString() + "<br>" );
        }
        return buffer.toString();
    }

}
