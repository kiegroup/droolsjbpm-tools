package org.drools.eclipse.editors.completion;

public class CompletionUtil {
	
	private CompletionUtil() {
	}

    /** Looks behind, gets stuff after the white space. Basically ripping out the last word.*/
    public static String stripLastWord(String prefix) {
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
    
    public static String stripWhiteSpace(String prefix) {
    	if ("".equals(prefix)) { 
    		return prefix;
    	}
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
