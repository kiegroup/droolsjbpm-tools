package org.drools.ide.editors.completion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.DroolsPluginImages;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.EditorPart;


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

    private static final String NEW_RULE_TEMPLATE = "rule \"new rule\"\n\n\twhen\n\n\tthen\n\nend";
    private static final String NEW_QUERY_TEMPLATE = "query \"query name\"\n\t#conditions\nend";
    private static final String NEW_FUNCTION_TEMPLATE = "function void yourFunction(Type arg) {\n\t/* code goes here*/\n}";
    private static final Pattern IMPORT_PATTERN = Pattern.compile(".*\\Wimport\\W[^;]*", Pattern.DOTALL);

    public DefaultCompletionProcessor(EditorPart editor) {
    	super(editor);
    }
    
	protected List getCompletionProposals(ITextViewer viewer, int documentOffset) {
        try {
	        IDocument doc = viewer.getDocument();
	        String backText = readBackwards( documentOffset, doc );            
	
	        String prefix = "";
	        prefix = stripWhiteSpace(backText);
	        
	        List props = null;
	        if (IMPORT_PATTERN.matcher(backText).matches()) {
	        	String classNameStart = backText.substring(backText.lastIndexOf("import") + 7);
	        	props = getAllClassProposals(classNameStart, documentOffset);
	        } else {
	        	props = getPossibleProposals(viewer, documentOffset, backText, prefix);
	        }
	        return props;
        } catch (Throwable t) {
        	DroolsIDEPlugin.log(t);
        }
        return null;
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
					System.out.println(className);
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
        list.add(new RuleCompletionProposal(prefix.length(), "function", NEW_FUNCTION_TEMPLATE));
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
        if (startPart == 0) {
            if (documentOffset < 32) {
                startPart = 0;
            } else {
                startPart = documentOffset - 32;
            }
        }
        
        String prefix = doc.get(startPart, documentOffset - startPart);
        return prefix;
    }

    /** Looks behind, gets stuff after the white space. Basically ripping out the last word.*/
    protected String stripWhiteSpace(String prefix) {
    	if (prefix.charAt(prefix.length() - 1) == ' ') {
    		return "";
    	} else {
	        char[] c = prefix.toCharArray();
	        int start = 0;
	        for (int i = c.length - 1; i >=0; i-- ) {
	            if (Character.isWhitespace(c[i])) {
	                start = i + 1;
	                break;
	            }
	        }
	        prefix = prefix.substring(start, prefix.length());
	        return prefix;
    	}
    }
}
