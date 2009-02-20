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
