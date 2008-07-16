package org.guvnor.tools.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A simple compare editor input for text files. 
 * @author jgraham
 */
public class GuvnorCompareEditorInput extends CompareEditorInput {
	
	private GuvnorResourceEdition left;
	private GuvnorResourceEdition right;
	
	public GuvnorCompareEditorInput(GuvnorResourceEdition left, GuvnorResourceEdition right) {
		super(new CompareConfiguration());
		this.left = left;
		this.right = right;
	}

	@Override
	protected Object prepareInput(IProgressMonitor monitor) 
						throws InvocationTargetException, InterruptedException {
		Differencer diff = new Differencer();
		return diff.findDifferences(false, null, null, null, left, right);
	}
}
