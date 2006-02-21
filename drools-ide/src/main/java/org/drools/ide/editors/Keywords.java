package org.drools.ide.editors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This provides a list of keywords for syntax highlighting.
 * Uses a pseudo properties file format.
 * @author Michael Neale
 */
public class Keywords {

    private String[] all;
    private static Keywords instance;
    
    public static Keywords getInstance() {
        if (instance == null) {
            instance = new Keywords();
        }
        return instance;
    }
    
    
    public String[] getAll() {
        return all;
    }
    
    
    private Keywords() {
        InputStream stream = this.getClass().getResourceAsStream("keywords.properties");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            
            List list = new ArrayList();
            
            String line = null;
            while ((line = reader.readLine()) != null) {
               if (!line.startsWith( "#" ))  list.add( line ); 
            }
            
            all = new String[list.size()];
            list.toArray( all );
                     
            
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
