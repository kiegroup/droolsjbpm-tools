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
