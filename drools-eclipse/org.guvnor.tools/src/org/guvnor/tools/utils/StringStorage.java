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

package org.guvnor.tools.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Stores a string for in-memory editing.
 */
public class StringStorage extends PlatformObject implements IStorage {
    private String contents;
    private String name;

    public StringStorage(String contents, String name) {
        this.contents = contents;
        this.name = name;
    }

    public InputStream getContents() throws CoreException {
        return new ByteArrayInputStream(contents.getBytes());
    }

    public IPath getFullPath() {
        return null;
    }

    public String getName() {
        return name + " (Read only)"; //$NON-NLS-1$
    }

    public boolean isReadOnly() {
        return true;
    }
}
