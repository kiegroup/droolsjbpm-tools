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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.drools.reteoo.BaseVertex;

/**
 * Represents one row in rete graph
 *
 */
public class Row {

    private final int depth;

    //List<BaseVertex>
    private List      vertices;

    /**
     * Default constructor.
     * 
     * @param depth row depth 
     */
    public Row(final int depth) {
        super();
        this.vertices = new ArrayList();
        this.depth = depth;
    }

    /**
     * Returns row depth
     * 
     * @return row depth
     */
    public int getDepth() {
        return this.depth;
    }

    /**
     * Adds new vertex to this row.
     * 
     * @param vertex new vertex to be added 
     */
    public void add(final BaseVertex vertex) {
        this.vertices.add( vertex );
    }

    /**
     * Returns all vertices from this row.
     * 
     * @return list of vertices with type BaseVertex
     */
    public List getVertices() {
        return this.vertices;
    }

    /**
     * @param vertex
     * @return <code>true</code> if vertex is found in row. <code>false</code> otherwise.
     */
    public boolean contains(final BaseVertex vertex) {
        return this.vertices.contains( vertex );
    }

    /**
     * @return number of vertices in row 
     */
    public int getWidth() {
        return this.vertices.size();
    }

    /**
     * Optimizing vertices for optimal presentation
     * 
     */
    public void optimize() {
        final List sorted = new ArrayList( this.vertices );

        Collections.sort( sorted,
                          new Comparator() {
                              public int compare(final Object o1,
                                                 final Object o2) {
                                  final BaseVertex v1 = (BaseVertex) o1;
                                  final BaseVertex v2 = (BaseVertex) o2;

                                  int v1OutDegree = v1.getSourceConnections().size();
                                  int v2OutDegree = v2.getSourceConnections().size();

                                  if ( v1OutDegree < v2OutDegree ) {
                                      return 1;
                                  }

                                  if ( v1OutDegree > v2OutDegree ) {
                                      return -1;
                                  }

                                  return 0;
                              }
                          } );

        final LinkedList optimized = new LinkedList();

        boolean front = false;

        for ( final Iterator vertexIter = sorted.iterator(); vertexIter.hasNext(); ) {
            final BaseVertex vertex = (BaseVertex) vertexIter.next();

            if ( front ) {
                optimized.addFirst( vertex );
            } else {
                optimized.addLast( vertex );
            }

            front = !front;
        }

        this.vertices = optimized;
    }
}