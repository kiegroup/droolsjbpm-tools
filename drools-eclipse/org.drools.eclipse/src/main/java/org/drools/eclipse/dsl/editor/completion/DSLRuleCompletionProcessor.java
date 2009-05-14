package org.drools.eclipse.dsl.editor.completion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DroolsPluginImages;
import org.drools.eclipse.dsl.editor.DSLAdapter;
import org.drools.eclipse.dsl.editor.DSLRuleEditor;
import org.drools.eclipse.editors.AbstractRuleEditor;
import org.drools.eclipse.editors.completion.RuleCompletionProcessor;
import org.drools.eclipse.editors.completion.RuleCompletionProposal;
import org.drools.lang.Location;
import org.eclipse.swt.graphics.Image;

/**
 * For handling DSL rules.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class DSLRuleCompletionProcessor extends RuleCompletionProcessor {

	private static final Image DSL_ICON = 
		DroolsPluginImages.getImage(DroolsPluginImages.DSL_EXPRESSION);

	public DSLRuleCompletionProcessor(AbstractRuleEditor editor) {
		super(editor);
	}

    protected DSLRuleEditor getDSLRuleEditor() {
    	return (DSLRuleEditor) getEditor();
    }
    
	protected void addRHSCompletionProposals(List list, int documentOffset, String prefix, String backText,
			String conditions, String consequence) {
		// super.addRHSCompletionProposals(list, documentOffset, prefix, backText, conditions, consequence);
		DSLAdapter adapter = getDSLRuleEditor().getDSLAdapter();
		if (adapter != null) {
			List dslConsequences = adapter.getDSLTree().getConsequenceChildrenList(prefix, true);
			addDSLProposals(list, documentOffset, prefix, dslConsequences);
		}
	}
	
	protected void addLHSCompletionProposals(List list, int documentOffset,
			Location location, String prefix, String backText) {
		// super.addLHSCompletionProposals(list, documentOffset, location, prefix, backText);
		DSLAdapter adapter = getDSLRuleEditor().getDSLAdapter();
		if (adapter != null) {
			String lastobj = this.getLastNonDashLine(backText);
			String last = this.getLastLine(backText);
			// we have to check if the last line is when. if it is we set
			// the last line to zero length string
			boolean firstLine = false;
			if (lastobj.equals("when")) {
				firstLine = true;
				last = "";
				lastobj = "*";
			}
			last = last.trim();
			// pass the last string in the backText to getProposals
			List dslConditions = this.getProposals(adapter, lastobj, last, firstLine);
			// if we couldn't find any matches, we add the list from
			// the DSLAdapter so that there's something
//			if (dslConditions.size() == 0) {
//				dslConditions.addAll(adapter.listConditionItems());
//			}
			addDSLProposals(list, documentOffset, last, dslConditions);
		}
	}
	
	private void addDSLProposals(final List list, int documentOffset, final String prefix, List dslItems) {
		Iterator iterator = dslItems.iterator();
		while (iterator.hasNext()) {
			String consequence = (String) iterator.next();
			RuleCompletionProposal p = new RuleCompletionProposal(
				documentOffset - prefix.length(), prefix.length(), consequence);
			p.setImage(DSL_ICON);
			list.add(p);
		}
	}

	/**
	 * because of how the backText works, we need to get the last line, so that
	 * we can pass it to the DSLUtility
	 * 
	 * @param backText
	 * @return
	 */
	public String getLastLine(String backText) {
		BufferedReader breader = new BufferedReader(new StringReader(backText));
		String last = "";
		String line = null;
		try {
			while ((line = breader.readLine()) != null) {
				// only if the line has text do we set last to it
				if (line.length() > 0) {
					last = line;
				}
			}
		} catch (IOException e) {
			DroolsEclipsePlugin.log(e);
		}
		// now that all the conditions for a single object are on the same line
		// we need to check for the left parenthesis
		if (last.indexOf("(") > -1) {
			last = last.substring(last.lastIndexOf("(") + 1);
		}
		// if the string has a comma "," we get the substring starting from
		// the index after the last comma
		if (last.indexOf(",") > -1) {
			last = last.substring(last.lastIndexOf(",") + 1);
		}
		// if the line ends with right parenthesis, we change it to zero length
		// string
		if (last.endsWith(")")) {
			last = "";
		}
		return last;
	}

	/**
	 * Returns the last line that doesn't start with a dash
	 * 
	 * @param backText
	 * @return
	 */
	public String getLastNonDashLine(String backText) {
		BufferedReader breader = new BufferedReader(new StringReader(backText));
		String last = "";
		String line = null;
		try {
			while ((line = breader.readLine()) != null) {
				// there may be blank lines, so we trim first
				line = line.trim();
				// only if the line has text do we set last to it
				if (line.length() > 0 && !line.startsWith("-")) {
					last = line;
				}
			}
		} catch (IOException e) {
			DroolsEclipsePlugin.log(e);
		}
		if (last.indexOf("(") > -1 && !last.endsWith(")")) {
			last = last.substring(0, last.indexOf("("));
		} else if (last.indexOf("(") > -1 && last.endsWith(")")) {
			last = "";
		}
		return last;
	}

	/**
	 * The DSLTree is configurable. It can either return just the child of the
	 * last token found, or it can traverse the tree and generate all the
	 * combinations beneath the last matching node. TODO I don't know how to add
	 * configuration to the editor, so it needs to be hooked up to the
	 * configuration for the editor later.
	 * 
	 * @param last
	 * @return
	 */
	protected List getProposals(DSLAdapter adapter, String obj, String last, boolean firstLine) {
		if (last.length() == 0) {
			last = " ";
		}
		return adapter.getDSLTree().getChildrenList(obj, last, true, firstLine);
	}
}
