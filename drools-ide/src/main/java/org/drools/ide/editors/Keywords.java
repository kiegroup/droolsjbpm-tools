package org.drools.ide.editors;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Keyword reading utility.
 * This will also slice up the keywords into the appropriate place where they should be shown in the editor.
 * TODO: hook in with the drools compiler, so the same keywords are used.
 * 
 * @author Michael Neale
 */
public class Keywords {

    private Properties keywords;
    
    private String[] all;
    private static Keywords instance;
    
    public static Keywords getInstance() {
        if (instance == null) {
            instance = new Keywords();
        }
        return instance;
    }
    
    
    /** 
     * @return A flat list of ALL keywords.
     */
    public String[] getAll() {
        if (all == null) {
            all = new String[keywords.values().size()];
            keywords.values().toArray(all);
            Arrays.sort(all);
            return all;
        }             
        return all;
    }
    
    
    /**
     * Returns a keyword from the configured RULE ASSEMBLY grammar.
     */
    public String lookup(String key) {
        return keywords.getProperty(key);
    }
    
    private Keywords() {
        keywords = new Properties();
        InputStream stream = this.getClass().getResourceAsStream("keywords.properties");
        try {
            keywords.load(stream);
            
        }
        catch ( IOException e ) {
            throw new IllegalArgumentException("Could not load keywords for editor.");
        }
        finally {
            try {
                stream.close();
            }
            catch ( IOException e ) {
                throw new IllegalStateException("Error closing stream.");
            }
        }
        
    }
    
    
}
