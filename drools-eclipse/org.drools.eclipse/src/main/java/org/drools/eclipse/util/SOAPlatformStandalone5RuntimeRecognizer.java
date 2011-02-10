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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class SOAPlatformStandalone5RuntimeRecognizer implements DroolsRuntimeRecognizer {

    public String[] recognizeJars(String path) {
        IPath jbossrulesesbPath = new Path(path).append("jboss-esb/server/default/deploy/jbrules.esb");
        File jbossrulesesb = jbossrulesesbPath.toFile();
        if (jbossrulesesb.isDirectory()) {
            List<String> list = new ArrayList<String>();
            // the SOA platform
            File[] files = jbossrulesesb.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    if (!name.endsWith(".jar")) {
                        return false;
                    }
                    if (name.startsWith("jbossesb")) {
                        return false;
                    }
                    return true;
                }

            });
            for (int i = 0; i < files.length; i++) {
                list.add(files[i].getAbsolutePath());
            }
            IPath jbossesbsarPath = new Path(path).append("jboss-esb/server/default/deployers/esb.deployer/lib");
            File jbossesbsar=jbossesbsarPath.toFile();
            if (jbossesbsar.isDirectory()) {
                files = jbossesbsar.listFiles(new FilenameFilter() {

                    public boolean accept(File dir, String name) {
                        if (!name.endsWith(".jar")) {
                            return false;
                        }
                        if (name.startsWith("mvel")) {
                            return true;
                        }
                        if (name.startsWith("xstream")) {
                            return true;
                        }
                        return false;
                    }

                });
                if (files == null || files.length == 0) {
                    // could not find MVEL, this is probably not a SOA-P v5 runtime
                    // but possibly a SOA-P v4 one
                    return null;
                }
                for (int i = 0; i < files.length; i++) {
                    list.add(files[i].getAbsolutePath());
                }
            }
            return list.toArray(new String[list.size()]);
        }
        return null;
    }

}
