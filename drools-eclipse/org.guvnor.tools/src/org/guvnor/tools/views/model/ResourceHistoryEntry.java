/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.guvnor.tools.views.model;

/**
 * A model for resource history data. 
 */
public class ResourceHistoryEntry {
    private String revision;
    private String date;
    private String author;
    private String comment;

    public ResourceHistoryEntry(String revision, String date,
                               String author, String comment) {
        this.revision = revision;
        this.date = date;
        this.author = author;
        this.comment = comment;
    }

    public String getRevision() {
        return revision != null?revision:""; //$NON-NLS-1$
    }
    public String getDate() {
        return date != null?date:""; //$NON-NLS-1$
    }
    public String getAuthor() {
        return author != null?author:""; //$NON-NLS-1$
    }
    public String getComment() {
        return comment != null?comment:""; //$NON-NLS-1$
    }
}
