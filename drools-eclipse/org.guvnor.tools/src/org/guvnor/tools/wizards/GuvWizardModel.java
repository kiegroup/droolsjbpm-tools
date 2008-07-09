package org.guvnor.tools.wizards;

import java.util.List;

public class GuvWizardModel {
	private String repLocation;
	private String username;
	private String password;
	private boolean createNewRep;
	private boolean saveAuthInfo;
	
	private String targetLocation;
	
	private List<String> resources;
	private String version;
	
	public String getRepLocation() {
		return repLocation;
	}
	public void setRepLocation(String repLocation) {
		this.repLocation = repLocation;
	}
	public String getUsername() {
		return username != null?username:"";
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password != null?password:"";
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean shouldCreateNewRep() {
		return createNewRep;
	}
	public void setCreateNewRep(boolean createNewRep) {
		this.createNewRep = createNewRep;
	}
	public String getTargetLocation() {
		return targetLocation;
	}
	public void setTargetLocation(String targetLocation) {
		this.targetLocation = targetLocation;
	}
	public boolean shouldSaveAuthInfo() {
		return saveAuthInfo;
	}
	public void setSaveAuthInfo(boolean saveAuthInfo) {
		this.saveAuthInfo = saveAuthInfo;
	}
	public List<String> getResources() {
		return resources;
	}
	public void setResources(List<String> resources) {
		this.resources = resources;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
}
