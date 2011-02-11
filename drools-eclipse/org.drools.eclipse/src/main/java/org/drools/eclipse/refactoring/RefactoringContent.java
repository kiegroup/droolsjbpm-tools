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

package org.drools.eclipse.refactoring;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;

/**
 * Store the files content changes when multiple files are refactored at same time.
 */
public class RefactoringContent {

    private Integer processorHashcode;
    private Map<IFile, String> fileContents;

    public RefactoringContent() {
        this.processorHashcode = -1;
        this.fileContents = new HashMap<IFile, String>();
    }

    public void setProcessorHashcode(Integer processorHashcode) {
        this.processorHashcode = processorHashcode;
    }

    public Integer getProcessorHashcode() {
        return processorHashcode;
    }

    public String getIFileContent(IFile file) {
        return this.fileContents.get(file);
    }

    public void updateContent(IFile file, String content) {
        this.fileContents.put(file, content);
    }

    public void clear() {
        this.fileContents.clear();
    }

}
