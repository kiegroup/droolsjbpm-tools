package org.drools.eclipse.refactoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class FileUtil {

	private FileUtil() { }
	
    public static String readFile(IFile file) throws CoreException {
        InputStream inputStream = file.getContents();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        try {
            char[] buf = new char[1024];
            int numRead=0;
            while((numRead=reader.read(buf)) != -1){
            	sb.append(buf, 0, numRead);
            }
        } catch (IOException e) {
            return null;
        } finally {
            try {
            	if (reader != null) {
            		reader.close();
            	}
            	if (inputStream != null) {
            		inputStream.close();
            	}
            }
            catch (IOException e) {
                // Nothing
            }
        }
        return sb.toString();
    }
}
