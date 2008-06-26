package org.guvnor.tools.utils.webdav;

public class WebDavException extends Exception {
	
	private static final long serialVersionUID = -2421203349714311291L;
	
	private int errCode;
	
	public WebDavException(String msg, int errCode) {
		super(msg);
		this.errCode = errCode;
	}
	
	public int getErrorCode() {
		return errCode;
	}
}
