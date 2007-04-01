package org.drools.eclipse.editors.completion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class ContextScanningTest extends TestCase {

    public void testCheckAfterToken() {
        
        
        
        assertTrue(checkContains("when", "rule something \nwhen \t blah"));
        assertTrue(checkContains("when", "rule something when nothing"));
        assertFalse(checkContains("when", "rule something whennothing"));
        assertTrue(checkContains("when", "rule something \twhen nothing"));
    }

    private boolean checkContains(String keyword,
                                 String chunk) {
        //Pattern p = Pattern.compile(".*(.)" + keyword + "(.).*", Pattern.DOTALL);
        Pattern p = Pattern.compile(".*rule.*\\W" + keyword + "\\W.*", Pattern.DOTALL);
        Matcher matcher = p.matcher(chunk);
       
        return matcher.matches();
    }
    
    



    
}
