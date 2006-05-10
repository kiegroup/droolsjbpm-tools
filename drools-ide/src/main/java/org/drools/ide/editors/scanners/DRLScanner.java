package org.drools.ide.editors.scanners;

import java.util.ArrayList;
import java.util.List;

import org.drools.ide.editors.ColorManager;
import org.drools.ide.editors.Keywords;
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
    
	private static Color KEYWORD_COLOUR = ColorManager.getInstance().getColor(ColorManager.KEYWORD);
	private static Color COMMENT_COLOR = ColorManager.getInstance().getColor(ColorManager.SINGLE_LINE_COMMENT);
    private static Color STRING_COLOR = ColorManager.getInstance().getColor(ColorManager.STRING);
    private static Color DEFAULT = ColorManager.getInstance().getColor(ColorManager.DEFAULT);
    
    private static String[] fgKeywords= Keywords.getInstance().getAll();

    private static String[] fgTypes= {  }; 

    private static String[] fgConstants= { "false", "true" }; 
    

	public DRLScanner() {
        
		IToken keyword = new Token(new TextAttribute(KEYWORD_COLOUR, null, SWT.BOLD));
		IToken comment= new Token(new TextAttribute(COMMENT_COLOR));
		IToken string = new Token(new TextAttribute(STRING_COLOR));
		IToken other = new Token(new TextAttribute(DEFAULT));
        
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
        for (int i= 0; i < fgKeywords.length; i++)
            wordRule.addWord(fgKeywords[i], keyword);
        for (int i= 0; i < fgTypes.length; i++)
            wordRule.addWord(fgTypes[i], keyword);
        for (int i= 0; i < fgConstants.length; i++)
            wordRule.addWord(fgConstants[i], keyword);
        rules.add(wordRule);        
        
        IRule[] result= new IRule[rules.size()];
        rules.toArray(result);        
        
		setRules(result);
        

	}
    
  
}
