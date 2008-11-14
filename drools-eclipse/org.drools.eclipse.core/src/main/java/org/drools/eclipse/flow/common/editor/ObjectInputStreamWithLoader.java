package org.drools.eclipse.flow.common.editor;
/*
 * Copyright 2005 JBoss Inc
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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;

/**
 * Implementation of an ObjectInputStream that has a custom classloader.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class ObjectInputStreamWithLoader extends ObjectInputStream {

    private ClassLoader loader;

    /**
     * Loader must be non-null;
     */
    public ObjectInputStreamWithLoader(InputStream in, ClassLoader loader)
            throws IOException, StreamCorruptedException {
        super(in);
        if (loader == null) {
            throw new IllegalArgumentException("Illegal null argument to ObjectInputStreamWithLoader");
        }
        this.loader = loader;
    }

    /**
     * Make a primitive array class
     */
    private Class primitiveType(char type) {
        switch (type) {
            case 'B': return byte.class;
            case 'C': return char.class;
            case 'D': return double.class;
            case 'F': return float.class;
            case 'I': return int.class;
            case 'J': return long.class;
            case 'S': return short.class;
            case 'Z': return boolean.class;
            default: return null;
        }
    }

    /**
     * Use the given ClassLoader rather than using the system class
     */
    protected Class resolveClass(ObjectStreamClass classDesc)
            throws IOException, ClassNotFoundException {
        String cname = classDesc.getName();
        if (cname.startsWith("[")) {
            // An array
            Class component = null;        // component class
            int dcount;         // dimension
            for (dcount=1; cname.charAt(dcount)=='['; dcount++);
            if (cname.charAt(dcount) == 'L') {
                String className = cname.substring(dcount+1, cname.length()-1);
                component = loader.loadClass(className);
            } else {
                if (cname.length() != dcount + 1) {
                    throw new ClassNotFoundException(cname);// malformed
                }
                component = primitiveType(cname.charAt(dcount));
            }
            int dim[] = new int[dcount];
            for (int i = 0; i < dcount; i++) {
                dim[i]=0;
            }
            return Array.newInstance(component, dim).getClass();
        }
        return loader.loadClass(cname);
    }
}

