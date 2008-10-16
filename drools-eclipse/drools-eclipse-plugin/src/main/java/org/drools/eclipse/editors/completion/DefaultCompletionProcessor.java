package org.drools.eclipse.editors.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DroolsPluginImages;
import org.drools.eclipse.editors.AbstractRuleEditor;
import org.drools.eclipse.editors.DRLRuleEditor;
import org.drools.lang.descr.FactTemplateDescr;
import org.drools.lang.descr.GlobalDescr;
import org.drools.rule.builder.dialect.java.KnowledgeHelperFixer;
import org.drools.util.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

/**
 * This is the basic completion processor that is used when the editor is outside of a rule block
 * partition.
 * The provides the content assistance for basic rule assembly stuff.
 *
 * This processor will also read behind the current editing position, to provide some context to
 * help provide the pop up list.
 *
 * @author Michael Neale, Kris Verlaenen
 */
public class DefaultCompletionProcessor extends AbstractCompletionProcessor {

    private static final String    NEW_RULE_TEMPLATE           = "rule \"new rule\"" + System.getProperty( "line.separator" ) + "\twhen" + System.getProperty( "line.separator" ) + "\t\t" + System.getProperty( "line.separator" ) + "\tthen"
                                                                 + System.getProperty( "line.separator" ) + "\t\t" + System.getProperty( "line.separator" ) + "end";
    private static final String    NEW_QUERY_TEMPLATE          = "query \"query name\"" + System.getProperty( "line.separator" ) + "\t#conditions" + System.getProperty( "line.separator" ) + "end";
    private static final String    NEW_FUNCTION_TEMPLATE       = "function void yourFunction(Type arg) {" + System.getProperty( "line.separator" ) + "\t/* code goes here*/" + System.getProperty( "line.separator" ) + "}";
    private static final String    NEW_TEMPLATE_TEMPLATE       = "template Name" + System.getProperty( "line.separator" ) + "\t" + System.getProperty( "line.separator" ) + "end";
    protected static final Pattern IMPORT_PATTERN              = Pattern.compile( ".*\n\\W*import\\W[^;\\s]*",
                                                                                  Pattern.DOTALL );
    // TODO: doesn't work for { inside functions
    private static final Pattern   FUNCTION_PATTERN            = Pattern.compile( ".*\n\\W*function\\s+(\\S+)\\s+(\\S+)\\s*\\(([^\\)]*)\\)\\s*\\{([^\\}]*)",
                                                                                  Pattern.DOTALL );
    protected static final Image   VARIABLE_ICON               = DroolsPluginImages.getImage( DroolsPluginImages.VARIABLE );
    protected static final Image   METHOD_ICON                 = DroolsPluginImages.getImage( DroolsPluginImages.METHOD );
    protected static final Image   CLASS_ICON                  = DroolsPluginImages.getImage( DroolsPluginImages.CLASS );

    public DefaultCompletionProcessor(AbstractRuleEditor editor) {
        super( editor );
    }

    protected List getCompletionProposals(ITextViewer viewer,
                                          int documentOffset) {
        try {
            IDocument doc = viewer.getDocument();
            String backText = readBackwards( documentOffset,
                                             doc );

            String prefix = CompletionUtil.stripLastWord( backText );

            List props = null;
            Matcher matcher = IMPORT_PATTERN.matcher( backText );
            if ( matcher.matches() ) {
                String classNameStart = backText.substring( backText.lastIndexOf( "import" ) + 7 );
                props = getAllClassProposals( classNameStart,
                                              documentOffset,
                                              prefix );
            } else {
                matcher = FUNCTION_PATTERN.matcher( backText );
                if ( matcher.matches() ) {
                    // extract function parameters
                    Map params = extractParams( matcher.group( 3 ) );
                    // add global parameters
//                    List globals = getGlobals();
//                    if ( globals != null ) {
//                        for ( Iterator iterator = globals.iterator(); iterator.hasNext(); ) {
//                            GlobalDescr global = (GlobalDescr) iterator.next();
//                            params.put( global.getIdentifier(),
//                                        global.getType() );
//                        }
//                    }
                    String functionText = matcher.group( 4 );
                    props = getJavaCompletionProposals( documentOffset,
                                                        functionText,
                                                        prefix,
                                                        params,
                                                        false );
                    filterProposalsOnPrefix( prefix,
                                             props );
                } else {
                    props = getPossibleProposals( viewer,
                                                  documentOffset,
                                                  backText,
                                                  prefix );
                }
            }
            return props;
        } catch ( Throwable t ) {
            DroolsEclipsePlugin.log( t );
        }
        return null;
    }

    private Map extractParams(String params) {
        Map result = new HashMap();
        String[] parameters = StringUtils.split( params,
                                                 "," );
        for ( int i = 0; i < parameters.length; i++ ) {
            String[] typeAndName = StringUtils.split( parameters[i] );
            if ( typeAndName.length == 2 ) {
                result.put( typeAndName[1],
                            typeAndName[0] );
            }
        }
        return result;
    }

    /*
     * create and returns a java project based on the current editor input or returns null
     */
    private IJavaProject getCurrentJavaProject() {
        IEditorInput input = getEditor().getEditorInput();
        if ( !(input instanceof IFileEditorInput) ) {
            return null;
        }
        IProject project = ((IFileEditorInput) input).getFile().getProject();
        IJavaProject javaProject = JavaCore.create( project );
        return javaProject;
    }

    protected List getAllClassProposals(final String classNameStart,
                                        final int documentOffset,
                                        final String prefix) {
        List result = new ArrayList();
        IJavaProject javaProject = getCurrentJavaProject();
        if ( javaProject == null ) {
            return result;
        }
        CompletionProposalCollector collector = new CompletionProposalCollector( javaProject ) {
            public void accept(CompletionProposal proposal) {
                if ( proposal.getKind() == org.eclipse.jdt.core.CompletionProposal.PACKAGE_REF || proposal.getKind() == org.eclipse.jdt.core.CompletionProposal.TYPE_REF ) {
                    super.accept( proposal );
                }
            }
        };
        collector.acceptContext( new CompletionContext() );
        try {
            IEvaluationContext evalContext = javaProject.newEvaluationContext();
            evalContext.codeComplete( classNameStart,
                                      classNameStart.length(),
                                      collector );
            IJavaCompletionProposal[] proposals = collector.getJavaCompletionProposals();
            for ( int i = 0; i < proposals.length; i++ ) {
                if ( proposals[i] instanceof AbstractJavaCompletionProposal ) {
                    AbstractJavaCompletionProposal javaProposal = (AbstractJavaCompletionProposal) proposals[i];
                    int replacementOffset = documentOffset - (classNameStart.length() - javaProposal.getReplacementOffset());
                    javaProposal.setReplacementOffset( replacementOffset );
                    if ( javaProposal instanceof LazyJavaTypeCompletionProposal ) {
                        String completionPrefix = classNameStart.substring( classNameStart.length() - javaProposal.getReplacementLength() );
                        int dotIndex = completionPrefix.lastIndexOf( '.' );
                        // match up to the last dot in order to make higher level matching still work (camel case...)
                        if ( dotIndex != -1 ) {
                            javaProposal.setReplacementString( ((LazyJavaTypeCompletionProposal) javaProposal).getQualifiedTypeName() );
                        }
                    }
                    result.add( proposals[i] );
                }
            }
        } catch ( Throwable t ) {
            DroolsEclipsePlugin.log( t );
        }
        return result;
    }

    protected List getPossibleProposals(ITextViewer viewer,
                                        int documentOffset,
                                        String backText,
                                        final String prefix) {
        List list = new ArrayList();
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "rule",
                                              NEW_RULE_TEMPLATE,
                                              6 ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "import",
                                              "import " ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "expander",
                                              "expander " ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "global",
                                              "global " ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "package",
                                              "package " ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "query",
                                              NEW_QUERY_TEMPLATE ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "function",
                                              NEW_FUNCTION_TEMPLATE,
                                              14 ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "template",
                                              NEW_TEMPLATE_TEMPLATE,
                                              9 ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "dialect \"java\"",
                                              "dialect \"java\" " ) );
        list.add( new RuleCompletionProposal( documentOffset - prefix.length(),
                                              prefix.length(),
                                              "dialect \"mvel\"",
                                              "dialect \"mvel\" " ) );
        filterProposalsOnPrefix( prefix,
                                 list );
        return list;
    }

    protected List getJavaCompletionProposals(final int documentOffset,
                                              final String javaText,
                                              final String prefix,
                                              Map params) {
        return getJavaCompletionProposals(documentOffset, javaText, prefix, params, true);
    }
    
    protected List getJavaCompletionProposals(final int documentOffset,
                                              final String javaText,
                                              final String prefix,
                                              Map params,
                                              boolean useDrools) {
        final List list = new ArrayList();
        requestJavaCompletionProposals( javaText,
                                        prefix,
                                        documentOffset,
                                        params,
                                        list,
                                        useDrools );
        return list;
    }

    /*
     * do we already have a completion for that string that would be either a local variable or a field?
     */
    protected static boolean doesNotContainFieldCompletion(String completion,
                                                         List completions) {
        if ( completion == null || completion.length() == 0 || completions == null ) {
            return false;
        }
        for ( Iterator iter = completions.iterator(); iter.hasNext(); ) {
            Object o = iter.next();
            if ( o instanceof AbstractJavaCompletionProposal ) {
                AbstractJavaCompletionProposal prop = (AbstractJavaCompletionProposal) o;
                String content = prop.getReplacementString();
                if ( completion.equals( content ) ) {
                    IJavaElement javaElement = prop.getJavaElement();
                    if ( javaElement instanceof ILocalVariable || javaElement instanceof IField ) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected void requestJavaCompletionProposals(final String javaText,
                                                  final String prefix,
                                                  final int documentOffset,
                                                  Map params,
                                                  Collection results) {
        requestJavaCompletionProposals(javaText, prefix, documentOffset, params, results, true);
    }

    protected void requestJavaCompletionProposals(final String javaText,
                                                  final String prefix,
                                                  final int documentOffset,
                                                  Map params,
                                                  Collection results,
                                                  boolean useDrools) {


        String javaTextWithoutPrefix = CompletionUtil.getTextWithoutPrefix( javaText,
                                                                            prefix );
        // boolean to filter default Object methods produced by code completion when in the beginning of a statement
        boolean filterObjectMethods = false;
        if ( "".equals( javaTextWithoutPrefix.trim() ) || CompletionUtil.START_OF_NEW_JAVA_STATEMENT.matcher( javaTextWithoutPrefix ).matches() ) {
            filterObjectMethods = true;
        }
        IJavaProject javaProject = getCurrentJavaProject();
        if ( javaProject == null ) {
            return;
        }

        CompletionProposalCollector collector = new CompletionProposalCollector( javaProject );
        collector.acceptContext( new CompletionContext() );

        try {
            IEvaluationContext evalContext = javaProject.newEvaluationContext();
            List imports = getImports();
            if ( imports != null && imports.size() > 0 ) {
                evalContext.setImports( (String[]) imports.toArray( new String[imports.size()] ) );
            }
            StringBuffer javaTextWithParams = new StringBuffer();
            Iterator iterator = params.entrySet().iterator();
            while ( iterator.hasNext() ) {
                Map.Entry entry = (Map.Entry) iterator.next();
                // this does not seem to work, so adding variables manually
                // evalContext.newVariable((String) entry.getValue(), (String) entry.getKey(), null);
                javaTextWithParams.append( entry.getValue() + " " + entry.getKey() + ";\n" );
            }
            if (useDrools) {
                javaTextWithParams.append( "org.drools.spi.KnowledgeHelper drools;" );
            }
            javaTextWithParams.append( javaText );
            String jtext = javaTextWithParams.toString();
            String fixedText = new KnowledgeHelperFixer().fix( jtext );

            evalContext.codeComplete( fixedText,
                                      fixedText.length(),
                                      collector );
            IJavaCompletionProposal[] proposals = collector.getJavaCompletionProposals();
            for ( int i = 0; i < proposals.length; i++ ) {
                if ( proposals[i] instanceof AbstractJavaCompletionProposal ) {
                    AbstractJavaCompletionProposal javaProposal = (AbstractJavaCompletionProposal) proposals[i];
                    int replacementOffset = documentOffset - (fixedText.length() - javaProposal.getReplacementOffset());
                    javaProposal.setReplacementOffset( replacementOffset );
                    if ( javaProposal instanceof LazyJavaTypeCompletionProposal ) {
                        String completionPrefix = javaText.substring( javaText.length() - javaProposal.getReplacementLength() );
                        int dotIndex = completionPrefix.lastIndexOf( '.' );
                        // match up to the last dot in order to make higher level matching still work (camel case...)
                        if ( dotIndex != -1 ) {
                            javaProposal.setReplacementString( ((LazyJavaTypeCompletionProposal) javaProposal).getQualifiedTypeName() );
                        }
                    }
                    if ( !filterObjectMethods || !(proposals[i] instanceof JavaMethodCompletionProposal) ) {
                        results.add( proposals[i] );
                    }
                }
            }
        } catch ( Throwable t ) {
            DroolsEclipsePlugin.log( t );
        }
    }

    protected String getPackage() {
        if ( getEditor() instanceof DRLRuleEditor ) {
            return ((DRLRuleEditor) getEditor()).getPackage();
        }
        return "";
    }

    protected List getImports() {
        if ( getEditor() instanceof DRLRuleEditor ) {
            return ((DRLRuleEditor) getEditor()).getImports();
        }
        return Collections.EMPTY_LIST;
    }

    protected Set getUniqueImports() {
        HashSet set = new HashSet();
        set.addAll( getImports() );
        return set;
    }

    protected List getFunctions() {
        if ( getEditor() instanceof DRLRuleEditor ) {
            return ((DRLRuleEditor) getEditor()).getFunctions();
        }
        return Collections.EMPTY_LIST;
    }

    protected Map getAttributes() {
        if ( getEditor() instanceof DRLRuleEditor ) {
            return ((DRLRuleEditor) getEditor()).getAttributes();
        }
        return Collections.EMPTY_MAP;
    }

    protected Set getTemplates() {
        if ( getEditor() instanceof DRLRuleEditor ) {
            return ((DRLRuleEditor) getEditor()).getTemplates();
        }
        return Collections.EMPTY_SET;
    }

    protected FactTemplateDescr getTemplate(String name) {
        if ( getEditor() instanceof DRLRuleEditor ) {
            return ((DRLRuleEditor) getEditor()).getTemplate( name );
        }
        return null;
    }

    protected List<GlobalDescr> getGlobals() {
        if ( getEditor() instanceof DRLRuleEditor ) {
            return ((DRLRuleEditor) getEditor()).getGlobals();
        }
        return Collections.EMPTY_LIST;
    }

    protected List getClassesInPackage() {
        if ( getEditor() instanceof DRLRuleEditor ) {
            return ((DRLRuleEditor) getEditor()).getClassesInPackage();
        }
        return Collections.EMPTY_LIST;
    }
}
