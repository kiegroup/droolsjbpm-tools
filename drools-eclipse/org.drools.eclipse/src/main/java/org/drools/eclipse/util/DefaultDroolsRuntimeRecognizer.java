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

package org.drools.eclipse.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Path;

public class DefaultDroolsRuntimeRecognizer implements DroolsRuntimeRecognizer {

    public String[] recognizeJars(String path) {
        List<String> list = new ArrayList<String>();
        if (path != null) {
            File file = (new Path(path)).toFile();
            addJarNames(file, list);
        }
        return list.toArray(new String[list.size()]);
    }

    private void addJarNames(File file, List<String> list) {
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory() && "lib".equals(files[i].getName())) {
                addJarNames(files[i], list);
            } else if (files[i].getPath().endsWith(".jar")) {
                list.add(files[i].getAbsolutePath());
            }
        }
    }
}
