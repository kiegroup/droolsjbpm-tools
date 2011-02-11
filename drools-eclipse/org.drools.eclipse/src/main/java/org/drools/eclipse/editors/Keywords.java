/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
