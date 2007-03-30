package org.drools.eclipse.editors.rete;

/*
 * Copyright 2006 JBoss Inc
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.drools.reteoo.BaseVertex;

/**
 * Holder for Row elements.
 * 
 */
public class RowList {

    // List<Row>
    private List rows;

    /**
     * Default constructor.
     */
    public RowList() {
        super();
        this.rows = new ArrayList();
    }

    /**
     * Adds new vertex to specified depth
     * 
     * @param depth depth for vertex
     * @param vertex vertex
     */
    public void add(final int depth,
                    final BaseVertex vertex) {
        if ( this.rows.size() < (depth + 1) ) {
            final int addRows = depth - this.rows.size() + 1;

            for ( int i = 0; i < addRows; ++i ) {
                this.rows.add( new Row( (depth - addRows) + i ) );
            }
        }

        ((Row) this.rows.get( depth )).add( vertex );
    }

    /**
     * @return number of rows in RowList
     */
    public int getDepth() {
        return this.rows.size();
    }

    /**
     * @param row index of the row to be returned (0..n-1)
     * @return specified row
     */
    public Row get(final int row) {
        return (Row) this.rows.get( row );
    }

    /**
     * Finds specified vertex from the rows.
     * 
     * @param vertex vertex
     * 
     * @return row number where vertex was found (0..n-1). <code>-1</code> if not found. 
     */
    public int getRow(final BaseVertex vertex) {
        final int numRows = this.rows.size();

        for ( int i = 0; i < numRows; ++i ) {
            if ( ((Row) this.rows.get( i )).contains( vertex ) ) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Finds the longest row width.
     * 
     * @return width of the longest row
     */
    public int getWidth() {
        int width = 0;

        for ( final Iterator rowIter = this.rows.iterator(); rowIter.hasNext(); ) {
            final Row row = (Row) rowIter.next();
            final int rowWidth = row.getWidth();

            if ( rowWidth > width ) {
                width = rowWidth;
            }
        }

        return width;
    }

    /**
     * Width of the row at specified index.
     * 
     * @param row
     * @return width
     */
    public int getWidth(final int row) {
        return ((Row) this.rows.get( row )).getWidth();
    }

    /**
     * @param vertex vertex to search
     * @return column where vertex was found
     */
    public int getColumn(final BaseVertex vertex) {
        final int row = getRow( vertex );

        if ( row < 0 ) {
            return -1;
        }

        final List rowVertices = get( row ).getVertices();

        final int numCols = rowVertices.size();

        for ( int i = 0; i < numCols; ++i ) {
            if ( rowVertices.get( i ).equals( vertex ) ) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Dumps all row vertices to System.err
     */
    public void dump() {
        final int numRows = this.rows.size();

        for ( int i = 0; i < numRows; ++i ) {
            System.err.println( i + ": " + get( i ).getVertices() );
        }
    }

    /**
     * Optimizes all rows for optimal presentation
     */
    public void optimize() {
        final int numRows = this.rows.size();

        for ( int i = 0; i < numRows; ++i ) {
            get( i ).optimize();
        }
    }
}