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

public abstract class EnumerationConverter extends EnumerationFilter {

    protected Enumeration sourceEnum;

    public EnumerationConverter(Enumeration sourceEnum) {
        super();
        this.sourceEnum = sourceEnum;
    }

    /**
     * @see #hasMoreElements()
     */
    public boolean hasMoreElements() {
        return sourceEnum.hasMoreElements();
    }

    /**
     * @see #nextElement()
     * Subclasses should override ths method to convert the
     * source enum objects to the new types.
     */
    public abstract Object nextElement();
}
