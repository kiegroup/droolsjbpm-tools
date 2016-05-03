/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.kie.eclipse.runtime.AbstractRuntime.Version;
import org.kie.eclipse.Activator;

public abstract class AbstractRuntimeManager implements IRuntimeManager {

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

    public IRuntime getEffectiveRuntime(IRuntime selectedRuntime, boolean useDefault) {
        if (useDefault) {
            return getDefaultRuntime();
        }
        return selectedRuntime;
    }

    protected String createStringFromRuntimes(IRuntime[] runtimes) {
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

    protected IRuntime[] createRuntimesFromString(String s) {
        List<IRuntime> result = new ArrayList<IRuntime>();
        if (s != null && !"".equals(s)) {
            String[] runtimeStrings = s.split("###");
            for (String runtimeString: runtimeStrings) {
                String[] properties = runtimeString.split("#");
                if (properties.length<2)
                	continue;
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
	                    index = 0;
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
                Collections.sort(result);
            }
        }
        return result.toArray(new IRuntime[result.size()]);
    }

    public IRuntime[] getConfiguredRuntimes() {
        String runtimesString = getPreferenceStore().getString(getRuntimePreferenceKey());
        if (runtimesString != null) {
        	IRuntime[] runtimes = createRuntimesFromString(runtimesString);
        	boolean rebuild = false;
        	for (IRuntime rt : runtimes) {
        		if (rt.getJars()==null || rt.getJars().length==0) {
        			rebuild = true;
        			recognizeJars(rt);
        		}
        	}
        	if (rebuild)
        		setRuntimes(runtimes);
        	return runtimes;
        }
        return new IRuntime[0];
    }

    public void setRuntimes(IRuntime[] runtimes) {
    	setRuntimesInternal(runtimes);
    	fireRuntimesChanged();
    }

    private void setRuntimesInternal(IRuntime[] runtimes) {
    	List<IRuntime> uniqueRuntimes = new ArrayList<IRuntime>();
    	for (IRuntime rt : runtimes) {
    		if (!uniqueRuntimes.contains(rt))
    			uniqueRuntimes.add(rt);
    	}
    	String s = createStringFromRuntimes(uniqueRuntimes.toArray(new IRuntime[uniqueRuntimes.size()]));
        getPreferenceStore().setValue(getRuntimePreferenceKey(),s);
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
    
	public int recognizeJars(IRuntime runtime) {
		String path = runtime.getPath();
		if (path != null) {
			// try all of the runtime recognizers defined in the Drools/jBPM
			// plugin.xml "runtimeRecognizer" extension point
			IRuntimeRecognizer recognizer = null;
			String[] jars = null;
			for (IRuntimeRecognizer r : getRuntimeRecognizers()) {
				jars = r.recognizeJars(path);
				if (jars != null && jars.length > 0) {
					recognizer = r;
					break;
				}
			}
			// fallback is to use a runtime created by the Drools/jBPM
			// runtime installer {@see DefaultRuntimeInstaller}
			if (recognizer == null) {
				recognizer = new DefaultRuntimeRecognizer();
				jars = recognizer.recognizeJars(path);
			}
			if (jars != null && jars.length > 0) {
				Version version = runtime.getVersion();
				String product = runtime.getProduct();
				List<Version> versions = recognizer.getProducts().get(product);
				if (versions!=null && versions.size()>0) {
					// we found at least one version of the runtime product
					if (!version.isValid() || !versions.contains(version)) {
						// runtime does not specify a version, so pick
						// the latest version of the product found in
						// the runtime location
						Collections.sort(versions, Collections.reverseOrder());
						if (versions.size()>0) {
							runtime.setVersion(versions.get(0).toString());
						}
					}
					runtime.setJars(jars);
					if (versions.size()>1) {
						Activator.logError(
								"The Runtime at '"+runtime.getPath()+"' "
								+"may have more than one version of installed jar files "
								+"for the "+runtime.getProduct()+" product.", new Throwable());
					}
				}
				return jars.length;
			}
		}
		return 0;
	}

    ///////////////////////////////////////////////////////////////////////////////////////
    // to be implemented
    ///////////////////////////////////////////////////////////////////////////////////////
    abstract public String getRuntimeWorkspaceLocation();
    abstract public String getRuntimePreferenceKey();
	abstract public IRuntimeRecognizer[] getRuntimeRecognizers();
    abstract public String getSettingsFilename();
    abstract public String getBundleSymbolicName();
    abstract public IRuntime createNewRuntime();
    abstract public void logException(Throwable t);
    abstract public IPreferenceStore getPreferenceStore();
}
