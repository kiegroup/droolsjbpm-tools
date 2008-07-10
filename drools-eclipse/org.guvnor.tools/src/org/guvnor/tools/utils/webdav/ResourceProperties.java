package org.guvnor.tools.utils.webdav;

public class ResourceProperties {
	private boolean isDirectory;
	private String creationDate;
	private String lastModifiedDate;
	private String revision;
	private String base;
	
	public String getBase() {
		return base;
	}
	public void setBase(String base) {
		this.base = base;
	}
	public boolean isDirectory() {
		return isDirectory;
	}
	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}
	public String getCreationDate() {
		return creationDate != null?creationDate:"";
	}
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	public String getLastModifiedDate() {
		return lastModifiedDate != null?lastModifiedDate:"";
	}
	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public String getRevision() {
		return revision != null?revision:"";
	}
	public void setRevision(String revision) {
		this.revision = revision;
	}
}
