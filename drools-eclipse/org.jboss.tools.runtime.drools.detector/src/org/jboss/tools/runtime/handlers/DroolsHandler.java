/*******************************************************************************

 * Copyright (c) 2010 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.runtime.handlers;

import java.io.IOException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.drools.eclipse.util.DroolsRuntime;
import org.drools.eclipse.util.DroolsRuntimeManager;
import org.eclipse.core.runtime.IProgressMonitor;
import
org.jboss.tools.runtime.core.model.AbstractRuntimeDetectorDelegate;
import org.jboss.tools.runtime.core.model.RuntimeDefinition;

public class DroolsHandler extends AbstractRuntimeDetectorDelegate {

	private static final String DROOLS_PREFIX = "Drools "; // NON-NLS-1$
	private static final String DROOLS_PREFIX_HYPHEN = DROOLS_PREFIX + " - "; // NON-NLS-1$
	
    private static final String DROOLS = "DROOLS"; // NON-NLS-1$
    private static final String SOA_P = "SOA-P"; //$NON-NLS-1$

    @Override
    public void initializeRuntimes(List<RuntimeDefinition> serverDefinitions) {
        DroolsRuntime[] existingRuntimes = DroolsRuntimeManager
                .getDroolsRuntimes();
        List<DroolsRuntime> droolsRuntimes = new ArrayList<DroolsRuntime>();
        if (existingRuntimes != null) {
            for (DroolsRuntime runtime : existingRuntimes) {
                droolsRuntimes.add(runtime);
            }
        }
        initializeInternal(serverDefinitions, droolsRuntimes);
        if (droolsRuntimes.size() > 0) {
            DroolsRuntime[] dra = droolsRuntimes.toArray(new DroolsRuntime[0]);
            DroolsRuntimeManager.setDroolsRuntimes(dra);
        }

    }

    private void initializeInternal(List<RuntimeDefinition> serverDefinitions,
            List<DroolsRuntime> droolsRuntimes) {
        for (RuntimeDefinition serverDefinition : serverDefinitions) {
            String type = serverDefinition.getType();
            if (serverDefinition.isEnabled() && !droolsExists(serverDefinition)) {
                if (DROOLS.equals(type)) {
                    File droolsRoot = serverDefinition.getLocation();
                    if (droolsRoot.isDirectory()) {
                        DroolsRuntime runtime = new DroolsRuntime();
                        boolean startsWithDrools = serverDefinition.getName().startsWith(DROOLS_PREFIX); 
                        String newName =   startsWithDrools ?  serverDefinition.getName() : 
                        	DROOLS_PREFIX + serverDefinition.getName();
                        runtime.setName(newName);

                        runtime.setPath(droolsRoot.getAbsolutePath());
                        DroolsRuntimeManager.recognizeJars(runtime);
                        runtime.setDefault(true);
                        droolsRuntimes.add(runtime);
                    }
                }
            }
            initializeInternal(serverDefinition.getIncludedRuntimeDefinitions(),
                    droolsRuntimes);
        }
    }

    /**
     * @param serverDefinition
     * @return
     */
    private static boolean droolsExists(RuntimeDefinition serverDefinition) {
    	return getRuntimeForLocation(serverDefinition.getLocation().getAbsolutePath()) != null;
    }
    
    private static DroolsRuntime getRuntimeForLocation(String loc) {
        DroolsRuntime[] droolsRuntimes = DroolsRuntimeManager.getDroolsRuntimes();
        for (DroolsRuntime dr : droolsRuntimes) {
            String location = dr.getPath();
            if (location != null && location.equals(loc)) {
                return dr;
            }
        }
        return null;
    }
    
    private boolean droolsRuntimeNameExists(String name) {
        DroolsRuntime[] droolsRuntimes = DroolsRuntimeManager.getDroolsRuntimes();
    	for( int i = 0; i < droolsRuntimes.length; i++ ) {
    		if( droolsRuntimes[i].getName().equals(name))
    			return true;
    	}
    	return false;
    }
    
    private String getDroolsRuntimeName(String prefix) {
    	if( !droolsRuntimeNameExists(prefix))
    		return prefix;
    	int count = 1;
    	while(droolsRuntimeNameExists(prefix + " (" + count + ")"))
    		count++;
    	return prefix + " (" + count + ")";
    }

    @Override
    public RuntimeDefinition getRuntimeDefinition(File root,
            IProgressMonitor monitor) {
        if (monitor.isCanceled() || root == null) {
            return null;
        }
        String[] files = root.list(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                if (name.startsWith("drools-api") && name.endsWith(".jar")) {
                    return true;
                }
                return false;
            }
        });
        if (files != null && files.length > 0) {
            String version = getImplementationVersion(root, files[0]);
            if (version != null) {
                version = version.substring(0, 3);
                String name = getDroolsRuntimeName(DROOLS_PREFIX + version);
                return new RuntimeDefinition(name, version, DROOLS,
                        root.getAbsoluteFile());
            }
        }
        return null;
    }

    public String getImplementationVersion(File dir, String file) {
        File jarFile = new File(dir, file);
        if(!jarFile.isFile()) {
            return null;
        }
        try {
            JarFile jar = new JarFile(jarFile);
            Attributes attributes = jar.getManifest().getMainAttributes();
            String version = attributes.getValue("Implementation-Version");
            return version;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean exists(RuntimeDefinition serverDefinition) {
        if (serverDefinition == null || serverDefinition.getLocation() == null) {
            return false;
        }
        return droolsExists(serverDefinition);
    }

    public void computeIncludedRuntimeDefinition(
            RuntimeDefinition serverDefinition) {
        if (serverDefinition == null
                || !SOA_P.equals(serverDefinition.getType())) {
            return;
        }
        File droolsRoot = serverDefinition.getLocation(); //$NON-NLS-1$
        if (droolsRoot.isDirectory()) {
            String name = DROOLS_PREFIX_HYPHEN + serverDefinition.getName(); //$NON-NLS-1$
            RuntimeDefinition sd = new RuntimeDefinition(name,
                    serverDefinition.getVersion(), DROOLS, droolsRoot);
            sd.setParent(serverDefinition);
            serverDefinition.getIncludedRuntimeDefinitions().add(sd);
        }
    }

    public String getVersion(RuntimeDefinition runtimeDefinition) {
        return runtimeDefinition.getVersion();
    }

} 