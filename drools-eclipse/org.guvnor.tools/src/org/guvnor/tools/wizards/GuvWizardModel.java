package org.guvnor.tools.wizards;

import java.util.List;

public class GuvWizardModel {
	private String repLocation;
	private String username;
	private String password;
	private boolean createNewRep;
	private boolean saveAuthInfo;
	
	private String targetProject;
	private boolean createNewProj;
	
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
	public boolean isCreateNewRep() {
		return createNewRep;
	}
	public void setCreateNewRep(boolean createNewRep) {
		this.createNewRep = createNewRep;
	}
	public String getTargetProject() {
		return targetProject;
	}
	public void setTargetProject(String targetProject) {
		this.targetProject = targetProject;
	}
	public boolean isCreateNewProj() {
		return createNewProj;
	}
	public boolean shouldSaveAuthInfo() {
		return saveAuthInfo;
	}
	public void setCreateNewProj(boolean createNewProj) {
		this.createNewProj = createNewProj;
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
	
	public boolean isModelComplete() {
		// TODO: Check if model is complete
		return true;
	}
}
