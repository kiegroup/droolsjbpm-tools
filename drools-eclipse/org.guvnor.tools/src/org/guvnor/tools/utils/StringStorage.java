package org.guvnor.tools.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;

public class StringStorage extends PlatformObject implements IStorage {
	private String contents;
	private String name;
	
	public StringStorage(String contents, String name) {
		this.contents = contents;
		this.name = name;
	}

	public InputStream getContents() throws CoreException {
		return new ByteArrayInputStream(contents.getBytes());
	}

	public IPath getFullPath() {
		return null;
	}

	public String getName() {
		return name + " (Read only)"; //$NON-NLS-1$
	}

	public boolean isReadOnly() {
		return true;
	}
}
