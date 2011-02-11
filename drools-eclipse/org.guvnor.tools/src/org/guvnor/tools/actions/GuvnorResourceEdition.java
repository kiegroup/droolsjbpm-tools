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

package org.guvnor.tools.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.graphics.Image;
import org.guvnor.tools.Activator;

/**
 * Represents compare input contents.
 *
 */
public class GuvnorResourceEdition implements IStreamContentAccessor, ITypedElement {

    private String contents;
    private String name;
    private String type;
    private String encoding;

    public GuvnorResourceEdition(String name, String type, String contents, String encoding) {
        this.name = name;
        this.type = type;
        this.contents = contents;
        this.encoding = encoding;
    }

    public InputStream getContents() throws CoreException {
        byte[] bytes = null;
        try {
            bytes = contents.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
            // Better than nothing?
            bytes = contents.getBytes();
        }
        return new ByteArrayInputStream(bytes);
    }

    public Image getImage() {
        return null;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
