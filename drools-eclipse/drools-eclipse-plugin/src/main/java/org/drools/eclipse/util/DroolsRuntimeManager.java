package org.drools.eclipse.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.drools.eclipse.preferences.DroolsRuntimesBlock.DroolsRuntime;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

public class DroolsRuntimeManager {
	
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
        // get eclipse jdt jar
        String pluginRootString = Platform.getInstallLocation().getURL().getPath() + "plugins/";
	    File pluginRoot = new Path(pluginRootString).toFile();
	    files = pluginRoot.listFiles();
	    for (int i = 0; i < files.length; i++) {
	        if (files[i].getAbsolutePath().indexOf("org.eclipse.jdt.core_3.4") > -1) {
	        	jars.add(files[i].getAbsolutePath());
	        	break;
	        }
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
			result += runtime.getName() + "#" + runtime.getPath() + "#" + runtime.isDefault() + "###";
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
        	IFile file = project.getFile(".drools.runtime");
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

}
