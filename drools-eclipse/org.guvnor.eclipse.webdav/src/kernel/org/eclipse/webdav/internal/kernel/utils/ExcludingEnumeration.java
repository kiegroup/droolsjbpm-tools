/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.eclipse.webdav.internal.kernel.utils;

import java.util.Enumeration;
import java.util.Vector;

public class ExcludingEnumeration extends EnumerationFilter {

    protected Enumeration e;
    protected Vector excludeList;
    protected Object next;

    public ExcludingEnumeration(Enumeration e, Vector excludeList) {
        super();
        this.e = e;
        this.excludeList = excludeList;
        getNextCandidate();
    }

    private void getNextCandidate() {
        while (e.hasMoreElements()) {
            Object candidate = e.nextElement();
            if (excludeList.indexOf(candidate) != -1) {
                next = candidate;
                return;
            }
        }
        next = null;
    }

    /**
     * @see #hasMoreElements()
     */
    public boolean hasMoreElements() {
        return (next != null);
    }

    /**
     * @see #nextElement()
     */
    public Object nextElement() {
        Object answer = next;
        getNextCandidate();
        return answer;
    }
}
