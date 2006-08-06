package org.drools.ide.editors.completion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.DroolsPluginImages;
import org.drools.ide.editors.DRLRuleEditor;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jface.text.BadLocationException;
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
 * @author Michael Neale
 */
public class DefaultCompletionProcessor extends AbstractCompletionProcessor {

    private static final String NEW_RULE_TEMPLATE = "rule \"new rule\"" + System.getProperty("line.separator") + "\twhen" + System.getProperty("line.separator") + "\t\t" + System.getProperty("line.separator") + "\tthen" + System.getProperty("line.separator") + "\t\t" + System.getProperty("line.separator") + "end";
    private static final String NEW_QUERY_TEMPLATE = "query \"query name\"" + System.getProperty("line.separator") + "\t#conditions" + System.getProperty("line.separator") + "end";
    private static final String NEW_FUNCTION_TEMPLATE = "function void yourFunction(Type arg) {" + System.getProperty("line.separator") + "\t/* code goes here*/" + System.getProperty("line.separator") + "}";
    private static final String NEW_TEMPLATE_TEMPLATE = "template Name" + System.getProperty("line.separator") + "\t" + System.getProperty("line.separator") + "end";
    private static final Pattern IMPORT_PATTERN = Pattern.compile(".*\n\\W*import\\W[^;\\s]*", Pattern.DOTALL);
    // TODO: doesn't work for { inside functions
    private static final Pattern FUNCTION_PATTERN = Pattern.compile( ".*\n\\W*function\\s+(\\S+)\\s+(\\S+)\\s*\\(([^\\)]*)\\)\\s*\\{([^\\}]*)", Pattern.DOTALL);
    protected static final Image VARIABLE_ICON = DroolsPluginImages.getImage(DroolsPluginImages.VARIABLE);
    protected static final Image methodIcon = DroolsPluginImages.getImage(DroolsPluginImages.METHOD);
    protected static final Pattern START_OF_NEW_JAVA_STATEMENT = Pattern.compile(".*[;{}]\\s*", Pattern.DOTALL);

    public DefaultCompletionProcessor(DRLRuleEditor editor) {
    	super(editor);
    }
    
    protected DRLRuleEditor getDRLEditor() {
    	return (DRLRuleEditor) getEditor();
    }
    
	protected List getCompletionProposals(ITextViewer viewer, int documentOffset) {
        try {
	        IDocument doc = viewer.getDocument();
	        String backText = readBackwards( documentOffset, doc );            
	
	        String prefix = "";
	        prefix = stripWhiteSpace(backText);
	        
	        List props = null;
	        Matcher matcher = IMPORT_PATTERN.matcher(backText); 
	        if (matcher.matches()) {
	        	String classNameStart = backText.substring(backText.lastIndexOf("import") + 7);
	        	props = getAllClassProposals(classNameStart, documentOffset);
	        } else {
	        	matcher = FUNCTION_PATTERN.matcher(backText);
	        	if (matcher.matches()) {
		        	Map params = extractParams(matcher.group(3));
		        	String functionText = matcher.group(4);
	        		props = getJavaCompletionProposals(functionText, prefix, params);
	        		filterProposalsOnPrefix(prefix, props);
	        	} else {
	        		props = getPossibleProposals(viewer, documentOffset, backText, prefix);
	        	}
	        }
	        return props;
        } catch (Throwable t) {
        	DroolsIDEPlugin.log(t);
        }
        return null;
	}
	
	private Map extractParams(String params) {
		Map result = new HashMap();
		String[] parameters = StringUtils.split(params, ",");
		for (int i = 0; i < parameters.length; i++) {
			String[] typeAndName = StringUtils.split(parameters[i]);
			if (typeAndName.length == 2) {
				result.put(typeAndName[1], typeAndName[0]);
			}
		}
		return result;
	}

	private List getAllClassProposals(final String classNameStart, final int documentOffset) {
		final List list = new ArrayList();
		IEditorInput input = getEditor().getEditorInput();
		if (input instanceof IFileEditorInput) {
			IProject project = ((IFileEditorInput) input).getFile().getProject();
			IJavaProject javaProject = JavaCore.create(project);
			
			CompletionRequestor requestor = new CompletionRequestor() {
				public void accept(org.eclipse.jdt.core.CompletionProposal proposal) {
					String className = new String(proposal.getCompletion());
					if (proposal.getKind() == org.eclipse.jdt.core.CompletionProposal.PACKAGE_REF) {
						RuleCompletionProposal prop = new RuleCompletionProposal(classNameStart.length(), className, className + ".");
						prop.setImage(DroolsPluginImages.getImage(DroolsPluginImages.PACKAGE));
						list.add(prop);
					} else if (proposal.getKind() == org.eclipse.jdt.core.CompletionProposal.TYPE_REF) {
						RuleCompletionProposal prop = new RuleCompletionProposal(classNameStart.length(), className, className + ";"); 
						prop.setImage(DroolsPluginImages.getImage(DroolsPluginImages.CLASS));
						list.add(prop);
					}
					// ignore all other proposals
				}
			};

			try {
				javaProject.newEvaluationContext().codeComplete(classNameStart, classNameStart.length(), requestor);
			} catch (Throwable t) {
				DroolsIDEPlugin.log(t);
			}
		}
		return list;
	}
			
    protected List getPossibleProposals(ITextViewer viewer, int documentOffset, String backText, final String prefix) {
        List list = new ArrayList();
        list.add(new RuleCompletionProposal(prefix.length(), "rule", NEW_RULE_TEMPLATE, 6));
        list.add(new RuleCompletionProposal(prefix.length(), "import", "import "));
        list.add(new RuleCompletionProposal(prefix.length(), "expander", "expander "));
        list.add(new RuleCompletionProposal(prefix.length(), "global", "global "));
        list.add(new RuleCompletionProposal(prefix.length(), "package", "package "));
        list.add(new RuleCompletionProposal(prefix.length(), "query", NEW_QUERY_TEMPLATE));
        list.add(new RuleCompletionProposal(prefix.length(), "function", NEW_FUNCTION_TEMPLATE, 14));
        list.add(new RuleCompletionProposal(prefix.length(), "template", NEW_TEMPLATE_TEMPLATE, 9));
        filterProposalsOnPrefix(prefix, list);
        return list;
    }

    /**
     * Read some text from behind the cursor position.
     * This provides context to both filter what is shown based
     * on what the user has typed in, and also to provide more information for the 
     * list of suggestions based on context.
     */
    protected String readBackwards(int documentOffset, IDocument doc) throws BadLocationException {
        int startPart = doc.getPartition(documentOffset).getOffset();
//        if (startPart == 0) {
//            if (documentOffset < 32) {
//                startPart = 0;
//            } else {
//                startPart = documentOffset - 32;
//            }
//        }
        
        String prefix = doc.get(startPart, documentOffset - startPart);
        return prefix;
    }

    /** Looks behind, gets stuff after the white space. Basically ripping out the last word.*/
    protected String stripWhiteSpace(String prefix) {
    	if ("".equals(prefix)) { 
    		return prefix;
    	}
    	if (prefix.charAt(prefix.length() - 1) == ' ') {
    		return "";
    	} else {
	        char[] c = prefix.toCharArray();
	        int start = 0;
	        for (int i = c.length - 1; i >=0; i-- ) {
	            if (Character.isWhitespace(c[i]) || c[i] == '(' || c[i] == ':' || c[i] == ';' || c[i] == '=' || c[i] == '<' || c[i] == '>' || c[i] == '.' || c[i] == '{' || c[i] == '}') {
	                start = i + 1;
	                break;
	            }
	        }
	        prefix = prefix.substring(start, prefix.length());
	        return prefix;
    	}
    }
    
	protected List getJavaCompletionProposals(final String javaText, final String prefix, Map params) {
		final List list = new ArrayList();
		IEditorInput input = getEditor().getEditorInput();
		if (input instanceof IFileEditorInput) {
			IProject project = ((IFileEditorInput) input).getFile().getProject();
			IJavaProject javaProject = JavaCore.create(project);
			
			CompletionRequestor requestor = new CompletionRequestor() {
				public void accept(CompletionProposal proposal) {
					// TODO set other proposal properties too (display name, icon, ...)
					String completion = new String(proposal.getCompletion());
					RuleCompletionProposal prop = new RuleCompletionProposal(prefix.length(), completion);
					switch (proposal.getKind()) {
						case CompletionProposal.LOCAL_VARIABLE_REF: 
							prop.setImage(VARIABLE_ICON);
							break;
						case CompletionProposal.METHOD_REF:
							// TODO: Object methods are proposed when in the start of a line
							String javaTextWithoutPrefix = javaText.substring(0, javaText.length() - prefix.length());
							if ("".equals(javaTextWithoutPrefix.trim()) || START_OF_NEW_JAVA_STATEMENT.matcher(javaTextWithoutPrefix).matches()) {
								return;
							}
							prop.setImage(methodIcon);
							break;
						default:
					}
					list.add(prop);
				}
			};

			try {
				IEvaluationContext evalContext = javaProject.newEvaluationContext();
				List imports = getDRLEditor().getImports();
				if (imports != null && imports.size() > 0) {
					evalContext.setImports((String[]) imports.toArray(new String[imports.size()]));
				}
				StringBuffer javaTextWithParams = new StringBuffer();
				Iterator iterator = params.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry entry = (Map.Entry) iterator.next();
					// this does not seem to work, so adding variables manually
					// evalContext.newVariable((String) entry.getValue(), (String) entry.getKey(), null);
					javaTextWithParams.append(entry.getValue() + " " + entry.getKey() + ";\n");
				}
				javaTextWithParams.append(javaText);
				String text = javaTextWithParams.toString();
				evalContext.codeComplete(text, text.length(), requestor);
			} catch (Throwable t) {
				DroolsIDEPlugin.log(t);
			}
		}
		return list;
	}

}
