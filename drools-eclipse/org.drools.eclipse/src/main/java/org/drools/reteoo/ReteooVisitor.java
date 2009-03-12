package org.drools.reteoo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.common.BaseNode;
import org.drools.eclipse.editors.rete.model.Connection;
import org.drools.eclipse.editors.rete.model.ReteGraph;
import org.drools.util.ObjectHashMap;
import org.drools.util.ReflectiveVisitor;

/**
 * Produces a graph in GraphViz DOT format.
 *
 * @see http://www.research.att.com/sw/tools/graphviz/ 
 * @see http://www.pixelglow.com/graphviz/
 *
 * @author Andy Barnett
 */
public class ReteooVisitor extends ReflectiveVisitor {

    private static final String PACKAGE_NAME = "org.drools.reteoo.";

    /**
     * Keeps track of visited JoinNode DOT IDs. This mapping allows the visitor
     * to recognize JoinNodes it has already visited and as a consequence link
     * existing nodes back together. This is vital to the Visitor being able to
     * link two JoinNodeInputs together through their common JoinNode.
     */
    private final Map           visitedNodes = new HashMap();

    private ReteGraph           graph;

    private BaseVertex          rootVertex;

    private BaseVertex          parentVertex;

    /**
     * Constructor.
     */
    public ReteooVisitor(final ReteGraph graph) {
        this.graph = graph;
    }

    public ReteGraph getGraph() {
        return this.graph;
    }

    public BaseVertex getRootVertex() {
        return this.rootVertex;
    }

    /**
     * RuleBaseImpl visits its Rete.
     */
    public void visitReteooRuleBase(final ReteooRuleBase ruleBase) {
        visit( (ruleBase).getRete() );
    }

    /**
     * Rete visits each of its ObjectTypeNodes.
     */
    public void visitRete(final Rete rete) {
        this.rootVertex = (ReteVertex) this.visitedNodes.get( dotId( rete ) );
        if ( this.rootVertex == null ) {
            this.rootVertex = new ReteVertex( rete );
            this.visitedNodes.put( dotId( rete ),
                                   this.rootVertex );
        }

        this.graph.addChild( this.rootVertex );
        this.parentVertex = this.rootVertex;

        for( EntryPointNode node : rete.getEntryPointNodes().values() ) {
            visit( node );
        }
    }

    public void visitBaseNode(final BaseNode node) {
        BaseVertex vertex = (BaseVertex) this.visitedNodes.get( dotId( node ) );
        if ( vertex == null ) {
            try {
                String name = node.getClass().getName();
                name = name.substring( name.lastIndexOf( '.' ) + 1 ) + "Vertex";
                final Class clazz = Class.forName( PACKAGE_NAME + name );
                vertex = (BaseVertex) clazz.getConstructor( new Class[]{node.getClass()} ).newInstance( new Object[]{node} );
            } catch ( final Exception e ) {
                throw new RuntimeException( "problem visiting vertex " + node.getClass().getName(),
                                            e );
            }
            this.graph.addChild( vertex );
            this.visitedNodes.put( dotId( node ),
                                   vertex );

            new Connection( this.parentVertex,
                            vertex );

            final BaseVertex oldParentVertex = this.parentVertex;
            this.parentVertex = vertex;

            List list = null;
            if ( node instanceof EntryPointNode ) {
            	list = new ArrayList( ((EntryPointNode) node).getObjectTypeNodes().values() );
            } else if ( node instanceof ObjectSource ) {
                list = Arrays.asList( ((ObjectSource) node).getSinkPropagator().getSinks() );
            } else if ( node instanceof LeftTupleSource ) {
                list = Arrays.asList( ((LeftTupleSource) node).getSinkPropagator().getSinks() );
            }

            if ( list != null ) {
                for ( final java.util.Iterator it = list.iterator(); it.hasNext(); ) {
                    final Object nextNode = it.next();
                    visitNode( nextNode );
                }
            }
            this.parentVertex = oldParentVertex;
        } else {
            new Connection( this.parentVertex,
                            vertex );
        }
    }

    /**
     * Helper method to ensure nodes are not visited more than once.
     */
    private void visitNode(final Object node) {
        Object realNode = node;
        if ( node instanceof ObjectHashMap.ObjectEntry ) {
            ObjectHashMap.ObjectEntry entry = (ObjectHashMap.ObjectEntry) node;
            realNode = entry.getValue();
        }
        visit( realNode );
    }

    /**
     * The identity hashCode for the given object is used as its unique DOT
     * identifier.
     */
    private static String dotId(final Object object) {
        return Integer.toHexString( System.identityHashCode( object ) ).toUpperCase();
    }

}
