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

package org.guvnor.tools.utils.webdav;

/**
 * Container for Guvnor properties.
 * @author jgraham
 */
public class ResourceProperties {
    private boolean isDirectory;
    private String creationDate;
    private String lastModifiedDate;
    private String revision;
    private String base;

    public String getBase() {
        return base;
    }
    public void setBase(String base) {
        this.base = base;
    }
    public boolean isDirectory() {
        return isDirectory;
    }
    public void setDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }
    public String getCreationDate() {
        return creationDate != null?creationDate:""; //$NON-NLS-1$
    }
    public void setCreationDate(String creationDate) {
        this.creationDate = StreamProcessingUtils.parseISODateFormat(creationDate);
    }
    public String getLastModifiedDate() {
        return lastModifiedDate != null?lastModifiedDate:""; //$NON-NLS-1$
    }
    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = StreamProcessingUtils.parseISODateFormat(lastModifiedDate);
    }
    public String getRevision() {
        return revision != null?revision:""; //$NON-NLS-1$
    }
    public void setRevision(String revision) {
        this.revision = revision;
    }
}
