package org.drools.ide.editors.completion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.EditorPart;


/**
 * This is the basic completion processor that is used when the editor is outside of a rule block
 * partition.
 * The provides the content assistance for basic rule assembly stuff.
 * 
 * Subclasses enhance this when a user is actually editing a rule.
 * 
 * This processor will also read behind the current editing position, to provide some context to 
 * help provide the pop up list.
 * 
 * @author Michael Neale
 */
public class DefaultCompletionProcessor implements IContentAssistProcessor {

    private static final String NEW_RULE_TEMPLATE = "rule \"new rule\"\n\n\twhen\n\n\tthen\n\nend";
    private static final String NEW_QUERY_TEMPLATE = "query \"query name\"\n\t#conditions\nend";
    private static final String NEW_FUNCTION_TEMPLATE = "function void yourFunction(Type arg) {\n\t/* code goes here*/\n}";
    private static final Pattern IMPORT_PATTERN = Pattern.compile(".*\\Wimport\\W[^;]*", Pattern.DOTALL);

    private EditorPart editor;
    
    public DefaultCompletionProcessor(EditorPart editor) {
    	this.editor = editor;
    }
    
    /** Subclasses *should* override this to change the list. */
    protected List getPossibleProposals(ITextViewer viewer, String backText) {
        List list = new ArrayList();
        list.add(new RuleCompletionProposal("rule", NEW_RULE_TEMPLATE, 6));
        list.add(new RuleCompletionProposal("import"));
        list.add(new RuleCompletionProposal("expander"));
        list.add(new RuleCompletionProposal("global"));
        list.add(new RuleCompletionProposal("package"));
        list.add(new RuleCompletionProposal("query", NEW_QUERY_TEMPLATE));
        list.add(new RuleCompletionProposal("function", NEW_FUNCTION_TEMPLATE));
        return list;
    }


	/**
     * This will filter based on what was typed in previously.
	 */
	public ICompletionProposal[] computeCompletionProposals(
		ITextViewer viewer,
		int documentOffset) {

//        partitionDebug( viewer,
//                        documentOffset );

        Offset offset = new Offset(documentOffset);
        
        
        try {
            IDocument doc = viewer.getDocument();

            
            String backText = readBackwards( offset,
                                           doc );            

            //System.out.println("back text: " + backText);
            
            if (doesPrefixExist(backText)) {
                String prefix = stripWhiteSpace(backText);
                offset.prefix = prefix;
            }
            
            List props = null;
            if (IMPORT_PATTERN.matcher(backText).matches()) {
            	String classNameStart = backText.substring(backText.lastIndexOf("import") + 7);
            	props = getAllClassProposals(classNameStart);
            } else {
            	props = getPossibleProposals(viewer, backText);
                props = filterProposals(offset, props);
            }
            
            ICompletionProposal[] result = new ICompletionProposal[props.size()];
            for (int i = 0; i < props.size(); i++) {
                result[i] = makeProposal( documentOffset,
                              offset,
                              props,
                              i );
            }        
            return result;
            
        } catch (BadLocationException e) {
        }
        
        return null;
		
	}


	private List getAllClassProposals(String classNameStart) {
		final List list = new ArrayList();
		IEditorInput input = editor.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IProject project = ((IFileEditorInput) input).getFile().getProject();
			IJavaProject javaProject = JavaCore.create(project);
			
			CompletionRequestor requestor = new CompletionRequestor() {
				public void accept(org.eclipse.jdt.core.CompletionProposal proposal) {
					String className = new String(proposal.getCompletion());
					String completion = className + (proposal.getKind() == org.eclipse.jdt.core.CompletionProposal.PACKAGE_REF ? ".*" : "") + ";";
					list.add(new RuleCompletionProposal(className, completion));
				}
			};

			try {
				javaProject.newEvaluationContext().codeComplete("import " + classNameStart, classNameStart.length() + 4, requestor);
			} catch (Throwable t) {
				DroolsIDEPlugin.log(t);
			}
		}
		return list;
	}
			
   /**
     * Just used for debugging, when it all gets to much.
     */
    private void partitionDebug(ITextViewer viewer,
                                int documentOffset) {
        try {
            ITypedRegion region = viewer.getDocument().getPartition(documentOffset);
            System.out.println("Region type: " + region.getType());
            System.out.println("Offset: " + documentOffset);
            System.out.println("Region offset: " + region.getOffset());
            System.out.println("Region length: " + region.getLength());
            
            
        }
        catch ( BadLocationException e1 ) {
            
            e1.printStackTrace();
        }
    }


    protected CompletionProposal makeProposal(int documentOffset,
                                              Offset offset,
                                              List props,
                                              int i) {
        RuleCompletionProposal proposal = (RuleCompletionProposal) props.get(i);
        
//        return new CompletionProposal(replacement, 
//        				documentOffset - offset.getPrefixLength(), offset.getPrefixLength(), replacement.length());
        return new CompletionProposal(proposal.getContent(), 
          documentOffset - offset.getPrefixLength(), offset.getPrefixLength(), proposal.getOffset(), 
              null, proposal.getDisplay(), null, null);
        
    }




    /** Filter the proposals based on what was typed. */
    private List filterProposals(Offset offset, List props) throws BadLocationException {
        if (offset.prefix != null) {
            props = filterList(props, offset.prefix);
        }
        return props;
    }


    /**
     * Read some text from behind the cursor position.
     * This provides context to both filter what is shown based
     * on what the user has typed in, and also to provide more information for the 
     * list of suggestions based on context.
     */
    private String readBackwards(Offset offset,
                                 IDocument doc) throws BadLocationException {
        int startPart = doc.getPartition(offset.documentOffset).getOffset();
        if (startPart == 0) {
            if (offset.documentOffset < 32) {
                startPart = 0;
            } else {
                startPart = offset.documentOffset - 32;
            }
        }
        
        String prefix = doc.get(startPart, 
                                offset.documentOffset - startPart);
        return prefix;
    }

    
    /** Filter the list by prefix */
    List filterList(List list, String prefix) {
        
        List result = new ArrayList();
        for ( int i = 0; i < list.size(); i++ ) {
            RuleCompletionProposal item = (RuleCompletionProposal) list.get(i);
            if (item.getDisplay().startsWith(prefix)) {
                result.add(item);
            }
        }
        return result;
    }

    boolean doesPrefixExist(String s) {
        if (s.length() == 0) return false;
        return !(s.charAt(s.length() - 1) == ' ');
    }
    
    /** Looks behind, gets stuff after the white space. Basically ripping out the last word.*/
    String stripWhiteSpace(String prefix) {
        
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
    
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	// For Context information 
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public IContextInformation[] computeContextInformation(
		ITextViewer viewer,
		int documentOffset) {
		return null;
	}

	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public String getErrorMessage() {
		return null;
	}
    
    
    /** used to track where to drop in replacement stuff */
    static class Offset {
        
        public int documentOffset; 
        public String prefix;
        public Offset(int documentOffset) {
            this.documentOffset = documentOffset;
        }
        
        
        public int getPrefixLength() {
            if (prefix == null) return 0;
            return prefix.length();
        }
    }
}
