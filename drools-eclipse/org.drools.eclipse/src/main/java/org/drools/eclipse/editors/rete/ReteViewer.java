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

import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.eclipse.DRLInfo;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.builder.DroolsBuilder;
import org.drools.eclipse.editors.DRLRuleEditor;
import org.drools.eclipse.editors.rete.model.ReteGraph;
import org.drools.eclipse.editors.rete.part.VertexEditPartFactory;
import org.drools.eclipse.util.ProjectClassLoader;
import org.drools.reteoo.BaseVertex;
import org.drools.reteoo.ReteooRuleBase;
import org.drools.reteoo.ReteooVisitor;
import org.drools.rule.Package;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWT;

/**
 * GEF-based RETE Viewer
 * 
 * @author Ahti Kitsik
 *
 */
public class ReteViewer extends GraphicalEditor {

    public static final String  MSG_PARSE_ERROR         = "Unable to parse rules to show RETE view!";

    private static final int     SIMPLE_ROUTER_MIN_NODES = 100;

    ScalableFreeformRootEditPart rootEditPart            = new ScalableFreeformRootEditPart();

    private ReteGraph            diagram                 = new ReteGraph();

    private boolean              relayoutRequired        = true;
    
    private DRLRuleEditor drlEditor;

    /**
     * Constructor.
     * 
     * @param documentProvider documentProvider must contain Document with rules.
     */
    public ReteViewer(DRLRuleEditor drlEditor) {
        this.drlEditor = drlEditor;
        setEditDomain( new DefaultEditDomain( this ) );
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
     */
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        GraphicalViewer viewer = getGraphicalViewer();
        viewer.getControl().setBackground( ColorConstants.white );
        viewer.setEditPartFactory( new VertexEditPartFactory() );
        viewer.setRootEditPart( rootEditPart );
        viewer.setKeyHandler( new GraphicalViewerKeyHandler( viewer ) );
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class type) {

        if ( type == ZoomManager.class ) return ((ScalableFreeformRootEditPart) getGraphicalViewer().getRootEditPart()).getZoomManager();
        if ( type == GraphicalViewer.class ) return getGraphicalViewer();
        if ( type == EditPart.class && getGraphicalViewer() != null ) return getGraphicalViewer().getRootEditPart();
        if ( type == IFigure.class && getGraphicalViewer() != null ) return ((GraphicalEditPart) getGraphicalViewer().getRootEditPart()).getFigure();
        return super.getAdapter( type );
    }

    /**
     * Loads model from rule base,
     * calculates rete view and initializes diagram model.
     * @param monitor 
     * @param contents 
     * @return
     */
    public ReteGraph loadReteModel(IProgressMonitor monitor,
                                   String contents) throws Throwable {
        if ( relayoutRequired == false ) {
            return diagram;
        }

        ReteGraph newDiagram = new ReteGraph();

        try {

            monitor.beginTask( "Loading RETE Tree",
                               100 );

            monitor.subTask( "Loading Rule Base" );
            ReteooRuleBase ruleBase = null;
            try {
                IResource resource = drlEditor.getResource();
                ClassLoader newLoader = DroolsBuilder.class.getClassLoader();
                if ( resource.getProject().getNature( "org.eclipse.jdt.core.javanature" ) != null ) {
                    IJavaProject project = JavaCore.create( resource.getProject() );
                    newLoader = ProjectClassLoader.getProjectClassLoader( project );
                }
                DRLInfo drlInfo = DroolsEclipsePlugin.getDefault().parseResource(drlEditor, true, true);
                if (drlInfo == null) {
                    throw new Exception( "Could not find DRL info" );
                }
                if (drlInfo.getBuilderErrors().length > 0) {
                    throw new Exception( drlInfo.getBuilderErrors().length + " build errors" );
                }
                if (drlInfo.getParserErrors().size() > 0) {
                    throw new Exception( drlInfo.getParserErrors().size() + " parser errors" );
                }

                Package pkg = drlInfo.getPackage();
                RuleBaseConfiguration config = new RuleBaseConfiguration();
                config.setClassLoader(newLoader);
                ruleBase = (ReteooRuleBase) RuleBaseFactory.newRuleBase(RuleBase.RETEOO, config);
                if (pkg != null) {
                    ruleBase.addPackage(pkg);
                }
            } catch ( Throwable t ) {
                DroolsEclipsePlugin.log( t );
                throw new Exception( MSG_PARSE_ERROR + " " + t.getMessage());
            }

            monitor.worked( 50 );
            if ( monitor.isCanceled() ) {
                throw new InterruptedException();
            }

            monitor.subTask( "Building RETE Tree" );
            final ReteooVisitor visitor = new ReteooVisitor( newDiagram );
            visitor.visit( ruleBase );
            monitor.worked( 30 );
            if ( monitor.isCanceled() ) {
                throw new InterruptedException();
            }

            monitor.subTask( "Calculating RETE Tree Layout" );
            BaseVertex rootVertex = visitor.getRootVertex();
            RowList rowList = ReteooLayoutFactory.calculateReteRows( rootVertex );
            ReteooLayoutFactory.layoutRowList( newDiagram,
                                               rowList );
            zeroBaseDiagram( newDiagram );
            monitor.worked( 20 );
            if ( monitor.isCanceled() ) {
                throw new InterruptedException();
            }
            monitor.done();

        } catch ( Throwable t ) {
            if ( !(t instanceof InterruptedException) ) {
                DroolsEclipsePlugin.log( t );
            }
            throw t;
        }
        relayoutRequired = false;
        return newDiagram;
    }

    private ReteGraph getModel() {
        return diagram;
    }

    /**
     * Loads Rete model and initializes zoom manager.
     * 
     */
    protected void initializeGraphicalViewer() {
        ZoomManager zoomManager = rootEditPart.getZoomManager();

        //List<String>
        List zoomLevels = new ArrayList( 3 );

        zoomLevels.add( ZoomManager.FIT_ALL );
        zoomLevels.add( ZoomManager.FIT_HEIGHT );
        zoomLevels.add( ZoomManager.FIT_WIDTH );

        zoomManager.setZoomLevelContributions( zoomLevels );

        // Zoom mousewheel - Ctrl+Mousewheel for zoom in/out
        getGraphicalViewer().setProperty( MouseWheelHandler.KeyGenerator.getKey( SWT.MOD1 ),
                                          MouseWheelZoomHandler.SINGLETON );

    }

    /**
     * Moves all <code>diagram</code> nodes to upper left corner
     * and shifting to right if neccessary to get rid of negative XY coordinates.
     * 
     */
    private void zeroBaseDiagram(ReteGraph graph) {

        Dimension dim = rootEditPart.getContentPane().getSize();

        int minx = 0, miny = 0, maxx = 0, x = dim.width;

        final Iterator nodeIter = graph.getChildren().iterator();
        while ( nodeIter.hasNext() ) {
            Point loc = ((BaseVertex) (nodeIter.next())).getLocation();
            minx = Math.min( loc.x,
                             minx );
            maxx = Math.max( loc.x,
                             maxx );
            miny = Math.min( loc.y,
                             miny );
        }

        int delta = (x - (maxx - minx + 20)) / 2;
        minx = minx - (delta);

        final Iterator nodeIter2 = graph.getChildren().iterator();
        while ( nodeIter2.hasNext() ) {
            final BaseVertex vertex = (BaseVertex) (nodeIter2.next());
            Point loc = vertex.getLocation();
            vertex.setLocation( new Point( loc.x - minx,
                                           loc.y - miny ) );
        }
    }

    /**
     * No save operation in ReteViewer
     */
    public void doSave(IProgressMonitor monitor) {

    }

    /**
     * ReteViewer is never dirty.
     * This prevents editor close mechanism to ask file save confirmation
     * even after one of the vertices is moved.
     */
    public boolean isDirty() {
        return false;
    }

    /**
     * Fired when underlying source is modified.
     * Marks graph viewer to be relayouted when activated.
     */
    public void fireDocumentChanged() {
        relayoutRequired = true;
    }

    /**
     * Draws graph.
     * 
     * @param newGraph used to replace existing graph. if null then existing graph is simply redrawn.
     */
    public void drawGraph(ReteGraph newGraph) {

        LayerManager manager = (LayerManager) getGraphicalViewer().getEditPartRegistry().get( LayerManager.ID );
        ConnectionLayer connLayer = (ConnectionLayer) manager.getLayer( LayerConstants.CONNECTION_LAYER );

        // Lazy-init model initialization
        if ( getGraphicalViewer().getContents() == null ) {
            getGraphicalViewer().setContents( getModel() );
        }

        final boolean isNewDiagram = newGraph != null && newGraph != diagram;

        if ( isNewDiagram ) {
            diagram.removeAll();
        }

        // Update connection router according to new model size
        ConnectionRouter router;
        if ( (isNewDiagram && newGraph.getChildren().size() < SIMPLE_ROUTER_MIN_NODES) || (!isNewDiagram && getModel().getChildren().size() < SIMPLE_ROUTER_MIN_NODES) ) {
            router = new ShortestPathConnectionRouter( (IFigure) rootEditPart.getContentPane().getChildren().get( 0 ) );
        } else {
            router = ConnectionRouter.NULL;
        }
        connLayer.setConnectionRouter( router );

        if ( newGraph != null && newGraph != diagram ) {
            diagram.addAll( newGraph.getChildren() );
        }

    }

}
