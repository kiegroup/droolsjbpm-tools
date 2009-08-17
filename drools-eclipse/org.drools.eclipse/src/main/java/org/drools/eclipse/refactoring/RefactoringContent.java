package org.drools.eclipse.refactoring;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;

/**
 * Store the files content changes when multiple files are refactored at same time.
 * 
 * @author Lucas Amador
 *
 */
public class RefactoringContent {

	private Integer processorHashcode;
	private Map<IFile, String> fileContents;

	public RefactoringContent() {
		this.processorHashcode = -1;
		this.fileContents = new HashMap<IFile, String>();
	}

	public void setProcessorHashcode(Integer processorHashcode) {
		this.processorHashcode = processorHashcode;
	}

	public Integer getProcessorHashcode() {
		return processorHashcode;
	}

	public String getIFileContent(IFile file) {
		return this.fileContents.get(file);
	}

	public void updateContent(IFile file, String content) {
		this.fileContents.put(file, content);
	}

	public void clear() {
		this.fileContents.clear();
	}

}
