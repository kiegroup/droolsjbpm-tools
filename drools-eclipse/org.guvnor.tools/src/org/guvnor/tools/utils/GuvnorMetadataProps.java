package org.guvnor.tools.utils;
/**
 * Model for properties associated with Guvnor resources.
 * @author jgraham
 */
public class GuvnorMetadataProps {
	private String filename;
	private String repository;
	private String fullpath;
	private String version;
	private String revision;
	
	public GuvnorMetadataProps(String filename, String repository,
			                  String fullpath, String version, String revision) {
		this.filename = filename;
		this.repository = repository;
		this.fullpath = fullpath;
		this.version = version;
		this.revision = revision;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getRepository() {
		return repository;
	}
	public void setRepository(String repository) {
		this.repository = repository;
	}
	public String getFullpath() {
		return fullpath;
	}
	public void setFullpath(String fullpath) {
		this.fullpath = fullpath;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getRevision() {
		return revision;
	}
	public void setRevision(String revision) {
		this.revision = revision;
	}
}
