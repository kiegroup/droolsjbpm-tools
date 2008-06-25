package org.guvnor.tools.utils.webdav;

public class WebDavException extends Exception {
	
	//TODO: put a real serialization value here
	private static final long serialVersionUID = 1L;
	
	private int errCode;
	
	public WebDavException(int errCode) {
		super();
		this.errCode = errCode;
	}
	
	public int getErrorCode() {
		return errCode;
	}
}
