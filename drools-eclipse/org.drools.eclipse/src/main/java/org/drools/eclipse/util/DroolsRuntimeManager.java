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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

public class DroolsRuntimeManager {

    private static final String DROOLS_RUNTIME_RECOGNIZER = "org.drools.eclipse.runtimeRecognizer";

    private static DroolsRuntimeManager manager;
    public static DroolsRuntimeManager getDefault() {
    	if( manager == null )
    		manager = new DroolsRuntimeManager();
    	return manager;
    }
    
    private ArrayList<IDroolsRuntimeManagerListener> listeners = new ArrayList<IDroolsRuntimeManagerListener>();
    /**
     * Add a listener to this model
     */
    public void addListener(IDroolsRuntimeManagerListener listener) {
    	listeners.add(listener);
    }
    
    /**
     * Remove a listener from this model
     */
    public void removeListener(IDroolsRuntimeManagerListener listener) {
    	listeners.remove(listener);
    }
    
    
    public void addRuntime(DroolsRuntime rt) {
    	ArrayList<DroolsRuntime> list = new ArrayList<DroolsRuntime>();
    	list.addAll(Arrays.asList(getDroolsRuntimes()));
    	list.add(rt);
    	setDroolsRuntimesInternal(list.toArray(new DroolsRuntime[list.size()]));
    	fireRuntimeAdded(rt);
    }

    
    public void removeRuntime(DroolsRuntime rt) {
    	ArrayList<DroolsRuntime> list = new ArrayList<DroolsRuntime>();
    	list.addAll(Arrays.asList(getDroolsRuntimes()));
    	list.remove(rt);
    	setDroolsRuntimesInternal(list.toArray(new DroolsRuntime[list.size()]));
    	fireRuntimeRemoved(rt);
    }

    private void fireRuntimeAdded(DroolsRuntime rt) {
    	Iterator<IDroolsRuntimeManagerListener> it = listeners.iterator();
    	while(it.hasNext()) {
    		it.next().runtimeAdded(rt);
    	}
    }

    private void fireRuntimeRemoved(DroolsRuntime rt) {
    	Iterator<IDroolsRuntimeManagerListener> it = listeners.iterator();
    	while(it.hasNext()) {
    		it.next().runtimeRemoved(rt);
    	}
    }

    private void fireRuntimesChanged() {
    	Iterator<IDroolsRuntimeManagerListener> it = listeners.iterator();
    	while(it.hasNext()) {
    		it.next().runtimesChanged(getDroolsRuntimes());
    	}
    }

    public static void createDefaultRuntime(String location) {
        List<String> jars = new ArrayList<String>();
        // get all drools jars from drools eclipse plugin
        String s = getDroolsLocation();
        File file = (new Path(s)).toFile();
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory() && files[i].getName().equals("lib")) {
                File[] jarFiles = files[i].listFiles();
                for (int j = 0; j < jarFiles.length; j++) {
                    if (jarFiles[j].getPath().endsWith(".jar")) {
                        jars.add(jarFiles[j].getAbsolutePath());
                    }
                }
            }
        }

        // search for latest eclipse jdt jar
        try {
            jars.add(FileLocator.getBundleFile(Platform.getBundle("org.eclipse.jdt.core")).getAbsolutePath());
        } catch (IOException ex) {
            DroolsEclipsePlugin.log(ex);
        }


        // copy jars to specified location
        if (!location.endsWith(File.separator)) {
            location = location + File.separator;
        }
        for (String jar: jars) {
            try {
                File jarFile = new File(jar);
                FileChannel inChannel = new FileInputStream(jarFile).getChannel();
                FileChannel outChannel = new FileOutputStream(new File(
                    location + jarFile.getName())).getChannel();
                try {
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                }
                catch (IOException e) {
                    throw e;
                }
                finally {
                    if (inChannel != null) inChannel.close();
                    if (outChannel != null) outChannel.close();
                }
            } catch (Throwable t) {
                DroolsEclipsePlugin.log(t);
            }
        }
    }

    private static String getDroolsLocation() {
        try {
            return FileLocator.toFileURL(Platform.getBundle("org.drools.eclipse")
                .getEntry("/")).getFile().toString();
        } catch (IOException e) {
            DroolsEclipsePlugin.log(e);
        }
        return null;
    }

    private static String generateString(DroolsRuntime[] droolsRuntimes) {
        String result = "";
        for (DroolsRuntime runtime: droolsRuntimes) {
            result += runtime.getName() + "#" + runtime.getPath() + "#" + runtime.isDefault() + "# ";
            if (runtime.getJars() != null) {
                for (String jar: runtime.getJars()) {
                    result += jar + ";";
                }
            }
            result += "###";
        }
        return result;
    }

    private static DroolsRuntime[] generateRuntimes(String s) {
        List<DroolsRuntime> result = new ArrayList<DroolsRuntime>();
        if (s != null && !"".equals(s)) {
            String[] runtimeStrings = s.split("###");
            for (String runtimeString: runtimeStrings) {
                String[] properties = runtimeString.split("#");
                DroolsRuntime runtime = new DroolsRuntime();
                runtime.setName(properties[0]);
                runtime.setPath(properties[1]);
                runtime.setDefault("true".equals(properties[2]));
                if (properties.length > 3) {
                    List<String> list = new ArrayList<String>();
                    String[] jars = properties[3].split(";");
                    for (String jar: jars) {
                        jar = jar.trim();
                        if (jar.length() > 0) {
                            list.add(jar);
                        }
                    }
                    runtime.setJars(list.toArray(new String[list.size()]));
                }
                result.add(runtime);
            }
        }
        return result.toArray(new DroolsRuntime[result.size()]);
    }

    public static DroolsRuntime[] getDroolsRuntimes() {
        String runtimesString = DroolsEclipsePlugin.getDefault().getPreferenceStore()
            .getString(IDroolsConstants.DROOLS_RUNTIMES);
        if (runtimesString != null) {
            return generateRuntimes(runtimesString);
        }
        return new DroolsRuntime[0];
    }

    public static void setDroolsRuntimes(DroolsRuntime[] runtimes) {
    	setDroolsRuntimesInternal(runtimes);
    	getDefault().fireRuntimesChanged();
    }

    private static void setDroolsRuntimesInternal(DroolsRuntime[] runtimes) {
        DroolsEclipsePlugin.getDefault().getPreferenceStore().setValue(IDroolsConstants.DROOLS_RUNTIMES,
        DroolsRuntimeManager.generateString(runtimes));
    }

    
    public static DroolsRuntime getDroolsRuntime(String name) {
        DroolsRuntime[] runtimes = getDroolsRuntimes();
        for (DroolsRuntime runtime: runtimes) {
            if (runtime.getName().equals(name)) {
                return runtime;
            }
        }
        return null;
    }

    public static DroolsRuntime getDefaultDroolsRuntime() {
        DroolsRuntime[] runtimes = getDroolsRuntimes();
        for (DroolsRuntime runtime: runtimes) {
            if (runtime.isDefault()) {
                return runtime;
            }
        }
        return null;
    }

    public static String getDroolsRuntime(IProject project) {
        try {
            IFile file = project.getFile(".settings/.drools.runtime");
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()));
                String location = reader.readLine();
                if (location.startsWith("<runtime>") && location.endsWith("</runtime>")) {
                    return location.substring(9, location.length() - 10);
                }
            }
        } catch (Exception e) {
            DroolsEclipsePlugin.log(e);
        }
        return null;
    }
    
    public static String[] getDroolsRuntimeJars(IProject project) {
        String runtimeName = getDroolsRuntime(project);
        DroolsRuntime runtime = null;
        if (runtimeName == null) {
            runtime = getDefaultDroolsRuntime();
        } else {
            runtime = getDroolsRuntime(runtimeName);
        }
        if (runtime == null) {
            return null;
        }
        if (runtime.getJars() == null || runtime.getJars().length == 0) {
            recognizeJars(runtime);
        }
        return runtime.getJars();
    }
    
    public static void recognizeJars(DroolsRuntime runtime) {
        String path = runtime.getPath();
        if (path != null) {
            try {
                IConfigurationElement[] config = Platform.getExtensionRegistry()
                        .getConfigurationElementsFor(DROOLS_RUNTIME_RECOGNIZER);
                for (IConfigurationElement e : config) {
                    Object o = e.createExecutableExtension("class");
                    if (o instanceof DroolsRuntimeRecognizer) {
                        String[] jars = ((DroolsRuntimeRecognizer) o).recognizeJars(path);
                        if (jars != null && jars.length > 0) {
                            runtime.setJars(jars);
                            return;
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            runtime.setJars(new DefaultDroolsRuntimeRecognizer().recognizeJars(path));
        }
    }

}
