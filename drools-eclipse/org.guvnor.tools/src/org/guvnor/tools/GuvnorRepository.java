package org.guvnor.tools;

public class GuvnorRepository {
	private String location;
	private String username;
	private String password;
	
	public GuvnorRepository(String location, String username, String password) {
		this.location = location;
		this.username = username;
		this.password = password;
	}

	public String getLocation() {
		return location;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
