package org.drools.eclipse.editors.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.drools.base.ClassTypeResolver;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.eclipse.DRLInfo;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DroolsPluginImages;
import org.drools.eclipse.DRLInfo.RuleInfo;
import org.drools.eclipse.editors.AbstractRuleEditor;
import org.drools.eclipse.util.ProjectClassLoader;
import org.drools.lang.Location;
import org.drools.lang.descr.FactTemplateDescr;
import org.drools.lang.descr.FieldTemplateDescr;
import org.drools.lang.descr.GlobalDescr;
import org.drools.rule.builder.dialect.mvel.MVELConsequenceBuilder;
import org.drools.rule.builder.dialect.mvel.MVELDialect;
import org.drools.spi.KnowledgeHelper;
import org.drools.util.asm.ClassFieldInspector;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IFileEditorInput;
import org.mvel.ParserContext;
import org.mvel.compiler.CompiledExpression;
import org.mvel.compiler.ExpressionCompiler;
import org.mvel.compiler.PropertyVerifier;

/**
 * For handling within rules.
 *
 * @author Michael Neale, Kris Verlanen
 */
public class RuleCompletionProcessor extends DefaultCompletionProcessor {

    private static final String DIALECT     = "dialect";

    private static final Image  DROOLS_ICON = DroolsPluginImages.getImage( DroolsPluginImages.DROOLS );

    private static final Image  CLASS_ICON  = DroolsPluginImages.getImage( DroolsPluginImages.CLASS );

    /**
     * A CompletionContext contains the DRL backtext parsing results, to avoid
     * multilpe parser invocations
     */
    private CompletionContext context;

    public RuleCompletionProcessor(AbstractRuleEditor editor) {
        super( editor );
    }

    protected List getCompletionProposals(ITextViewer viewer,
                                          int documentOffset) {
        try {
            final List list = new ArrayList();
            IDocument doc = viewer.getDocument();

            String backText = readBackwards( documentOffset,
                                             doc );
            final String prefix = CompletionUtil.stripLastWord( backText );

            // if inside the keyword "rule ", no code completion
            if ( backText.length() < 5 ) {
                return list;
            }

            this.context = new CompletionContext( backText );
            Location location = context.getLocation();

            if ( location.getType() == Location.LOCATION_RULE_HEADER ) {
                addRuleHeaderProposals( list,
                                        documentOffset,
                                        prefix,
                                        backText );
            } else if ( location.getType() == Location.LOCATION_RHS ) {
                addRHSCompletionProposals( list,
                                           documentOffset,
                                           prefix,
                                           backText,
                                           (String) location.getProperty( Location.LOCATION_LHS_CONTENT ),
                                           (String) location.getProperty( Location.LOCATION_RHS_CONTENT ) );
            } else {
                addLHSCompletionProposals( list,
                                           documentOffset,
                                           location,
                                           prefix,
                                           backText );
            }

            filterProposalsOnPrefix( prefix,
                                     list );
            return list;
        } catch ( Throwable t ) {
            DroolsEclipsePlugin.log( t );
        }
        return null;
    }

    protected void addRHSCompletionProposals(List list,
                                             int documentOffset,
                                             String prefix,
                                             String backText,
                                             String conditions,
                                             String consequence) {
        // only add functions and keywords if at the beginning of a
        // new statement
        if ( consequence == null || consequence.length() < prefix.length() ) {
            // possible if doing code completion directly after "then"
            return;
        }
        String consequenceWithoutPrefix = consequence.substring( 0,
                                                                 consequence.length() - prefix.length() );

        if ( context == null ) {
            context = new CompletionContext( backText );
        }

        boolean startOfDialectExpression = CompletionUtil.isStartOfDialectExpression( consequenceWithoutPrefix );
        if ( //isJavaDialect() && 
        		startOfDialectExpression ) {
            addRHSKeywordCompletionProposals( list,
                                              documentOffset,
                                              prefix );
            addRHSFunctionCompletionProposals( list,
                                               documentOffset,
                                               prefix );
        }
        

        //if we have 1st a dialect defined locally, or 2nd a global dialect
        //the locally defined dialect will override the package default
        if ( isJavaDialect() ) {
            addRHSJavaCompletionProposals( list,
                                           documentOffset,
                                           prefix,
                                           backText,
                                           consequence );
        } else if ( isMvelDialect() ) {
            addRHSMvelCompletionProposals( list,
                                           documentOffset,
                                           prefix,
                                           backText,
                                           consequence,
                                           startOfDialectExpression );
        }
    }

    private boolean isJavaDialect() {
        // java is the default dialect, so no package dialect means java
        // conditions are ordered from the more specific to the more general
        if ( context.isJavaDialect() ) {
            return true;
        } else if ( context.isDefaultDialect() && (!(getAttributes().containsKey( DIALECT )) || hasPackageDialect( "java" )) ) {
            return true;
        }

        return false;
    }

    private boolean isMvelDialect() {
        if ( context.isMvelDialect() ) {
            return true;
        } else if ( context.isDefaultDialect() && hasPackageDialect( "mvel" ) ) {
            return true;
        }
        return false;
    }

    private boolean hasPackageDialect(String dialect) {
        String globalDialect = (String) getAttributes().get( DIALECT );
        if ( globalDialect != null && dialect.equalsIgnoreCase( globalDialect ) ) {
            return true;
        }
        return false;
    }

    protected void addLHSCompletionProposals(List<RuleCompletionProposal> list,
                                             int documentOffset,
                                             Location location,
                                             String prefix,
                                             String backText) {
        switch ( location.getType() ) {
            case Location.LOCATION_LHS_BEGIN_OF_CONDITION :
                // if we are at the beginning of a new condition
                // add drools keywords
                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      "and",
                                                      "and ",
                                                      DROOLS_ICON ) );
                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      "or",
                                                      "or ",
                                                      DROOLS_ICON ) );
                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      "from",
                                                      "from ",
                                                      DROOLS_ICON ) );
                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      "forall",
                                                      "forall(  )",
                                                      8,
                                                      DROOLS_ICON ) );
                RuleCompletionProposal prop = new RuleCompletionProposal( documentOffset - prefix.length(),
                                                                          prefix.length(),
                                                                          "eval",
                                                                          "eval(  )",
                                                                          6 );
                prop.setImage( DROOLS_ICON );
                list.add( prop );
                prop = new RuleCompletionProposal( documentOffset - prefix.length(),
                                                   prefix.length(),
                                                   "then",
                                                   "then" + System.getProperty( "line.separator" ) + "\t" );
                prop.setImage( DROOLS_ICON );
                list.add( prop );
                // we do not break but also add all elements that are needed for
                // and/or
            case Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR :
                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      "not",
                                                      "not ",
                                                      DROOLS_ICON ) );
                // we do not break but also add all elements that are needed for
                // not
            case Location.LOCATION_LHS_BEGIN_OF_CONDITION_NOT :
                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      "exists",
                                                      "exists ",
                                                      DROOLS_ICON ) );
                // we do not break but also add all elements that are needed for
                // exists
            case Location.LOCATION_LHS_FROM_ACCUMULATE :
            case Location.LOCATION_LHS_FROM_COLLECT :
            case Location.LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS :
                // and add imported classes
                Iterator<String> iterator = getImports().iterator();
                while ( iterator.hasNext() ) {
                    String name = iterator.next();
                    int index = name.lastIndexOf( "." );
                    if ( index != -1 ) {
                        String className = name.substring( index + 1 );
                        RuleCompletionProposal p = new RuleCompletionProposal( documentOffset - prefix.length(),
                                                                               prefix.length(),
                                                                               className,
                                                                               className + "(  )",
                                                                               className.length() + 2 );
                        p.setPriority( -1 );
                        p.setImage( CLASS_ICON );
                        list.add( p );
                    }
                }
                iterator = getClassesInPackage().iterator();
                while ( iterator.hasNext() ) {
                    String name = iterator.next();
                    int index = name.lastIndexOf( "." );
                    if ( index != -1 ) {
                        String className = name.substring( index + 1 );
                        RuleCompletionProposal p = new RuleCompletionProposal( documentOffset - prefix.length(),
                                                                               prefix.length(),
                                                                               className,
                                                                               className + "(  )",
                                                                               className.length() + 2 );
                        p.setPriority( -1 );
                        p.setImage( CLASS_ICON );
                        list.add( p );
                    }
                }
                iterator = getTemplates().iterator();
                while ( iterator.hasNext() ) {
                    String name = (String) iterator.next();
                    RuleCompletionProposal p = new RuleCompletionProposal( documentOffset - prefix.length(),
                                                                           prefix.length(),
                                                                           name,
                                                                           name + "(  )",
                                                                           name.length() + 2 );
                    p.setPriority( -1 );
                    p.setImage( CLASS_ICON );
                    list.add( p );
                }
                break;
            case Location.LOCATION_LHS_INSIDE_CONDITION_START :
                String className = (String) location.getProperty( Location.LOCATION_PROPERTY_CLASS_NAME );
                String propertyName = (String) location.getProperty( Location.LOCATION_PROPERTY_PROPERTY_NAME );
                if ( className != null ) {
                    boolean isTemplate = addFactTemplatePropertyProposals( documentOffset,
                                                                           prefix,
                                                                           className,
                                                                           list );
                    if ( !isTemplate ) {
                        ClassTypeResolver resolver = new ClassTypeResolver( getUniqueImports(),
                                                                            ProjectClassLoader.getProjectClassLoader( getEditor() ) );
                        try {
                            String currentClass = className;
                            if ( propertyName != null ) {
                                String[] nestedProperties = propertyName.split( "\\." );
                                int nbSuperProperties = nestedProperties.length - 1;
                                if ( propertyName.endsWith( "." ) ) {
                                    nbSuperProperties++;
                                }
                                for ( int i = 0; i < nbSuperProperties; i++ ) {
                                    String simplePropertyName = nestedProperties[i];
                                    currentClass = getSimplePropertyClass( currentClass,
                                                                           simplePropertyName );
                                    currentClass = convertToNonPrimitiveClass( currentClass );
                                }
                            }
                            RuleCompletionProposal p = new RuleCompletionProposal( documentOffset - prefix.length(),
                                                                                   prefix.length(),
                                                                                   "this" );
                            p.setImage( METHOD_ICON );
                            list.add( p );
                            Class clazz = resolver.resolveType( currentClass );
                            if ( clazz != null ) {
                                if ( Map.class.isAssignableFrom( clazz ) ) {
                                    p = new RuleCompletionProposal( documentOffset - prefix.length(),
                                                                    prefix.length(),
                                                                    "this['']",
                                                                    "this['']",
                                                                    6 );
                                    p.setImage( METHOD_ICON );
                                    list.add( p );
                                }
                                ClassFieldInspector inspector = new ClassFieldInspector( clazz );
                                Map types = inspector.getFieldTypes();
                                Iterator iterator2 = inspector.getFieldNames().keySet().iterator();
                                while ( iterator2.hasNext() ) {
                                    String name = (String) iterator2.next();
                                    p = new RuleCompletionProposal( documentOffset - prefix.length(),
                                                                    prefix.length(),
                                                                    name,
                                                                    name + " " );
                                    p.setImage( METHOD_ICON );
                                    list.add( p );
                                    Class type = (Class) types.get( name );
                                    if ( type != null && Map.class.isAssignableFrom( type ) ) {
                                        name += "['']";
                                        p = new RuleCompletionProposal( documentOffset - prefix.length(),
                                                                        prefix.length(),
                                                                        name,
                                                                        name,
                                                                        name.length() - 2 );
                                        p.setImage( METHOD_ICON );
                                        list.add( p );
                                    }
                                }
                            }
                        } catch ( IOException exc ) {
                            // Do nothing
                        } catch ( ClassNotFoundException exc ) {
                            // Do nothing
                        }
                    }
                }
                break;
            case Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR :
                className = (String) location.getProperty( Location.LOCATION_PROPERTY_CLASS_NAME );
                String property = (String) location.getProperty( Location.LOCATION_PROPERTY_PROPERTY_NAME );
                String type = getPropertyClass( className,
                                                property );

                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      "==",
                                                      "== ",
                                                      DROOLS_ICON ) );
                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      "!=",
                                                      "!= ",
                                                      DROOLS_ICON ) );
                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      ":",
                                                      ": ",
                                                      DROOLS_ICON ) );
                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      "->",
                                                      "-> (  )",
                                                      5,
                                                      DROOLS_ICON ) );
                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      "memberOf",
                                                      "memberOf ",
                                                      DROOLS_ICON ) );
                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      "not memberOf",
                                                      "not memberOf ",
                                                      DROOLS_ICON ) );
                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      "in",
                                                      "in (  )",
                                                      5,
                                                      DROOLS_ICON ) );
                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      "not in",
                                                      "not in (  )",
                                                      9,
                                                      DROOLS_ICON ) );

                if ( isComparable( type ) ) {
                    list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                          prefix.length(),
                                                          "<",
                                                          "< ",
                                                          DROOLS_ICON ) );
                    list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                          prefix.length(),
                                                          "<=",
                                                          "<= ",
                                                          DROOLS_ICON ) );
                    list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                          prefix.length(),
                                                          ">",
                                                          "> ",
                                                          DROOLS_ICON ) );
                    list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                          prefix.length(),
                                                          ">=",
                                                          ">= ",
                                                          DROOLS_ICON ) );
                }
                if ( type.equals( "java.lang.String" ) ) {
                    list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                          prefix.length(),
                                                          "matches",
                                                          "matches \"\"",
                                                          9,
                                                          DROOLS_ICON ) );
                    list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                          prefix.length(),
                                                          "not matches",
                                                          "not matches \"\"",
                                                          13,
                                                          DROOLS_ICON ) );
                }
                if ( isSubtypeOf( type,
                                  "java.util.Collection" ) ) {
                    list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                          prefix.length(),
                                                          "contains",
                                                          "contains ",
                                                          DROOLS_ICON ) );
                    list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                          prefix.length(),
                                                          "not contains",
                                                          "not contains ",
                                                          DROOLS_ICON ) );
                }
                break;
            case Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT :
                // determine type
                className = (String) location.getProperty( Location.LOCATION_PROPERTY_CLASS_NAME );
                property = (String) location.getProperty( Location.LOCATION_PROPERTY_PROPERTY_NAME );
                String operator = (String) location.getProperty( Location.LOCATION_PROPERTY_OPERATOR );
                type = getPropertyClass( className,
                                         property );

                if ( "in".equals( operator ) ) {
                    list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                          prefix.length(),
                                                          "()",
                                                          "(  )",
                                                          2,
                                                          DROOLS_ICON ) );
                    break;
                }

                if ( "contains".equals( operator ) || "excludes".equals( operator ) ) {
                    type = "java.lang.Object";
                }

                if ( "memberOf".equals( operator ) ) {
                    type = "java.util.Collection";
                }

                boolean isObject = false;
                if ( "java.lang.Object".equals( type ) ) {
                    isObject = true;
                }

                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      "null",
                                                      "null ",
                                                      DROOLS_ICON ) );
                if ( "boolean".equals( type ) ) {
                    list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                          prefix.length(),
                                                          "true",
                                                          "true ",
                                                          DROOLS_ICON ) );
                    list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                          prefix.length(),
                                                          "false",
                                                          "false ",
                                                          DROOLS_ICON ) );
                }
                if ( isObject || "java.lang.String".equals( type ) ) {
                    list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                          prefix.length(),
                                                          "\"\"",
                                                          "\"\"",
                                                          1,
                                                          DROOLS_ICON ) );
                }
                if ( isObject || "java.util.Date".equals( type ) ) {
                    list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                          prefix.length(),
                                                          "\"dd-mmm-yyyy\"",
                                                          "\"dd-mmm-yyyy\"",
                                                          1,
                                                          DROOLS_ICON ) );
                }
                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      "()",
                                                      "(  )",
                                                      2,
                                                      DROOLS_ICON ) );
                // add parameters with possibly matching type
                Map result = new HashMap();
                addRuleParameters( result, context.getRuleParameters() );
                Iterator iterator2 = result.entrySet().iterator();
                while ( iterator2.hasNext() ) {
                    Map.Entry entry = (Map.Entry) iterator2.next();
                    String paramName = (String) entry.getKey();
                    String paramType = (String) entry.getValue();
                    if ( isSubtypeOf( paramType,
                                      type ) ) {
                        RuleCompletionProposal proposal = new RuleCompletionProposal( documentOffset - prefix.length(),
                                                                                      prefix.length(),
                                                                                      paramName );
                        proposal.setPriority( -1 );
                        proposal.setImage( VARIABLE_ICON );
                        list.add( proposal );
                    }
                }
                // add globals with possibly matching type
                List<GlobalDescr> globals = getGlobals();
                if ( globals != null ) {
                    for ( GlobalDescr global: globals ) {
                        if ( isSubtypeOf( global.getType(),
                                          type ) ) {
                            RuleCompletionProposal proposal = new RuleCompletionProposal( documentOffset - prefix.length(),
                                                                                          prefix.length(),
                                                                                          global.getIdentifier() );
                            proposal.setPriority( -1 );
                            proposal.setImage( VARIABLE_ICON );
                            list.add( proposal );
                        }
                    }
                }
                break;
            case Location.LOCATION_LHS_INSIDE_EVAL :
                String content = (String) location.getProperty( Location.LOCATION_EVAL_CONTENT );
                list.addAll( getJavaCompletionProposals( documentOffset,
                                                         content,
                                                         prefix,
                                                         getRuleParameters( backText ) ) );
                break;
            case Location.LOCATION_LHS_INSIDE_CONDITION_END :
                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      "&&",
                                                      "&& ",
                                                      DROOLS_ICON ) );
                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      "||",
                                                      "|| ",
                                                      DROOLS_ICON ) );
                list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                      prefix.length(),
                                                      ",",
                                                      ", ",
                                                      DROOLS_ICON ) );
                break;
            case Location.LOCATION_LHS_FROM :
                String fromText = (String) location.getProperty( Location.LOCATION_FROM_CONTENT );
                int index = fromText.indexOf( '.' );
                if ( index == -1 ) {
                    // add accumulate and collect keyword
                    list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                          prefix.length(),
                                                          "accumulate",
                                                          "accumulate (  , init (  ), action (  ), result (  ) )",
                                                          13,
                                                          DROOLS_ICON ) );
                    PackageBuilderConfiguration config = new PackageBuilderConfiguration( ProjectClassLoader.getProjectClassLoader( getEditor() ),
                                                                                          null );
                    Map accumulateFunctions = config.getAccumulateFunctionsMap();
                    for ( iterator2 = accumulateFunctions.keySet().iterator(); iterator2.hasNext(); ) {
                        String accumulateFunction = (String) iterator2.next();
                        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                              prefix.length(),
                                                              "accumulate " + accumulateFunction,
                                                              "accumulate (  , " + accumulateFunction + "(  ) )",
                                                              13,
                                                              DROOLS_ICON ) );
                    }
                    list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                                          prefix.length(),
                                                          "collect",
                                                          "collect (  )",
                                                          10,
                                                          DROOLS_ICON ) );
                    // add all functions
                    if ( "".equals( fromText ) ) {
                        List functions = getFunctions();
                        iterator = functions.iterator();
                        while ( iterator.hasNext() ) {
                            String name = (String) iterator.next() + "()";
                            prop = new RuleCompletionProposal( documentOffset - prefix.length(),
                                                               prefix.length(),
                                                               name,
                                                               name,
                                                               name.length() - 1 );
                            prop.setPriority( -1 );
                            prop.setImage( METHOD_ICON );
                            list.add( prop );
                        }
                    }
                    list.addAll( getJavaCompletionProposals( documentOffset,
                                                             fromText,
                                                             prefix,
                                                             getRuleParameters( backText ) ) );
                }
                break;
            case Location.LOCATION_LHS_FROM_ACCUMULATE_INIT_INSIDE :
                content = (String) location.getProperty( Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT );
                list.addAll( getJavaCompletionProposals( documentOffset,
                                                         content,
                                                         prefix,
                                                         getRuleParameters( backText ) ) );
                break;
            case Location.LOCATION_LHS_FROM_ACCUMULATE_ACTION_INSIDE :
                content = (String) location.getProperty( Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT );
                content += (String) location.getProperty( Location.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT );
                list.addAll( getJavaCompletionProposals( documentOffset,
                                                         content,
                                                         prefix,
                                                         getRuleParameters( backText ) ) );
                break;
            case Location.LOCATION_LHS_FROM_ACCUMULATE_RESULT_INSIDE :
                content = (String) location.getProperty( Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT );
                content += (String) location.getProperty( Location.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT );
                content += (String) location.getProperty( Location.LOCATION_PROPERTY_FROM_ACCUMULATE_RESULT_CONTENT );
                list.addAll( getJavaCompletionProposals( documentOffset,
                                                         content,
                                                         prefix,
                                                         getRuleParameters( backText ) ) );
                break;
        }
    }

    private String getPropertyClass(String className,
                                    String propertyName) {
        if ( className != null && propertyName != null ) {
            FactTemplateDescr template = getTemplate( className );
            if ( template != null ) {
                Iterator iterator = template.getFields().iterator();
                while ( iterator.hasNext() ) {
                    FieldTemplateDescr field = (FieldTemplateDescr) iterator.next();
                    if ( propertyName.equals( field.getName() ) ) {
                        String type = field.getClassType();
                        if ( isPrimitiveType( type ) ) {
                            return type;
                        }
                        ClassTypeResolver resolver = new ClassTypeResolver( getUniqueImports(),
                                                                            ProjectClassLoader.getProjectClassLoader( getEditor() ) );
                        try {
                            Class clazz = resolver.resolveType( type );
                            if ( clazz != null ) {
                                return clazz.getName();
                            }
                        } catch ( ClassNotFoundException exc ) {
                            DroolsEclipsePlugin.log( exc );
                        }
                    }
                }
                // if not found, return null
            } else {
                String[] nestedProperties = propertyName.split( "\\." );
                String currentClass = className;
                for ( int i = 0; i < nestedProperties.length; i++ ) {
                    String simplePropertyName = nestedProperties[i];
                    currentClass = getSimplePropertyClass( currentClass,
                                                           simplePropertyName );
                }
                return currentClass;
            }
        }
        return null;
    }

    private String getSimplePropertyClass(String className,
                                          String propertyName) {
        if ( "this".equals( propertyName ) ) {
            return className;
        }
        if ( propertyName.endsWith( "]" ) ) {
            // TODO can we take advantage of generics here?
            return "java.lang.Object";
        }
        ClassTypeResolver resolver = new ClassTypeResolver( getUniqueImports(),
                                                            ProjectClassLoader.getProjectClassLoader( getEditor() ) );
        try {
            Class clazz = resolver.resolveType( className );
            if ( clazz != null ) {
                Class clazzz = (Class) new ClassFieldInspector( clazz ).getFieldTypes().get( propertyName );
                if ( clazzz != null ) {
                    return clazzz.getName();
                }
            }
        } catch ( IOException exc ) {
            // Do nothing
        } catch ( ClassNotFoundException exc ) {
            // Do nothing
        }
        return "java.lang.Object";
    }

    private Map getRuleParameters(String backText) {
        Map result = new HashMap();
        // add globals
        List globals = getGlobals();
        if ( globals != null ) {
            for ( Iterator iterator = globals.iterator(); iterator.hasNext(); ) {
                GlobalDescr global = (GlobalDescr) iterator.next();
                result.put( global.getIdentifier(),
                            global.getType() );
            }
        }

        if ( context == null ) {
            context = new CompletionContext( backText );
        }
        addRuleParameters( result, context.getRuleParameters() );
        return result;
    }

    private boolean isComparable(String type) {
        if ( type == null ) {
            return false;
        }
        if ( isPrimitiveNumericType( type ) ) {
            return true;
        }
        if ( isObjectNumericType( type ) ) {
            return true;
        }
        if ( isSubtypeOf( type,
                          "java.lang.Comparable" ) ) {
            return true;
        }
        return false;
    }

    private boolean isPrimitiveType(String type) {
        return isPrimitiveNumericType( type ) || type.equals( "boolean" );
    }

    private boolean isPrimitiveNumericType(String type) {
        return type.equals( "byte" ) || type.equals( "short" ) || type.equals( "int" ) || type.equals( "long" ) || type.equals( "float" ) || type.equals( "double" ) || type.equals( "char" );
    }

    private boolean isObjectNumericType(String type) {
        return type.equals( "java.lang.Byte" ) || type.equals( "java.lang.Short" ) || type.equals( "java.lang.Integer" ) || type.equals( "java.lang.Long" ) || type.equals( "java.lang.Float" ) || type.equals( "java.lang.Double" )
               || type.equals( "java.lang.Char" );
    }

    /**
     * Returns true if the first class is the same or a subtype of the second
     * class.
     *
     * @param class1
     * @param class2
     * @return
     */
    private boolean isSubtypeOf(String class1,
                                String class2) {
        if ( class1 == null || class2 == null ) {
            return false;
        }
        class1 = convertToNonPrimitiveClass( class1 );
        class2 = convertToNonPrimitiveClass( class2 );
        // TODO add code to take primitive types into account
        ClassTypeResolver resolver = new ClassTypeResolver( getUniqueImports(),
                                                            ProjectClassLoader.getProjectClassLoader( getEditor() ) );
        try {
            Class clazz1 = resolver.resolveType( class1 );
            Class clazz2 = resolver.resolveType( class2 );
            if ( clazz1 == null || clazz2 == null ) {
                return false;
            }
            return clazz2.isAssignableFrom( clazz1 );
        } catch ( ClassNotFoundException exc ) {
            return false;
        }
    }

    private String convertToNonPrimitiveClass(String clazz) {
        if ( !isPrimitiveType( clazz ) ) {
            return clazz;
        }
        if ( "byte".equals( clazz ) ) {
            return "java.lang.Byte";
        } else if ( "short".equals( clazz ) ) {
            return "java.lang.Short";
        } else if ( "int".equals( clazz ) ) {
            return "java.lang.Integer";
        } else if ( "long".equals( clazz ) ) {
            return "java.lang.Long";
        } else if ( "float".equals( clazz ) ) {
            return "java.lang.Float";
        } else if ( "double".equals( clazz ) ) {
            return "java.lang.Double";
        } else if ( "char".equals( clazz ) ) {
            return "java.lang.Char";
        } else if ( "boolean".equals( clazz ) ) {
            return "java.lang.Boolean";
        }
        // should never occur
        return null;
    }

    private void addRHSFunctionCompletionProposals(List list,
                                                   int documentOffset,
                                                   String prefix) {
        Iterator iterator;
        RuleCompletionProposal prop;
        List functions = getFunctions();
        iterator = functions.iterator();
        while ( iterator.hasNext() ) {
            String name = (String) iterator.next() + "()";
            prop = new RuleCompletionProposal( documentOffset - prefix.length(),
                                               prefix.length(),
                                               name,
                                               name + ";",
                                               name.length() - 1 );
            prop.setPriority( -1 );
            prop.setImage( METHOD_ICON );
            list.add( prop );
        }
    }

    private void addRHSKeywordCompletionProposals(List list,
                                                  int documentOffset,
                                                  String prefix) {
        RuleCompletionProposal prop = new RuleCompletionProposal( documentOffset - prefix.length(),
                                                                  prefix.length(),
                                                                  "update",
                                                                  "update();",
                                                                  7 );
        prop.setImage( DROOLS_ICON );
        list.add( prop );
        prop = new RuleCompletionProposal( documentOffset - prefix.length(),
                                           prefix.length(),
                                           "retract",
                                           "retract();",
                                           8 );
        prop.setImage( DROOLS_ICON );
        list.add( prop );
        prop = new RuleCompletionProposal( documentOffset - prefix.length(),
                                           prefix.length(),
                                           "insert",
                                           "insert();",
                                           7 );
        prop.setImage( DROOLS_ICON );
        list.add( prop );
        prop = new RuleCompletionProposal( documentOffset - prefix.length(),
                                           prefix.length(),
                                           "insertLogical",
                                           "insertLogical();",
                                           14 );
        prop.setImage( DROOLS_ICON );
        list.add( prop );
    }

    private void addRHSJavaCompletionProposals(List list,
                                               int documentOffset,
                                               String prefix,
                                               String backText,
                                               String consequence) {
        list.addAll( getJavaCompletionProposals( documentOffset,
                                                 consequence,
                                                 prefix,
                                                 getRuleParameters( backText ) ) );
    }

    private void addRHSMvelCompletionProposals(List list,
                                               final int documentOffset,
                                               String prefix,
                                               String backText,
                                               String consequence,
                                               boolean expressionStart) {

        Collection mvelCompletionProposals = getMvelCompletionProposals( consequence,
                                                                         documentOffset,
                                                                         prefix,
                                                                         getRuleParameters( backText ),
                                                                         backText,
                                                                         expressionStart );
        list.addAll( mvelCompletionProposals );
    }

    private Collection getMvelCompletionProposals(final String consequenceBackText,
                                                  final int documentOffset,
                                                  final String prefix,
                                                  Map params,
                                                  String ruleBackText,
                                                  boolean startOfExpression) {

        final Set proposals = new HashSet();

        if (!(getEditor().getEditorInput() instanceof IFileEditorInput)) {
            return proposals;
        }

        try {
            DRLInfo drlInfo = DroolsEclipsePlugin.getDefault().generateParsedResource(
                "package dummy; \n" + ruleBackText,
                ((IFileEditorInput) getEditor().getEditorInput()).getFile(),
                false,
                false );

            String textWithoutPrefix = CompletionUtil.getTextWithoutPrefix( consequenceBackText,
                                                                            prefix );
            boolean expressionStart = CompletionUtil.isStartOfDialectExpression( textWithoutPrefix );

            boolean isConstrained = textWithoutPrefix.endsWith( "." );

            // we split the expression in various regions:
            // *the previous expression
            // *the last expression
            // *the last inner expression

            // attempt to compile and analyze the previous expression to collect inputs and vars
            String previousExpression = CompletionUtil.getPreviousExpression( consequenceBackText );
            MvelContext previousExprContext = analyzeMvelExpression( getResolvedMvelInputs( params ),
                                                                     drlInfo,
                                                                     previousExpression );

            // attempt to compile and analyze the last and last inner expression, using as inputs the previous expression inputs and vars
            Map variables = previousExprContext.getContext().getVariables();
            Map inputs = previousExprContext.getContext().getInputs();
            inputs.putAll( variables );

            //last inner expression
            String lastInnerExpression = CompletionUtil.getTextWithoutPrefix( CompletionUtil.getInnerExpression( consequenceBackText ),
                                                                              prefix );
            String compilableLastInnerExpression = CompletionUtil.getCompilableText( lastInnerExpression );

            MvelContext lastInnerExprContext = analyzeMvelExpression( inputs,
                                                                      drlInfo,
                                                                      compilableLastInnerExpression );

            //last expression
            String lastExpression = CompletionUtil.getLastExpression( consequenceBackText ).trim();
            //is this a modify expression?
            //group 1 is the body of modify
            //group 2 if present is the whole with block including brackets
            //group 3 if present is the inner content of the with block
            Matcher modMatcher = CompletionUtil.MODIFY_PATTERN.matcher( lastExpression );

            boolean isModifyBlock = modMatcher.matches() && modMatcher.groupCount() == 3;

            //if constrained, get completion for egress of last inner, filtered on prefix
            if ( isConstrained ) {
                if ( lastInnerExprContext.isStaticFlag() ) {
                    return getMvelClassCompletionsFromJDT( documentOffset,
                                                           "",
                                                           params,
                                                           lastInnerExprContext.getReturnedType() );

                }

                return getMvelInstanceCompletionsFromJDT( documentOffset,
                                                          "",
                                                          params,
                                                          lastInnerExprContext.getReturnedType(),
                                                          false );
            }
            //if expression start inside with block, then get completion for prefix with egrss of modif var + prev expr var&inputs
            else if ( expressionStart && isModifyBlock ) {
                String modifyVar = modMatcher.group( 1 );
                //String modifyWith = modMatcher.group( 3 );

                //get the egress type of the modify var
                MvelContext modVarContext = analyzeMvelExpression( inputs,
                                                                   drlInfo,
                                                                   modifyVar );

                Class modVarType = modVarContext.getReturnedType();

                Collection modVarComps = getMvelInstanceCompletionsFromJDT( documentOffset,
                                                                            "",
                                                                            params,
                                                                            modVarType,
                                                                            true );

                proposals.addAll( modVarComps );

                //                addMvelCompletions( proposals,
                //                                    documentOffset,
                //                                    "",
                //                                    lastInnerExprContext.getContext().getVariables() );
                //
                //                addMvelCompletions( proposals,
                //                                    documentOffset,
                //                                    "",
                //                                    lastInnerExprContext.getContext().getInputs() );
                //
                //                Collection jdtProps = getJavaCompletionProposals( documentOffset,
                //                                                                  prefix,
                //                                                                  prefix,
                //                                                                  params );
                //
                //                proposals.addAll( jdtProps );
                return proposals;

            }
            //If expression start, and all other cases then get completion for prefix with prev expr var&inputs
            addMvelCompletions( proposals,
                                documentOffset,
                                prefix,
                                lastInnerExprContext.getContext().getVariables() );

            addMvelCompletions( proposals,
                                documentOffset,
                                prefix,
                                lastInnerExprContext.getContext().getInputs() );

            Collection jdtProps = getJavaCompletionProposals( documentOffset,
                                                              prefix,
                                                              prefix,
                                                              params );

            proposals.addAll( jdtProps );

        } catch ( Throwable e ) {
            DroolsEclipsePlugin.log( e );
        }
        Set uniqueProposals = new HashSet();
        addAllNewProposals( uniqueProposals,
                            proposals );
        return uniqueProposals;
    }
    
    private Map getResolvedMvelInputs(Map params) {
        ClassTypeResolver resolver = new ClassTypeResolver( getUniqueImports(),
                                                            ProjectClassLoader.getProjectClassLoader( getEditor() ) );

        Map resolved = new HashMap();
        for ( Iterator iter = params.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            String inputType = (String) entry.getValue();
            try {
                Class type = resolver.resolveType( inputType );
                resolved.put( entry.getKey(),
                              type );
            } catch ( ClassNotFoundException e ) {
                DroolsEclipsePlugin.log( e );
            }
        }
        return resolved;
    }

    class MvelContext {
        private CompiledExpression expression;
        private ParserContext      initialContext;
        private Class              returnedType;
        private boolean            staticFlag;

        public ParserContext getContext() {
            if ( getExpression() != null ) {
                if ( getExpression().getParserContext() != null ) {
                    return getExpression().getParserContext();
                }
            }
            return getInitialContext();
        }

        void setExpression(CompiledExpression expression) {
            this.expression = expression;
        }

        CompiledExpression getExpression() {
            return expression;
        }

        void setInitialContext(ParserContext initialContext) {
            this.initialContext = initialContext;
        }

        ParserContext getInitialContext() {
            return initialContext;
        }

        void setReturnedType(Class returnedType) {
            this.returnedType = returnedType;
        }

        Class getReturnedType() {
            return returnedType;
        }

        public boolean isStaticFlag() {
            return staticFlag;
        }

        public void setStaticFlag(boolean staticFlag) {
            this.staticFlag = staticFlag;
        }
    }

    private MvelContext analyzeMvelExpression(Map params,
                                              DRLInfo drlInfo,
                                              String mvel) {

        String macroMvel = processMacros( mvel );

        String name = context.getRuleName();
        RuleInfo currentRule = getCurrentRule( drlInfo,
                                               name );
        String qName = drlInfo.getPackageName() + "." + name;
        MVELDialect dialect = (MVELDialect) drlInfo.getDialectRegistry().getDialect("mvel");
        ParserContext initialContext = createInitialContext( params,
                                                             qName,
                                                             dialect );
        MvelContext mCon = new MvelContext();
        mCon.setInitialContext( initialContext );

        try {
            ExpressionCompiler compiler = new ExpressionCompiler( macroMvel );
            CompiledExpression expression = compiler.compile( initialContext );
            mCon.setExpression( expression );

            ParserContext compilationContext = compiler.getParserContextState();

            Class lastType = expression.getKnownEgressType();

            //Statics expression may return Class as an egress type
            if ( lastType != null && "java.lang.Class".equals( lastType.getName() ) ) {
                mCon.setStaticFlag( true );
            }

            if ( lastType == null || "java.lang.Object".equals( lastType.getName() ) || "java.lang.Class".equals( lastType.getName() ) ) {
                // attempt to use the property verifier to get
                // a better type  resolution (a recommend by cbrock, though egress gives consistent results)
                lastType = new PropertyVerifier( macroMvel,
                                                 compilationContext ).analyze();
            }

            if ( lastType == null ) {
                lastType = Object.class;
            }

            mCon.setReturnedType( lastType );
        } catch ( Exception e ) {
            //do nothing while doing completion.
        }
        return mCon;
    }

    private static ParserContext createInitialContext(Map params,
                                                      String qualifiedName,
                                                      MVELDialect dialect) {

        final ParserContext context = new ParserContext( dialect.getImports(),
                                                         null,
                                                         qualifiedName );

        if (dialect.getPackgeImports() != null) {
            for ( Iterator it = dialect.getPackgeImports().values().iterator(); it.hasNext(); ) {
                String packageImport = (String) it.next();
                context.addPackageImport( packageImport );
            }
        }
        context.setStrictTypeEnforcement( false );

        context.setInterceptors( dialect.getInterceptors() );
        context.setInputs( params );
        context.addInput( "drools",
                          KnowledgeHelper.class );
        context.setCompiled( true );
        return context;
    }

    public static String processMacros(String mvel) {
        MVELConsequenceBuilder builder = new MVELConsequenceBuilder();
        String macrosProcessedCompilableConsequence = builder.processMacros( mvel.trim() );
        return macrosProcessedCompilableConsequence;
    }

    private static RuleInfo getCurrentRule(DRLInfo drlInfo,
                                    String currentRulename) {
        RuleInfo currentRule = null;
        RuleInfo[] ruleInfos = drlInfo.getRuleInfos();
        for ( int i = 0; i < ruleInfos.length; i++ ) {
            if ( currentRulename.equals( ruleInfos[i].getRuleName() ) ) {
                currentRule = ruleInfos[i];
                break;
            }
        }
        return currentRule;
    }

    /*
     * Completions for object instance members
     */
    private Collection getMvelInstanceCompletionsFromJDT(final int documentOffset,
                                                         final String prefix,
                                                         Map params,
                                                         Class lastType,
                                                         boolean settersOnly) {
        if ( lastType == null ) {
            lastType = Object.class;
        }

        //FIXME: there is a small chance of var name collision using this arbitrary mvdrlofc as a variable name.
        //ideally the variable name should be inferred from the last member of the expression
        final String syntheticVarName = "mvdrlofc";

        String javaText = "\n" + lastType.getPackage().getName() + "." + CompletionUtil.getSimpleClassName( lastType ) + " " + syntheticVarName + ";\n" + syntheticVarName + ".";
        final List list1 = new ArrayList();
        requestJavaCompletionProposals( javaText,
                                        prefix,
                                        documentOffset,
                                        params,
                                        list1 );

        final List list = list1;

        Collection mvelList = RuleCompletionProcessor.mvelifyProposals( list,
                                                                        settersOnly );
        return mvelList;
    }

    /*
     * Completions for static Class members
     */
    private Collection getMvelClassCompletionsFromJDT(final int documentOffset,
                                                      final String prefix,
                                                      Map params,
                                                      Class lastType) {
        if ( lastType == null ) {
            lastType = Object.class;
        }

        //FIXME: there is a small chance of var name collision using this arbitrary mvdrlofc as a variable name.
        //ideally the variable name should be inferred from the last member of the expression

        String javaText = "\n" + CompletionUtil.getSimpleClassName( lastType ) + ".";
        final List list1 = new ArrayList();
        requestJavaCompletionProposals( javaText,
                                        prefix,
                                        documentOffset,
                                        params,
                                        list1 );
        final List list = list1;
        Collection mvelList = RuleCompletionProcessor.mvelifyProposals( list,
                                                                        false );
        return mvelList;
    }

    private static void addMvelCompletions(final Collection proposals,
                                    int documentOffset,
                                    String prefix,
                                    Map inputs) {
        Set newProposals = new HashSet();
        for ( Iterator iter = inputs.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            String prop = (String) entry.getKey();

            Class type = (Class) entry.getValue();
            String display = prop + "  " + CompletionUtil.getSimpleClassName( type );

            RuleCompletionProposal rcp = new RuleCompletionProposal( documentOffset - prefix.length(),
                                                                     prefix.length(),
                                                                     display,
                                                                     prop );
            rcp.setImage( DefaultCompletionProcessor.VARIABLE_ICON );
            newProposals.add( rcp );
        }
        addAllNewProposals( proposals,
                            newProposals );
    }

    public static void addAllNewProposals(final Collection proposals,
                                          final Collection newProposals) {
        for ( Iterator iter = newProposals.iterator(); iter.hasNext(); ) {
            ICompletionProposal newProp = (ICompletionProposal) iter.next();
            String displayString = newProp.getDisplayString();

            //JBRULES-1134 do not add completions if they already exist
            if ( !containsProposal( proposals,
                                    displayString ) ) {
                proposals.add( newProp );
            }
        }
    }

    /**
     * Attempt to compare proposals of different types based on the tokenized display string
     * @param proposals
     * @param newProposal
     * @return true if the collection contains a proposal which matches the new Proposal.
     * The match is based on the first token based on a space split
     */
    public static boolean containsProposal(final Collection proposals,
                                           String newProposal) {
        for ( Iterator iter = proposals.iterator(); iter.hasNext(); ) {
            ICompletionProposal prop = (ICompletionProposal) iter.next();
            String displayString = prop.getDisplayString();
            String[] existings = displayString.split( " " );
            if ( existings.length == 0 ) {
                continue;
            }

            String[] newProposals = newProposal.split( " " );
            if ( newProposals.length == 0 ) {
                continue;
            }

            if ( existings[0].equals( newProposals[0] ) ) {
                return true;
            }
        }
        return false;
    }

    private void addRuleParameters(Map<String, String> result,
                                   Map<String, String[]> ruleParameters) {
        for (Map.Entry<String, String[]> entry: ruleParameters.entrySet()) {
            String name = entry.getKey();
            String clazz = entry.getValue()[0];
            String field = entry.getValue()[1];
            String type;
            if (field == null) {
            	type = clazz;
            } else {
            	type = getPropertyClass( clazz, field );
            }
            result.put( name, type );
        }
    }

    private void addRuleHeaderProposals(List list,
                                        int documentOffset,
                                        String prefix,
                                        String backText) {
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "salience",
                                              "salience ",
                                              DROOLS_ICON ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "no-loop",
                                              "no-loop ",
                                              DROOLS_ICON ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "agenda-group",
                                              "agenda-group ",
                                              DROOLS_ICON ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "duration",
                                              "duration ",
                                              DROOLS_ICON ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "auto-focus",
                                              "auto-focus ",
                                              DROOLS_ICON ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "when",
                                              "when" + System.getProperty( "line.separator" ) + "\t ",
                                              DROOLS_ICON ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "activation-group",
                                              "activation-group ",
                                              DROOLS_ICON ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "date-effective",
                                              "date-effective \"dd-MMM-yyyy\"",
                                              16,
                                              DROOLS_ICON ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "date-expires",
                                              "date-expires \"dd-MMM-yyyy\"",
                                              14,
                                              DROOLS_ICON ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "enabled",
                                              "enabled false",
                                              DROOLS_ICON ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "ruleflow-group",
                                              "ruleflow-group \"\"",
                                              16,
                                              DROOLS_ICON ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "lock-on-active",
                                              "lock-on-active ",
                                              DROOLS_ICON ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "dialect \"java\"",
                                              "dialect \"java\" ",
                                              DROOLS_ICON ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "dialect \"mvel\"",
                                              "dialect \"mvel\" ",
                                              DROOLS_ICON ) );
    }

    private boolean addFactTemplatePropertyProposals(int documentOffset,
                                                     String prefix,
                                                     String templateName,
                                                     List list) {
        FactTemplateDescr descr = getTemplate( templateName );
        if ( descr == null ) {
            return false;
        }
        Iterator iterator = descr.getFields().iterator();
        while ( iterator.hasNext() ) {
            FieldTemplateDescr field = (FieldTemplateDescr) iterator.next();
            String fieldName = field.getName();
            RuleCompletionProposal p = new RuleCompletionProposal( documentOffset - prefix.length(),
                                                                   prefix.length(),
                                                                   fieldName,
                                                                   fieldName + " " );
            p.setImage( METHOD_ICON );
            list.add( p );
        }
        return true;
    }

    /*
     * Filters accessor method proposals to replace them with their mvel expression equivalent
     * For instance a completion for getStatus() would be replaced by a completion for status
     * when asking for stters only, then only setters or writable fields will be returned
     */
    public static Collection mvelifyProposals(List list,
                                              boolean settersOnly) {
        final Collection set = new HashSet();

        for ( Iterator iter = list.iterator(); iter.hasNext(); ) {
            Object o = iter.next();
            if ( o instanceof JavaMethodCompletionProposal ) {
                //methods
                processJavaMethodCompletionProposal( list,
                                                     settersOnly,
                                                     set,
                                                     o );

            } else if ( o instanceof JavaCompletionProposal ) {
                //fields
                processesJavaCompletionProposal( settersOnly,
                                                 set,
                                                 o );
            } else if ( !settersOnly ) {
                set.add( o );
            }
        }
        return set;
    }

    private static void processesJavaCompletionProposal(boolean settersOnly,
                                                        final Collection set,
                                                        Object o) {
        if ( settersOnly ) {
            JavaCompletionProposal jcp = (JavaCompletionProposal) o;
            //TODO: FIXME: this is very fragile as it uses reflection to access the private completion field.
            //Yet this is needed to do mvel filtering based on the method signtures, IF we use the richer JDT completion
            //                    Object field = ReflectionUtils.getField( o,
            //                                                             "fProposal" );
            IJavaElement javaElement = jcp.getJavaElement();
            if ( javaElement.getElementType() == IJavaElement.FIELD ) {
                set.add( o );

            }
        } else {
            set.add( o );
        }
    }

    private static void processJavaMethodCompletionProposal(List list,
                                                            boolean settersOnly,
                                                            final Collection set,
                                                            Object o) {
        LazyJavaCompletionProposal javaProposal = (LazyJavaCompletionProposal) o;
        //TODO: FIXME: this is very fragile as it uses reflection to access the private completion field.
        //Yet this is needed to do mvel filtering based on the method signtures, IF we use the richer JDT completion
        Object field = ReflectionUtils.getField( o,
                                                 "fProposal" );
        if ( field != null && field instanceof CompletionProposal ) {
            CompletionProposal proposal = (CompletionProposal) field;

            String completion = new String( proposal.getCompletion() );

            String propertyOrMethodName = null;

            boolean isSetter = false;
            boolean isAccessor = false;
            if ( settersOnly ) {
                // get the eventual writable property name for that method name and signature
                propertyOrMethodName = CompletionUtil.getWritablePropertyName( completion,
                                                                               proposal.getSignature() );
                //                      if we got a property name that differs from the orginal method name
                //then this is a bean accessor
                isSetter = !completion.equals( propertyOrMethodName );

            } else {
                // get the eventual property name for that method name and signature
                propertyOrMethodName = CompletionUtil.getPropertyName( completion,
                                                                       proposal.getSignature() );
                //if we got a property name that differs from the orginal method name
                //then this is a bean accessor
                isAccessor = !completion.equals( propertyOrMethodName );
            }

            // is the completion for a bean accessor? and do we have already some relevant completion?
            boolean doesNotContainFieldCompletion = DefaultCompletionProcessor.doesNotContainFieldCompletion( propertyOrMethodName,
                                                                                                              list );
            if ( ((settersOnly && isSetter) || (!settersOnly && isAccessor)) && doesNotContainFieldCompletion ) {

                //TODO: craft a better JDTish display name than just the property name
                RuleCompletionProposal prop = new RuleCompletionProposal( javaProposal.getReplacementOffset(),
                                                                          javaProposal.getReplacementLength(),
                                                                          propertyOrMethodName );
                prop.setImage( DefaultCompletionProcessor.VARIABLE_ICON );
                //set high priority such that the completion for accessors shows up first
                prop.setPriority( 1000 );
                set.add( prop );

            }

            else if ( !settersOnly ) {
                set.add( o );
            }
        }
    }
}
