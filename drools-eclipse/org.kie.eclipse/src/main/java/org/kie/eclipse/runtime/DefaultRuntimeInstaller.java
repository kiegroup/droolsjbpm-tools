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

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jgit.fnmatch.FileNameMatcher;

public class DefaultRuntimeInstaller extends AbstractRuntimeInstaller {

	public static DefaultRuntimeInstaller INSTANCE = new DefaultRuntimeInstaller();

	public DefaultRuntimeInstaller() {}
	
	@Override
	public String install(String runtimeId, String location, IProgressMonitor monitor) {
		FileNameMatcher fn;
		AbstractRuntimeInstaller installer = FACTORY.getInstaller(runtimeId);
		if (installer==null) {
			return "No installer found for "+runtimeId;
		}
		return null;
	}
	
	public static Collection<? extends IRuntimeInstaller> getInstallers() {
		return FACTORY.createInstallers();
	}

	@Override
	public String getRuntimeId() {
		return getProduct() + "_" + getVersion();
	}
}
