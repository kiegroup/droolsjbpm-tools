/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.drools.eclipse.editors.rete;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.compiler.DrlParser;
import org.drools.compiler.compiler.DroolsParserException;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.core.definitions.InternalKnowledgePackage;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.eclipse.editors.rete.model.ReteGraph;
import org.drools.eclipse.reteoo.AlphaNodeVertex;
import org.drools.eclipse.reteoo.BaseVertex;
import org.drools.eclipse.reteoo.EntryPointNodeVertex;
import org.drools.eclipse.reteoo.LeftInputAdapterNodeVertex;
import org.drools.eclipse.reteoo.ObjectTypeNodeVertex;
import org.drools.eclipse.reteoo.ReteVertex;
import org.drools.eclipse.reteoo.ReteooVisitor;
import org.drools.eclipse.reteoo.RuleTerminalNodeVertex;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * 
 * Integration-like tests
 * 
 * Testing {@link ReteooLayoutFactory}
 * It is using following components:
 * {@link Row},
 * {@link RowList},
 * org.drools.core.reteoo.*Vertex,
 * org.drools.ide.editors.rete.model.*
 */
public class ReteooLayoutFactoryTest {

    /**
     * Test method for {@link org.drools.eclipse.editors.rete.ReteooLayoutFactory#calculateReteRows(org.drools.core.reteoo.BaseVertex)}.
     * @throws IOException 
     * @throws DroolsParserException 
     * @throws PackageIntegrationException 
     * @throws DroolsParserException 
     * @throws PackageIntegrationException 
     */
    @Test
    public void testCalculateReteRows() throws IOException,
                                             DroolsParserException {
        ReteGraph graph = new ReteGraph();
        BaseVertex root = loadRete( graph );
        final RowList rows = ReteooLayoutFactory.calculateReteRows( root );

        int rownum = rows.getDepth();

        assertEquals( 6,
                      rownum );

        int[] expectedDepths = new int[]{-1, 0, 1, 2, 3, 4};
        int[] expectedSizes = new int[]{1, 1, 2, 2, 2, 2};

        for ( int j = 0; j < rownum; j++ ) {
            final Row row = rows.get( j );
            final int rowDepth = row.getDepth();
            assertEquals( expectedDepths[j],
                          rowDepth );
            assertEquals( expectedSizes[j],
                          row.getVertices().size() );
        }

    }

    /**
     * Test method for {@link org.drools.eclipse.editors.rete.ReteooLayoutFactory#layoutRowList(org.drools.eclipse.editors.rete.model.ReteGraph, org.drools.eclipse.editors.rete.RowList)}.
     * 
     * @throws IOException 
     * @throws DroolsParserException 
     * @throws PackageIntegrationException 
     * @throws DroolsParserException 
     * @throws PackageIntegrationException 
     */
    @Test
    public void testLayoutRowList() throws IOException,
                                         DroolsParserException {
        ReteGraph graph = new ReteGraph();
        BaseVertex root = loadRete( graph );
        final RowList rows = ReteooLayoutFactory.calculateReteRows( root );

        ReteooLayoutFactory.layoutRowList( graph,
                                           rows );

        final List nodes = graph.getChildren();

        BaseVertex[] yOrder = (BaseVertex[]) nodes.toArray( new BaseVertex[0] );
        Arrays.sort( yOrder,
                     new Comparator() {
                         public int compare(Object o1,
                                            Object o2) {
                             BaseVertex v1 = (BaseVertex) o1;
                             BaseVertex v2 = (BaseVertex) o2;
                             int y1 = v1.getLocation().y;
                             int y2 = v2.getLocation().y;
                             return new Integer( y1 ).compareTo( new Integer( y2 ) );
                         }

                     } );

        Class[] expectedTypes = new Class[]{ReteVertex.class, EntryPointNodeVertex.class,
            ObjectTypeNodeVertex.class, ObjectTypeNodeVertex.class, AlphaNodeVertex.class, 
            AlphaNodeVertex.class, LeftInputAdapterNodeVertex.class, LeftInputAdapterNodeVertex.class,
            RuleTerminalNodeVertex.class, RuleTerminalNodeVertex.class};

        for ( int i = 0; i < yOrder.length; i++ ) {
            assertEquals( expectedTypes[i],
                          yOrder[i].getClass() );
            if ( i > 0 ) {
                // If current vertex has same type as previous then
                // y-pos should match and x-pos should not match.                
                // If type is different then y-pos should *not* match.

                BaseVertex current = yOrder[i];
                BaseVertex previous = yOrder[i - 1];
                if ( current.getClass().equals( previous.getClass() ) ) {
                    assertEquals( current.getLocation().y,
                                  previous.getLocation().y );
                    assertNotSame( new Integer( current.getLocation().x ),
                                   new Integer( previous.getLocation().x ) );
                } else {
                    assertNotSame( new Integer( current.getLocation().y ),
                                   new Integer( previous.getLocation().y ) );
                }
            }
        }

    }

    private BaseVertex loadRete(ReteGraph graph) throws IOException,
                                                DroolsParserException {
        final InputStream is = getClass().getClassLoader().getResourceAsStream( "simplerule.drl" );
        String drl = streamToString( is );

        DrlParser parser = new DrlParser();
        PackageDescr packageDescr = parser.parse(null, drl);
        KnowledgeBuilderImpl builder = new KnowledgeBuilderImpl();
        builder.addPackage(packageDescr);
        InternalKnowledgePackage pkg = builder.getPackage("org.drools.examples");
        KnowledgeBaseImpl ruleBase = (KnowledgeBaseImpl) KnowledgeBaseFactory.newKnowledgeBase();
        ruleBase.addPackage(pkg);

        final ReteooVisitor visitor = new ReteooVisitor( graph );
        visitor.visit( ruleBase );

        BaseVertex root = visitor.getRootVertex();
        return root;
    }

    private String streamToString(InputStream is) throws IOException {
        byte[] buffer = new byte[4096];
        OutputStream outputStream = new ByteArrayOutputStream();

        while ( true ) {
            int read = is.read( buffer );

            if ( read == -1 ) {
                break;
            }

            outputStream.write( buffer,
                                0,
                                read );
        }

        outputStream.close();
        is.close();

        return outputStream.toString();
    }

    /**
     * Used by simplerule.drl
     */
    public static class Message {
        public static final int HELLO   = 0;
        public static final int GOODBYE = 1;

        private String          message;

        private int             status;

        public String getMessage() {
            return this.message;
        }

        public void setMessage(final String message) {
            this.message = message;
        }

        public int getStatus() {
            return this.status;
        }

        public void setStatus(final int status) {
            this.status = status;
        }
    }

}
