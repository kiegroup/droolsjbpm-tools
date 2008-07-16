package org.guvnor.tools.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.graphics.Image;
import org.guvnor.tools.Activator;

/**
 * Represents compare input contents.
 * @author jgraham
 *
 */
public class GuvnorResourceEdition implements IStreamContentAccessor, ITypedElement {
	
	private String contents;
	private Image image;
	private String name;
	private String type;
	private String encoding;
	
	public GuvnorResourceEdition(String name, String type, String contents, String encoding) {
		this.name = name;
		this.type = type;
		this.contents = contents;
		this.encoding = encoding;
	}
	
	public InputStream getContents() throws CoreException {
		byte[] bytes = null;
		try {
			bytes = contents.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
			// Better than nothing?
			bytes = contents.getBytes();
		}
		return new ByteArrayInputStream(bytes);
	}

	public Image getImage() {
		return image;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}
}
