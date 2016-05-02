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

package org.kie.eclipse.runtime;

import java.io.File;

import org.eclipse.core.runtime.Path;

public class DefaultRuntimeRecognizer extends AbstractRuntimeRecognizer {
	
    public String[] recognizeJars(String path) {
        clearFiles();
        if (path != null) {
            File file = (new Path(path)).toFile();
            addJarsRecursive(file);
        }
        return getFiles();
    }

    protected void addJarsRecursive(File file) {
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory() &&
            		("lib".equals(files[i].getName()) || "runtime".equals(files[i].getName()))) {
                addJarsRecursive(files[i]);
            } else if (files[i].getName().endsWith(".jar")) {
            	addFile(files[i]);
            }
        }
    }
}
