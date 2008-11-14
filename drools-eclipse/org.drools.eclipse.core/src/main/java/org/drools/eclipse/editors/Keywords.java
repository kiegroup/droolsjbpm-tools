package org.drools.eclipse.editors;

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

    private String[] allDrools;
    private String[] allJava;
    private String[] allMvel;
    private static Keywords instance;

    public static Keywords getInstance() {
        if (instance == null) {
            instance = new Keywords();
        }
        return instance;
    }


    public String[] getAllDroolsKeywords() {
        return allDrools;
    }

    public String[] getAllJavaKeywords() {
        return allJava;
    }

    public String[] getAllMvelKeywords() {
        return allMvel;
    }


    private Keywords() {
    	allDrools = readKeywords("keywords.properties");
        allJava = readKeywords("java_keywords.properties");
        allMvel = readKeywords("mvel_keywords.properties");
    }

    private String[] readKeywords(String fileName) {
        InputStream stream = this.getClass().getResourceAsStream(fileName);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            List list = new ArrayList();

            String line = null;
            while ((line = reader.readLine()) != null) {
               if (!line.startsWith( "#" ))  list.add( line );
            }

            return (String[]) list.toArray( new String[list.size()] );
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
