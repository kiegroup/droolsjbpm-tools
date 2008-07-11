package org.guvnor.tools.utils.webdav;

import org.eclipse.webdav.IResponse;

public class WebDavException extends Exception {
	
	private static final long serialVersionUID = -2421203349714311291L;
	
	private int errCode;
	
	public WebDavException(IResponse response) {
		super("WebDav error: " + response.getStatusMessage() + 
	          " (" + response.getStatusCode() + ")");
		this.errCode = response.getStatusCode();
	}
	
	public int getErrorCode() {
		return errCode;
	}
}
