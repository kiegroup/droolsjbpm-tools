/**
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
