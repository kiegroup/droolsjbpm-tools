package org.drools.eclipse.editors.scanners;

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.editors.ColorManager;
import org.drools.eclipse.editors.Keywords;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;



/**
 * Basic keyword scanner for syntax highlighting.
 * 
 * @author Michael Neale
 */
public class DRLScanner extends RuleBasedScanner {
    
	private static final Color KEYWORD_COLOR = ColorManager.getInstance().getColor(ColorManager.KEYWORD);
	private static final Color COMMENT_COLOR = ColorManager.getInstance().getColor(ColorManager.SINGLE_LINE_COMMENT);
    private static final Color STRING_COLOR = ColorManager.getInstance().getColor(ColorManager.STRING);
    private static final Color DEFAULT_COLOR = ColorManager.getInstance().getColor(ColorManager.DEFAULT);
    
    private static final String[] DROOLS_KEYWORDS = Keywords.getInstance().getAllDroolsKeywords();
    private static final String[] JAVA_KEYWORDS = Keywords.getInstance().getAllJavaKeywords();

    private static final String[] TYPES = { "void", "boolean", "char", "byte", "short", "strictfp", "int", "long", "float", "double" }; 

    private static final String[] CONSTANTS = { "false", "true", "null" }; 
    

	public DRLScanner() {
        
		IToken keyword = new Token(new TextAttribute(KEYWORD_COLOR, null, SWT.BOLD));
		IToken comment= new Token(new TextAttribute(COMMENT_COLOR));
		IToken string = new Token(new TextAttribute(STRING_COLOR));
		IToken other = new Token(new TextAttribute(DEFAULT_COLOR));
        
        List rules = new ArrayList();

		rules.add(new EndOfLineRule("//", comment));
        rules.add(new EndOfLineRule("#", comment));
        
        // Add rule for strings and character constants.
        rules.add(new SingleLineRule("\"", "\"", string, '\\')); 
        rules.add(new SingleLineRule("'", "'", string, '\\'));         
        
        //for unfilled "holes"    
        //rules.add(new SingleLineRule("{", "}", comment));
        
        // Add generic whitespace rule.
        rules.add(new WhitespaceRule(new WhitespaceDetector()));

        // Add word rule for keywords, types, and constants.
        WordRule wordRule= new WordRule(new RuleWordDetector(), other);
        for (int i= 0; i < DROOLS_KEYWORDS.length; i++)
            wordRule.addWord(DROOLS_KEYWORDS[i], keyword);
        for (int i= 0; i < JAVA_KEYWORDS.length; i++)
            wordRule.addWord(JAVA_KEYWORDS[i], keyword);
        for (int i= 0; i < TYPES.length; i++)
            wordRule.addWord(TYPES[i], keyword);
        for (int i= 0; i < CONSTANTS.length; i++)
            wordRule.addWord(CONSTANTS[i], keyword);
        rules.add(wordRule);        
        
        IRule[] result= new IRule[rules.size()];
        rules.toArray(result);        
        
		setRules(result);
	}
  
}