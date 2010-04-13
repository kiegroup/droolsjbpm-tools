package org.drools.eclipse.editors.rete.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.reteoo.AccumulateNodeVertex;
import org.drools.reteoo.AlphaNodeVertex;
import org.drools.reteoo.BaseVertex;
import org.drools.reteoo.EntryPointNodeVertex;
import org.drools.reteoo.EvalConditionNodeVertex;
import org.drools.reteoo.ExistsNodeVertex;
import org.drools.reteoo.FromNodeVertex;
import org.drools.reteoo.JoinNodeVertex;
import org.drools.reteoo.LeftInputAdapterNodeVertex;
import org.drools.reteoo.NotNodeVertex;
import org.drools.reteoo.ObjectTypeNodeVertex;
import org.drools.reteoo.PropagationQueuingNodeVertex;
import org.drools.reteoo.QueryTerminalNodeVertex;
import org.drools.reteoo.ReteVertex;
import org.drools.reteoo.RightInputAdapterNodeVertex;
import org.drools.reteoo.RuleTerminalNodeVertex;
import org.drools.spi.Constraint;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Providing IPropertySource for property sheets that show
 * rete graph vertex properties.
 * 
 */
public class VertexPropertySource
    implements
    IPropertySource {

    private static final String       VERTEX_FROM                = "From BaseVertex";

    private static final String       VERTEX_ACCUMULATE          = "Accumulate BaseVertex";

    private static final String       VERTEX_COLLECT             = "Collect BaseVertex";

    private static final String       VERTEX_EXISTS              = "Exists BaseVertex";

    private static final String       VERTEX_TERMINAL            = "Terminal BaseVertex";

    private static final String       VERTEX_QUERY_TERMINAL      = "Query Terminal BaseVertex";

    private static final String       VERTEX_EVAL_CONDITION      = "Eval Condition BaseVertex";

    private static final String       VERTEX_NOT                 = "Not BaseVertex";

    private static final String       VERTEX_JOIN                = "Join BaseVertex";

    private static final String       VERTEX_RIGHT_INPUT_ADAPTER = "Right Input Adapter BaseVertex";

    private static final String       VERTEX_LEFT_INPUT_ADAPTER  = "Left Input Adapter BaseVertex";

    private static final String       VERTEX_ALPHA               = "Alpha BaseVertex";

    private static final String       VERTEX_OBJECT_TYPE         = "Object Type BaseVertex";

    private static final String       VERTEX_RETE                = "Rete BaseVertex";

    private static final String       VERTEX_ENTRY_POINT         = "Entry Point BaseVertex";

    private static final String       VERTEX_PROPAGATION_QUEUING = "Propagation Queuing BaseVertex";

    private static final String       CONSTRAINT_CAP             = "Constraint";

    private static final String       CONSTRAINT                 = "constraint";

    public String                     ID_ROOT                    = "vertex";                               //$NON-NLS-1$

    public String                     ID_ID                      = "id";                                   //$NON-NLS-1$
    public String                     ID_HTML                    = "html";                                 //$NON-NLS-1$

    private IPropertyDescriptor[]     descriptors;

    private final IPropertyDescriptor PROP_NAME                  = new PropertyDescriptor( "name",
                                                                                           "Name" );
    private final IPropertyDescriptor PROP_ID                    = new PropertyDescriptor( "id",
                                                                                           "ID" );
    private final IPropertyDescriptor PROP_RULE                  = new PropertyDescriptor( "rule",
                                                                                           "Rule" );

    private final IPropertyDescriptor PROP_QUERY                 = new PropertyDescriptor( "query",
                                                                                           "Query" );
    // Alpha-specific
    private final IPropertyDescriptor PROP_ALPHA_FIELD_NAME      = new PropertyDescriptor( "fieldName",
                                                                                           "Field Name" );
    private final IPropertyDescriptor PROP_ALPHA_EVALUATOR       = new PropertyDescriptor( "evaluator",
                                                                                           "Evaluator" );
    private final IPropertyDescriptor PROP_ALPHA_VALUE           = new PropertyDescriptor( "value",
                                                                                           "Value" );

    // ObjectType specific
    private final IPropertyDescriptor PROP_OBJ_TYPE              = new PropertyDescriptor( "objectType",
                                                                                           "Object Type" );
    private final IPropertyDescriptor PROP_EXP_OFFSET            = new PropertyDescriptor( "expirationOffset",
                                                                                           "Expiration Offset" );
    
    // EntryPoint specific
    private final IPropertyDescriptor PROP_ENTRY_POINT_NAME      = new PropertyDescriptor( "entryPointName",
    																					   "Entry Point Name" );

    private final static String       CAT_GENERAL                = "General";
    private final static String       CAT_OTHER                  = "Other";

    protected BaseVertex              vertex                     = null;

    // Map<String,NodeValue>
    private Map                       values                     = new HashMap();

    /**
     * Constructor initializing properties from <code>vertex</code>
     * 
     * @param vertex source vertex for this property source
     */
    public VertexPropertySource(BaseVertex vertex) {
        this.vertex = vertex;

        initProperties( vertex );
    }

    final private void initProperties(BaseVertex vertex) {

        List descriptorList = new ArrayList();

        if ( vertex instanceof ExistsNodeVertex ) {
            initExistsNodeProperties( (ExistsNodeVertex) vertex,
                                      descriptorList,
                                      values );
        } else if ( vertex instanceof FromNodeVertex ) {
            initFromNodeProperties( (FromNodeVertex) vertex,
                                    descriptorList,
                                    values );
        } else if ( vertex instanceof AccumulateNodeVertex ) {
            initAccumulateNodeProperties( (AccumulateNodeVertex) vertex,
                                          descriptorList,
                                          values );
        }else if ( vertex instanceof RuleTerminalNodeVertex ) {
            initTerminalNodeProperties( (RuleTerminalNodeVertex) vertex,
                    descriptorList,
                    values );
    	} else if ( vertex instanceof QueryTerminalNodeVertex ) {
            initQueryTerminalNodeProperties( (QueryTerminalNodeVertex) vertex,
                    descriptorList,
                    values );
    	} else if ( vertex instanceof EvalConditionNodeVertex ) {
            initEvalConditionNodeProperties( (EvalConditionNodeVertex) vertex,
                                             descriptorList,
                                             values );
        } else if ( vertex instanceof NotNodeVertex ) {
            initNotNodeProperties( (NotNodeVertex) vertex,
                                   descriptorList,
                                   values );
        } else if ( vertex instanceof JoinNodeVertex ) {
            initJoinNodeProperties( (JoinNodeVertex) vertex,
                                    descriptorList,
                                    values );
        } else if ( vertex instanceof RightInputAdapterNodeVertex ) {
            initRightInputAdapterNodeProperties( descriptorList,
                                                 values );
        } else if ( vertex instanceof LeftInputAdapterNodeVertex ) {
            initLeftInputAdapterNodeProperties( (LeftInputAdapterNodeVertex) vertex,
                                                descriptorList,
                                                values );
        } else if ( vertex instanceof AlphaNodeVertex ) {
            initAlphaNodeProperties( (AlphaNodeVertex) vertex,
                                     descriptorList,
                                     values );
        } else if ( vertex instanceof ObjectTypeNodeVertex ) {
            initObjectTypeNodeProperties( (ObjectTypeNodeVertex) vertex,
                                          descriptorList,
                                          values );
        } else if ( vertex instanceof ReteVertex ) {
            initReteNodeProperties( (ReteVertex) vertex,
                                    descriptorList,
                                    values );
        } else if ( vertex instanceof PropagationQueuingNodeVertex ) {
            initPropagationQueuingNodeProperties( (PropagationQueuingNodeVertex) vertex,
                                                  descriptorList,
                                                  values );
        } else if ( vertex instanceof EntryPointNodeVertex ) {
            initEntryPointNodeProperties( (EntryPointNodeVertex) vertex,
                                          descriptorList,
                                          values );
        }

        descriptors = (IPropertyDescriptor[]) descriptorList.toArray( new IPropertyDescriptor[0] );
    }

    private void initExistsNodeProperties(ExistsNodeVertex vertex,
                                          List descriptorList,
                                          Map valueMap) {
        addProperty( PROP_NAME,
                     VERTEX_EXISTS,
                     descriptorList,
                     valueMap );
        addProperty( PROP_ID,
                     Integer.toString( vertex.getId() ),
                     descriptorList,
                     valueMap );

    }

    private void initAccumulateNodeProperties(AccumulateNodeVertex vertex,
                                              List descriptorList,
                                              Map valueMap) {
        addProperty( PROP_NAME,
                     VERTEX_ACCUMULATE,
                     descriptorList,
                     valueMap );
        addProperty( PROP_ID,
                     Integer.toString( vertex.getId() ),
                     descriptorList,
                     valueMap );
    }

    private void initFromNodeProperties(FromNodeVertex vertex,
                                        List descriptorList,
                                        Map valueMap) {
        addProperty( PROP_NAME,
                     VERTEX_FROM,
                     descriptorList,
                     valueMap );
        addProperty( PROP_ID,
                     Integer.toString( vertex.getId() ),
                     descriptorList,
                     valueMap );
    }

    private void initReteNodeProperties(ReteVertex vertex,
                                        List descriptorList,
                                        Map valueMap) {
        addProperty( PROP_NAME,
                     VERTEX_RETE,
                     descriptorList,
                     valueMap );
        addProperty( PROP_ID,
                     Integer.toString( vertex.getId() ),
                     descriptorList,
                     valueMap );
    }

    private void initObjectTypeNodeProperties(ObjectTypeNodeVertex vertex,
                                              List descriptorList,
                                              Map valueMap) {
        addProperty( PROP_NAME,
                     VERTEX_OBJECT_TYPE,
                     descriptorList,
                     valueMap );
        addProperty( PROP_OBJ_TYPE,
                     vertex.getObjectType(),
                     descriptorList,
                     valueMap );
        addProperty( PROP_EXP_OFFSET,
                     vertex.getExpirationOffset(),
                     descriptorList,
                     valueMap );
    }

    private void initAlphaNodeProperties(AlphaNodeVertex vertex,
                                         List descriptorList,
                                         Map valueMap) {
        addProperty( PROP_NAME,
                     VERTEX_ALPHA,
                     descriptorList,
                     valueMap );
        addProperty( PROP_ALPHA_FIELD_NAME,
                     vertex.getFieldName(),
                     descriptorList,
                     valueMap );
        addProperty( PROP_ALPHA_EVALUATOR,
                     vertex.getEvaluator(),
                     descriptorList,
                     valueMap );
        addProperty( PROP_ALPHA_VALUE,
                     vertex.getValue(),
                     descriptorList,
                     valueMap );

        Constraint constraint = vertex.getConstraint();
        if ( constraint == null ) {
            return;
        }
        IPropertyDescriptor prop = new PropertyDescriptor( CONSTRAINT,
                                                           CONSTRAINT_CAP );
        addProperty( prop,
                     constraint.toString(),
                     descriptorList,
                     valueMap );

    }

    private void initLeftInputAdapterNodeProperties(LeftInputAdapterNodeVertex vertex,
                                                    List descriptorList,
                                                    Map valueMap) {
        addProperty( PROP_NAME,
                     VERTEX_LEFT_INPUT_ADAPTER,
                     descriptorList,
                     valueMap );

    }

    private void initRightInputAdapterNodeProperties(List descriptorList,
                                                     Map valueMap) {
        addProperty( PROP_NAME,
                     VERTEX_RIGHT_INPUT_ADAPTER,
                     descriptorList,
                     valueMap );
    }

    private void initJoinNodeProperties(JoinNodeVertex vertex,
                                        List descriptorList,
                                        Map valueMap) {

        addProperty( PROP_NAME,
                     VERTEX_JOIN,
                     descriptorList,
                     valueMap );
        addProperty( PROP_ID,
                     Integer.toString( vertex.getId() ),
                     descriptorList,
                     valueMap );

        Constraint[] constraints = vertex.getConstraints();

        if ( constraints == null ) {
            return;
        }

        for ( int i = 0, length = constraints.length; i < length; i++ ) {
            PropertyDescriptor prop = new PropertyDescriptor( CONSTRAINT + (i + 1),
                                                              CONSTRAINT_CAP + " " + (i + 1) );
            addOther( prop,
                      constraints[i].toString(),
                      descriptorList,
                      valueMap );
        }

    }

    private void initNotNodeProperties(NotNodeVertex vertex,
                                       List descriptorList,
                                       Map valueMap) {
        addProperty( PROP_NAME,
                     VERTEX_NOT,
                     descriptorList,
                     valueMap );
        addProperty( PROP_ID,
                     Integer.toString( vertex.getId() ),
                     descriptorList,
                     valueMap );
    }

    private void initEvalConditionNodeProperties(EvalConditionNodeVertex vertex,
                                                 List descriptorList,
                                                 Map valueMap) {
        addProperty( PROP_NAME,
                     VERTEX_EVAL_CONDITION,
                     descriptorList,
                     valueMap );
        addProperty( PROP_ID,
                     Integer.toString( vertex.getId() ),
                     descriptorList,
                     valueMap );
    }

    private void initTerminalNodeProperties(RuleTerminalNodeVertex node,
                                            List descriptorList,
                                            Map valueMap) {

        addProperty( PROP_NAME,
                     VERTEX_TERMINAL,
                     descriptorList,
                     valueMap );
        addProperty( PROP_ID,
                     Integer.toString( node.getId() ),
                     descriptorList,
                     valueMap );
        addProperty( PROP_RULE,
                     node.getRuleName(),
                     descriptorList,
                     valueMap );

    }

    private void initQueryTerminalNodeProperties(QueryTerminalNodeVertex node,
		            List descriptorList,
		            Map valueMap) {
		
		addProperty( PROP_NAME,
				     VERTEX_QUERY_TERMINAL,
                     descriptorList,
                     valueMap );
		addProperty( PROP_ID,
                     Integer.toString( node.getId() ),
                     descriptorList,
		             valueMap );
		addProperty( PROP_QUERY,
		             node.getQueryName(),
		             descriptorList,
		             valueMap );
		
	}

    private void initPropagationQueuingNodeProperties(PropagationQueuingNodeVertex vertex,
                                                      List descriptorList,
                                                      Map valueMap) {
    	addProperty( PROP_NAME,
    			     VERTEX_PROPAGATION_QUEUING,
                     descriptorList,
                     valueMap );

    	addProperty( PROP_ID,
                     Integer.toString( vertex.getId() ),
                     descriptorList,
    	             valueMap );

    }

    private void initEntryPointNodeProperties(EntryPointNodeVertex vertex,
                                              List descriptorList,
                                              Map valueMap) {
    	addProperty( PROP_NAME,
                     VERTEX_ENTRY_POINT,
                     descriptorList,
                     valueMap );

    	addProperty( PROP_ENTRY_POINT_NAME,
                     vertex.getEntryPointName(),
                     descriptorList,
                     valueMap );

    	addProperty( PROP_ID,
                     Integer.toString( vertex.getId() ),
                     descriptorList,
                     valueMap );

    }

    private void addProperty(IPropertyDescriptor field,
                             String value,
                             List descriptorList,
                             Map valueMap) {
        descriptorList.add( field );
        valueMap.put( field.getId().toString(),
                      new NodeValue( CAT_GENERAL,
                                     value ) );
        if ( field instanceof PropertyDescriptor ) {
            ((PropertyDescriptor) field).setAlwaysIncompatible( true );
            ((PropertyDescriptor) field).setCategory( CAT_GENERAL );
        }

    }

    private void addOther(IPropertyDescriptor field,
                          String value,
                          List descriptorList,
                          Map valueMap) {
        descriptorList.add( field );
        valueMap.put( field.getId().toString(),
                      new NodeValue( CAT_OTHER,
                                     value ) );

        if ( field instanceof PropertyDescriptor ) {
            ((PropertyDescriptor) field).setAlwaysIncompatible( true );
            ((PropertyDescriptor) field).setCategory( CAT_OTHER );
        }

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
     */
    public Object getEditableValue() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(java.lang.Object)
     */
    public Object getPropertyValue(Object propName) {
        return getPropertyValue( (String) propName );
    }

    /**
     * Property value.
     * 
     * @param propName
     * @return
     */
    public Object getPropertyValue(String propName) {
        return ((NodeValue) (values.get( propName ))).value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(java.lang.Object, java.lang.Object)
     */
    public void setPropertyValue(Object propName,
                                 Object value) {
        setPropertyValue( propName,
                          value );
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return descriptors;
    }

    /**
     * Doing nothing as resetting properties from property sheet is not possible.
     */
    public void resetPropertyValue(Object propName) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(java.lang.Object)
     */
    public boolean isPropertySet(Object propName) {
        return values.containsKey( propName );
    }

    private class NodeValue {
        final String category;
        final String value;

        NodeValue(String category,
                  String value) {
            this.category = category;
            this.value = value;
        }
    }

}
