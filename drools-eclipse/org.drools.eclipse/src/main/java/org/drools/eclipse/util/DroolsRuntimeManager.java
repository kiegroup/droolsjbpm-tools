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
import java.io.ByteArrayInputStream;
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
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeManager;

public class DroolsRuntimeManager implements IRuntimeManager {

    private static final String DROOLS_RUNTIME_RECOGNIZER = "org.drools.eclipse.runtimeRecognizer";
	// This is the "hidden" Eclipse Workspace Project name that will hold a copy
	// of the Drools Runtime that is packaged with this plugin; currently just
	// a simple project with a "lib" folder containing all of the required
	// Drools Runtime jars. If the user has not yet created a default Runtime,
    // this  project will be created, populated and used as the default.
    private static final String DROOLS_BUNDLE_RUNTIME_LOCATION = ".drools.runtime";
    
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
    
    public String[] getAllRuntimeNames() {
    	return DroolsRuntime.getAllNames();
    }
    
    public String[] getAllRuntimeIds() {
    	return DroolsRuntime.getAllIds();
    }
    
    public boolean isMavenized(String runtimeId) {
    	return DroolsRuntime.ID_DROOLS_6.equals(runtimeId);
    }
    
    public void addRuntime(DroolsRuntime rt) {
    	ArrayList<DroolsRuntime> list = new ArrayList<DroolsRuntime>();
    	list.addAll(Arrays.asList(getConfiguredRuntimes()));
    	list.add(rt);
    	setDroolsRuntimesInternal(list.toArray(new DroolsRuntime[list.size()]));
    	fireRuntimeAdded(rt);
    }

    
    public void removeRuntime(DroolsRuntime rt) {
    	ArrayList<DroolsRuntime> list = new ArrayList<DroolsRuntime>();
    	list.addAll(Arrays.asList(getConfiguredRuntimes()));
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
    		it.next().runtimesChanged(getConfiguredRuntimes());
    	}
    }

    /**
     * Returns the version number of the Drools Runtime that is packaged with
     * this plugin, i.e. the version number of the org.drools.eclipse plugin.
     * 
     * @return version number of default Drools Runtime.
     */
    public String getBundleRuntimeVersion() {
    	String version = Platform.getBundle("org.drools.eclipse").getVersion().toString();
    	String a[] = version.split("\\.");
    	if (a.length>3) {
    		return a[0] + "." + a[1] + "." + a[2];
    	}
    	return version;
    }
    
    public String getBundleRuntimeName() {
    	return "Drools " + getBundleRuntimeVersion() + " Runtime";
    }
    
    public String getBundleRuntimeLocation() {
    	return DROOLS_BUNDLE_RUNTIME_LOCATION;
    }
    
    /**
     * @deprecated this method name conflicts with the notion of a "default runtime"
     * since it is not creating a Default Runtime, but rather a Drools Runtime based
     * on the jars contained in the org.drools.eclipse bundle (plugin).
     * Use createBundleRuntime() instead
     * @param location
     */
    public void createDefaultRuntime(String location) {
    	createBundleRuntime(location);
    }
    
    public DroolsRuntime createBundleRuntime(String location) {
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
        String separator = "";
        if (!location.endsWith(File.separator)) {
            separator = File.separator;
        }
        List<String> jarsCreated = new ArrayList<String>();
        for (String jar: jars) {
            try {
                File jarFile = new File(jar);
                FileChannel inChannel = new FileInputStream(jarFile).getChannel();
                String createdJar = location + separator + jarFile.getName();
                FileChannel outChannel = new FileOutputStream(new File(createdJar)).getChannel();
                try {
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                    jarsCreated.add(createdJar);
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

    	DroolsRuntime runtime = new DroolsRuntime();
        runtime.setPath(location);
        runtime.setName(getBundleRuntimeName());
        runtime.setJars(jarsCreated.toArray(new String[jarsCreated.size()]));
        
        return runtime;
    }
    
    public IRuntime getEffectiveRuntime(IRuntime selectedRuntime, boolean useDefault) {
    	
    	// The bundle runtime project may have been deleted; if so, we need to rebuild it
    	boolean rebuildBundleRuntimeProject = false;
    	String bundleRuntimeLocation = getBundleRuntimeLocation();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        if (selectedRuntime.getPath() != null) {
        	// If this really is the Bundle Runtime project,
        	// remove the "lib" segment if there is one
        	IPath runtimeRootPath = new Path(selectedRuntime.getPath());
        	if ("lib".equals(runtimeRootPath.lastSegment()))
        		runtimeRootPath = runtimeRootPath.removeLastSegments(1);
        	if (bundleRuntimeLocation.equals(runtimeRootPath.lastSegment()))
        		// then remove the Bundle Runtime project name. 
        		runtimeRootPath = runtimeRootPath.removeLastSegments(1);

			// if the absolute path matches the Workspace Root path, then this
			// is the Bundle Runtime Project
        	IPath rootPath = workspace.getRoot().getLocation();
        	if (rootPath.equals(runtimeRootPath)) {
        		IProject project = workspace.getRoot().getProject(bundleRuntimeLocation);
        		// If the project exists and is not open, try to open it
        		if (!project.isOpen()) {
	                try {
						project.open(IResource.BACKGROUND_REFRESH,null);
					} catch (CoreException ex) {
			            DroolsEclipsePlugin.log(ex);
					}
        		}
				// If the project does not exist, we need to create it. This is
				// an indication that the project was previously deleted by the
				// user.
            	if (!project.exists()) {
            		rebuildBundleRuntimeProject = true;
            	}
            	else {
					// Check if the "lib" folder was deleted, or if the jars
					// were removed
        			int jarCount = 0;
            		IFolder lib = project.getFolder("lib");
            		if (!lib.exists()) {
            			try {
							// The "lib" folder is gone, might as well rebuild
							// the entire project
							project.delete(true, null);
		            		rebuildBundleRuntimeProject = true;
						} catch (CoreException ex) {
				            DroolsEclipsePlugin.log(ex);
						}
            		}
            		else {
	        			try {
							// Count the number of jars in the "lib" folder.
							// We don't actually know how many jars SHOULD be in
							// there, but if there are none, this is a clear
							// indication that the "lib" folder was cleaned out.
							for (IResource f : lib.members()) {
								if ("jar".equals(f.getFileExtension())) {
									++jarCount;
								}
							}
						} catch (CoreException ex) {
				            DroolsEclipsePlugin.log(ex);
						}
					}
        			if (jarCount==0) {
                		rebuildBundleRuntimeProject = true;
        			}
            	}
        	}
        }
        
        List<IRuntime> droolsRuntimes = new ArrayList<IRuntime>();
    	for (DroolsRuntime rt : getConfiguredRuntimes())
    		droolsRuntimes.add(rt);
    	
        if (selectedRuntime.getPath() == null || rebuildBundleRuntimeProject || droolsRuntimes.size()==0) {
			// This is the Bundle Runtime and it doesn't exist yet, or needs
			// to be rebuilt. Create a "hidden" workspace project.
			// But first, remove the old DroolsRuntime entry from our list.
        	droolsRuntimes.remove(selectedRuntime);
        	
        	final IProject project = workspace.getRoot().getProject(bundleRuntimeLocation);
        	// The "lib" folder will contain the runtime jars.
        	final IFolder lib = project.getFolder("lib");
        	if (!project.exists() || !lib.exists()) {
                final IProjectDescription description = workspace
                        .newProjectDescription(project.getName());
                description.setLocation(null);
                try {
					project.create(description, null);
	                project.open(IResource.BACKGROUND_REFRESH,null);
	                lib.create(true, true, null);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	
        	// and create a new entry
            String location = lib.getLocation().removeTrailingSeparator().toString();
            selectedRuntime = createBundleRuntime(location);
            if (droolsRuntimes.size()==0)
            	useDefault = true;
            
            selectedRuntime.setDefault(useDefault);
        	droolsRuntimes.add(selectedRuntime);
        	// finally rebuild the DroolsRuntime definitions in User Preferences
        	setDroolsRuntimes(droolsRuntimes.toArray(new DroolsRuntime[droolsRuntimes.size()]));
        }
        if (useDefault) {
            return null;
        }
        return selectedRuntime;
    }

    private String getDroolsLocation() {
        try {
            return FileLocator.toFileURL(Platform.getBundle("org.drools.eclipse")
                .getEntry("/")).getFile().toString();
        } catch (IOException e) {
            DroolsEclipsePlugin.log(e);
        }
        return null;
    }

    private String generateString(DroolsRuntime[] droolsRuntimes) {
        String result = "";
        for (DroolsRuntime runtime: droolsRuntimes) {
            result += runtime.getName() + "#" + runtime.getPath() + "#" + runtime.isDefault() + "# ";
            // migrate to new runtime string format by adding the runtime ID
            result += runtime.getId() + "#";
            if (runtime.getJars() != null) {
                for (String jar: runtime.getJars()) {
                    result += jar + ";";
                }
            }
            result += "###";
        }
        return result;
    }

    private DroolsRuntime[] generateRuntimes(String s) {
        List<DroolsRuntime> result = new ArrayList<DroolsRuntime>();
        if (s != null && !"".equals(s)) {
            String[] runtimeStrings = s.split("###");
            for (String runtimeString: runtimeStrings) {
                String[] properties = runtimeString.split("#");
                DroolsRuntime runtime = new DroolsRuntime();
                runtime.setName(properties[0]);
                String location = properties[1];
                File file = new File(location);
                // if the path no longer exists remove it from our list
                if (file.exists()) {
	                runtime.setPath(location);
	                runtime.setDefault("true".equals(properties[2]));
	                if (properties.length > 3) {
	                    List<String> list = new ArrayList<String>();
	                    String[] jars = properties[3].split(";");
	                    // migrate to new runtime string format
	                    int index = 0;
	                    if (!jars[index].endsWith(".jar")) {
	                    	runtime.setId(jars[index++]);
	                    }
	                    while (index<jars.length) {
	                    	String jar = jars[index++];
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
        }
        return result.toArray(new DroolsRuntime[result.size()]);
    }

    public DroolsRuntime[] getConfiguredRuntimes() {
        String runtimesString = DroolsEclipsePlugin.getDefault().getPreferenceStore()
            .getString(IDroolsConstants.DROOLS_RUNTIMES);
        if (runtimesString != null) {
            return generateRuntimes(runtimesString);
        }
        return new DroolsRuntime[0];
    }

    public void setDroolsRuntimes(DroolsRuntime[] runtimes) {
    	setDroolsRuntimesInternal(runtimes);
    	getDefault().fireRuntimesChanged();
    }

    private void setDroolsRuntimesInternal(DroolsRuntime[] runtimes) {
        DroolsEclipsePlugin.getDefault().getPreferenceStore().setValue(IDroolsConstants.DROOLS_RUNTIMES,
        generateString(runtimes));
    }

    
    public DroolsRuntime getDroolsRuntime(String name) {
        DroolsRuntime[] runtimes = getConfiguredRuntimes();
        for (DroolsRuntime runtime: runtimes) {
            if (runtime.getName().equals(name)) {
                return runtime;
            }
        }
        return null;
    }

    public DroolsRuntime getDefaultRuntime() {
        DroolsRuntime[] runtimes = getConfiguredRuntimes();
        for (DroolsRuntime runtime: runtimes) {
            if (runtime.isDefault()) {
                return runtime;
            }
        }
        return null;
    }
    
    public void setRuntime(IRuntime runtime, IProject project, IProgressMonitor monitor) throws CoreException {
        if (runtime != null) {
            IFile file = project.getFile(".settings/.drools.runtime");
            String runtimeString = "<runtime>" + runtime.getName() + "</runtime>";
            if (!file.exists()) {
                IFolder folder = project.getProject().getFolder(".settings");
                if (!folder.exists()) {
                    folder.create(true, true, null);
                }
                file.create(new ByteArrayInputStream(runtimeString.getBytes()), true, null);
            } else {
                file.setContents(new ByteArrayInputStream(runtimeString.getBytes()), true, false, null);
            }
        }
    }

    public IRuntime getRuntime(IProject project) {
        try {
            IFile file = project.getFile(".settings/.drools.runtime");
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()));
                String location = reader.readLine();
                if (location.startsWith("<runtime>") && location.endsWith("</runtime>")) {
                    String runtimeName = location.substring(9, location.length() - 10);
                    return getDroolsRuntime(runtimeName);
                }
            }
        } catch (Exception e) {
            DroolsEclipsePlugin.log(e);
        }
        return getDefaultRuntime();
    }
    
    public String[] getDroolsRuntimeJars(IProject project) {
        DroolsRuntime runtime = (DroolsRuntime) getRuntime(project);
        if (runtime == null) {
            return null;
        }
        if (runtime.getJars() == null || runtime.getJars().length == 0) {
            recognizeJars(runtime);
        }
        return runtime.getJars();
    }
    
    public void recognizeJars(DroolsRuntime runtime) {
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
