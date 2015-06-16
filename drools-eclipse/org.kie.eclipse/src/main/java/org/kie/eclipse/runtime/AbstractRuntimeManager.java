package org.kie.eclipse.runtime;

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
import org.eclipse.jface.preference.IPreferenceStore;

public abstract class AbstractRuntimeManager implements IRuntimeManager {

	private static final String KIE_RUNTIME_RECOGNIZER = "org.kie.eclipse.runtimeRecognizer";

    private ArrayList<IRuntimeManagerListener> listeners = new ArrayList<IRuntimeManagerListener>();
    /**
     * Add a listener to this model
     */
    public void addListener(IRuntimeManagerListener listener) {
    	listeners.add(listener);
    }
    
    /**
     * Remove a listener from this model
     */
    public void removeListener(IRuntimeManagerListener listener) {
    	listeners.remove(listener);
    }
    
    public void addRuntime(IRuntime rt) {
    	ArrayList<IRuntime> list = new ArrayList<IRuntime>();
    	list.addAll(Arrays.asList(getConfiguredRuntimes()));
    	list.add(rt);
    	setRuntimesInternal(list.toArray(new IRuntime[list.size()]));
    	fireRuntimeAdded(rt);
    }

    
    public void removeRuntime(IRuntime rt) {
    	ArrayList<IRuntime> list = new ArrayList<IRuntime>();
    	list.addAll(Arrays.asList(getConfiguredRuntimes()));
    	list.remove(rt);
    	setRuntimesInternal(list.toArray(new IRuntime[list.size()]));
    	fireRuntimeRemoved(rt);
    }

    private void fireRuntimeAdded(IRuntime rt) {
    	Iterator<IRuntimeManagerListener> it = listeners.iterator();
    	while(it.hasNext()) {
    		it.next().runtimeAdded(rt);
    	}
    }

    private void fireRuntimeRemoved(IRuntime rt) {
    	Iterator<IRuntimeManagerListener> it = listeners.iterator();
    	while(it.hasNext()) {
    		it.next().runtimeRemoved(rt);
    	}
    }

    private void fireRuntimesChanged() {
    	Iterator<IRuntimeManagerListener> it = listeners.iterator();
    	while(it.hasNext()) {
    		it.next().runtimesChanged(getConfiguredRuntimes());
    	}
    }

    /**
     * Returns the version number of the Runtime that is packaged with this plugin,
     * for example the version number of the org.drools.eclipse plugin.
     * 
     * @return version number of bundle Runtime.
     */
    public String getBundleRuntimeVersion() {
    	String version = Platform.getBundle( getBundleSymbolicName() ).getVersion().toString();
    	String a[] = version.split("\\.");
    	if (a.length>3) {
    		return a[0] + "." + a[1] + "." + a[2];
    	}
    	return version;
    }
    
    public String getBundleRuntimeName() {
    	return "Runtime for version " + getBundleRuntimeVersion();
    }
    
    public IRuntime createBundleRuntime(String location) {
        List<String> jars = new ArrayList<String>();
        // get all drools jars from drools eclipse plugin
        String s = getBundleLocation();
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
            logException(ex);
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
                logException(t);
            }
        }

    	IRuntime runtime = createNewRuntime();
        runtime.setName(getBundleRuntimeName());
        runtime.setVersion(getBundleRuntimeVersion());
        runtime.setPath(location);
        runtime.setJars(jarsCreated.toArray(new String[jarsCreated.size()]));
        
        return runtime;
    }

    private String getBundleLocation() {
        try {
            return FileLocator.toFileURL(Platform.getBundle(getBundleSymbolicName())
                .getEntry("/")).getFile().toString();
        } catch (IOException e) {
            logException(e);
        }
        return null;
    }

    public IRuntime getEffectiveRuntime(IRuntime selectedRuntime, boolean useDefault) {
    	
    	// The bundle runtime project may have been deleted; if so, we need to rebuild it
    	boolean rebuildBundleRuntimeProject = false;
    	String bundleRuntimeLocation = getBundleRuntimeLocation() + "_" + getBundleRuntimeVersion();
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
			            logException(ex);
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
				            logException(ex);
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
				            logException(ex);
						}
					}
        			if (jarCount==0) {
                		rebuildBundleRuntimeProject = true;
        			}
            	}
        	}
        }
        
        List<IRuntime> runtimes = new ArrayList<IRuntime>();
    	for (IRuntime rt : getConfiguredRuntimes())
    		runtimes.add(rt);
    	
        if (selectedRuntime.getPath() == null || rebuildBundleRuntimeProject || runtimes.size()==0) {
			// This is the Bundle Runtime and it doesn't exist yet, or needs
			// to be rebuilt. Create a "hidden" workspace project.
			// But first, remove the old IRuntime entry from our list.
        	for (IRuntime rt : runtimes) {
        		if (rt.getName().equals(selectedRuntime.getName())) {
                	runtimes.remove(rt);
                	break;
        		}
        	}
        	
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
            if (runtimes.size()==0)
            	useDefault = true;
            
            selectedRuntime.setDefault(useDefault);
        	runtimes.add(selectedRuntime);
        	// finally rebuild the IRuntime definitions in User Preferences
        	setRuntimes(runtimes.toArray(new IRuntime[runtimes.size()]));
        }
        if (useDefault) {
            return null;
        }
        return selectedRuntime;
    }

    private String createStringFromRuntimes(IRuntime[] runtimes) {
        String result = "";
        for (IRuntime runtime: runtimes) {
            result += runtime.getName() + "#" + runtime.getPath() + "#" + runtime.isDefault() + "# ";
            // migrate to new runtime string format by adding runtime product and version info
            result += runtime.getProduct() + "#";
            result += runtime.getVersion() + "#";
            if (runtime.getJars() != null) {
                for (String jar: runtime.getJars()) {
                    result += jar + ";";
                }
            }
            result += "###";
        }
        return result;
    }

    private IRuntime[] createRuntimesFromString(String s) {
        List<IRuntime> result = new ArrayList<IRuntime>();
        if (s != null && !"".equals(s)) {
            String[] runtimeStrings = s.split("###");
            for (String runtimeString: runtimeStrings) {
                String[] properties = runtimeString.split("#");
                IRuntime runtime = createNewRuntime();
                runtime.setName(properties[0]);
                String location = properties[1];
                File file = new File(location);
                // if the path no longer exists remove it from our list
                if (file.exists()) {
	                runtime.setPath(location);
	                runtime.setDefault("true".equals(properties[2]));
	                if (properties.length > 3) {
	                	int index = 3;
	                	if (properties.length>5) {
		                    // migrate to new runtime string format
	                    	runtime.setProduct(properties[index++]);
	                    	runtime.setVersion(properties[index++]);
	                	}
	                    List<String> list = new ArrayList<String>();
	                    String[] jars = properties[index].split(";");
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
        return result.toArray(new IRuntime[result.size()]);
    }

    public IRuntime[] getConfiguredRuntimes() {
        String runtimesString = getPreferenceStore().getString(getRuntimePreferenceKey());
        if (runtimesString != null) {
            return createRuntimesFromString(runtimesString);
        }
        return new IRuntime[0];
    }

    public void setRuntimes(IRuntime[] runtimes) {
    	setRuntimesInternal(runtimes);
    	fireRuntimesChanged();
    }

    private void setRuntimesInternal(IRuntime[] runtimes) {
        getPreferenceStore().setValue(getRuntimePreferenceKey(),createStringFromRuntimes(runtimes));
    }

    
    public IRuntime getRuntime(String name) {
        IRuntime[] runtimes = getConfiguredRuntimes();
        for (IRuntime runtime: runtimes) {
            if (runtime.getName().equals(name)) {
                return runtime;
            }
        }
        return null;
    }

    public IRuntime getDefaultRuntime() {
        IRuntime[] runtimes = getConfiguredRuntimes();
        for (IRuntime runtime: runtimes) {
            if (runtime.isDefault()) {
                return runtime;
            }
        }
        return null;
    }
    
    public void setRuntime(IRuntime runtime, IProject project, IProgressMonitor monitor) throws CoreException {
        if (runtime != null) {
            IFile file = project.getFile(".settings/" + getSettingsFilename());
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
            IFile file = project.getFile(".settings/" + getSettingsFilename());
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()));
                String location = reader.readLine();
                if (location.startsWith("<runtime>") && location.endsWith("</runtime>")) {
                    String runtimeName = location.substring(9, location.length() - 10);
                    return getRuntime(runtimeName);
                }
            }
        } catch (Exception e) {
            logException(e);
        }
        return getDefaultRuntime();
    }
    
    public String[] getRuntimeJars(IProject project) {
        IRuntime runtime = (IRuntime) getRuntime(project);
        if (runtime == null) {
            return null;
        }
        if (runtime.getJars() == null || runtime.getJars().length == 0) {
            recognizeJars(runtime);
        }
        return runtime.getJars();
    }
    
    public void recognizeJars(IRuntime runtime) {
        String path = runtime.getPath();
        if (path != null) {
            try {
                IConfigurationElement[] config = Platform.getExtensionRegistry()
                        .getConfigurationElementsFor(KIE_RUNTIME_RECOGNIZER);
                for (IConfigurationElement e : config) {
                    Object o = e.createExecutableExtension("class");
                    if (o instanceof IRuntimeRecognizer) {
                        String[] jars = ((IRuntimeRecognizer) o).recognizeJars(path);
                        if (jars != null && jars.length > 0) {
                            runtime.setJars(jars);
                            return;
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            runtime.setJars(new DefaultRuntimeRecognizer().recognizeJars(path));
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////
    // to be implemented
    ///////////////////////////////////////////////////////////////////////////////////////
    abstract public String getBundleRuntimeLocation();
    abstract public String getRuntimePreferenceKey();
    abstract public boolean isMavenized(IRuntime runtime);
    abstract public String getSettingsFilename();
    abstract public String getBundleSymbolicName();
    abstract public IRuntime createNewRuntime();
    abstract public void logException(Throwable t);
    abstract public IPreferenceStore getPreferenceStore();
}
