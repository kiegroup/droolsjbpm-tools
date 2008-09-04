package org.guvnor.tools;

/**
 * A simple representation of a Guvnor repository.
 * @author jgraham
 */
public class GuvnorRepository {
	private String location;
	
	public GuvnorRepository(String location) {
		this.location = location;
	}

	public String getLocation() {
		return location;
	}
}
