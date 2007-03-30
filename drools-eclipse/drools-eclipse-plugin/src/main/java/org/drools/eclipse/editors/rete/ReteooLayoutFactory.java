package org.drools.eclipse.editors.rete;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.drools.eclipse.editors.rete.model.Connection;
import org.drools.eclipse.editors.rete.model.ReteGraph;
import org.drools.reteoo.BaseVertex;
import org.eclipse.draw2d.geometry.Point;

/**
 * Factory methods for calculating and layouting reteoo graph
 *
 */
public class ReteooLayoutFactory {

    /**
     * Calculates layouting for provided graph.
     * 
     * @param root graph root vertex
     * 
     * @return Optimally layouted rows from provided graph
     */
    public static RowList calculateReteRows(BaseVertex root) {
        RowList rowList;
        rowList = new RowList();

        rowList.add( 0,
                     root );

        int curRow = 0;

        final Set seenVertices = new HashSet();
        seenVertices.add( root );

        while ( curRow < rowList.getDepth() ) {
            final List rowVertices = rowList.get( curRow ).getVertices();

            for ( final Iterator rowNodeIter = rowVertices.iterator(); rowNodeIter.hasNext(); ) {
                final BaseVertex rowNode = (BaseVertex) rowNodeIter.next();

                final List edges = rowNode.getSourceConnections();

                for ( final Iterator edgeIter = edges.iterator(); edgeIter.hasNext(); ) {

                    final Connection edge = (Connection) edgeIter.next();
                    final BaseVertex destNode = edge.getOpposite( rowNode );

                    if ( !seenVertices.contains( destNode ) ) {
                        rowList.add( curRow + 1,
                                     destNode );
                        seenVertices.add( destNode );
                    }
                }

                seenVertices.add( rowNode );
            }

            ++curRow;
        }

        rowList.optimize();

        return rowList;
    }

    /**
     * Adds all vertices from rowList to the graph.
     * 
     * @param graph
     * @param rowList
     */
    public static void layoutRowList(ReteGraph graph,
                                     RowList rowList) {
        new LayoutCalculator( graph,
                              rowList );
    }

    private static class LayoutCalculator {

        public final static String COORDS                = "drools.LayoutCalculator.coords";

        private static final int   COLUMN_SPACE          = 40;
        private static final int   ROW_HEIGHT_MULTIPLIER = 6;

        private RowList            rowList;

        private int                columnWidth;
        private int                rowHeight;

        private ReteGraph          graph;

        private LayoutCalculator(final ReteGraph graph,
                                 final RowList rowList) {
            this.graph = graph;
            this.rowList = rowList;
            computeSize();

            List vertices = getGraph().getChildren();
            Iterator iter = vertices.iterator();
            while ( iter.hasNext() ) {
                BaseVertex v = (BaseVertex) iter.next();
                initialize_local_vertex( v );
            }

        }

        private void computeSize() {
            final List vertices = getGraph().getChildren();

            for ( final Iterator vertexIter = vertices.iterator(); vertexIter.hasNext(); ) {
                final BaseVertex vertex = (BaseVertex) vertexIter.next();

                final int width = vertex.getSize().width;
                final int height = vertex.getSize().height;

                if ( width > this.columnWidth ) {
                    this.columnWidth = width;
                }

                if ( height > this.rowHeight ) {
                    this.rowHeight = height;
                }
            }

            this.columnWidth = this.columnWidth + LayoutCalculator.COLUMN_SPACE;
        }

        private void initialize_local_vertex(final BaseVertex vertex) {
            final int row = this.rowList.getRow( vertex );
            final int col = this.rowList.getColumn( vertex );

            final int rowWidth = this.rowList.getWidth( row );

            final int columnWidthPx = columnWidth;
            final int rowHeightPx = rowHeight;

            double x = (col * columnWidthPx);
            double y = (row * (rowHeightPx * LayoutCalculator.ROW_HEIGHT_MULTIPLIER));

            x = x + (columnWidthPx / 2) - ((rowWidth - 1) * (columnWidthPx / 2));
            y = y + (rowHeightPx / 2) + 3;

            vertex.setLocation( new Point( x,
                                           y ) );
        }

        private ReteGraph getGraph() {
            return graph;
        }
    }

}
