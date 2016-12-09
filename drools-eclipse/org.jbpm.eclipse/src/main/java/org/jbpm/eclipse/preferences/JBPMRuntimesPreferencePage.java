/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.eclipse.preferences;

import org.jbpm.eclipse.util.JBPMRuntimeManager;
import org.kie.eclipse.preferences.AbstractRuntimesBlock;
import org.kie.eclipse.preferences.AbstractRuntimesPreferencePage;
import org.kie.eclipse.runtime.IRuntimeManager;

public class JBPMRuntimesPreferencePage extends AbstractRuntimesPreferencePage {

	public JBPMRuntimesPreferencePage() {
		super("Installed jBPM Runtimes");
	}

	@Override
	protected IRuntimeManager getRuntimeManager() {
		return JBPMRuntimeManager.getDefault();
	}

	@Override
	protected AbstractRuntimesBlock createRuntimesBlock() {
		return new JBPMRuntimesBlock();
	}

	protected String getDownloadHelpURL() {
		return "http://www.jbpm.org/download/download.html";
	}
}
