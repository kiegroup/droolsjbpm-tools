package org.guvnor.tools.preferences;
/**
 * Constants for Guvnor tools preference names and values.
 * @author jgraham
 */
public interface IGuvnorPreferenceConstants {
	
	// Value is a string, specified in GUVNOR_LOC_TEMPLATE_DEFAULT 
	public String GUVNOR_LOC_TEMPLATE_PREF = "guvnor_loc_template";
	
	public String GUVNOR_LOC_TEMPLATE_DEFAULT = "http://localhost:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav";
	
	public String SAVE_PASSWORDS_PREF = "save_passwords";
	
	// Value it an integer, specified in OVERLAY_LOCATIONS 
	public String OVERLAY_LOCATION_PREF = "overlay_location";
	
	// Text for the corresponding IDecoration overlay locations, 0 -> 3. 4 is for "don't display"
	public String[] OVERLAY_LOCATIONS = { "Top left", 
			                              "Top right", 
			                              "Bottom left", 
			                              "Bottom right", 
			                              "<None>" };
	
	public int NO_OVERLAY = 4;
	
	// The default location is top right
	public int OVERLAY_LOCATION_DEFAULT = 1;
	
	// Value is a boolean, true by default
	public String SHOW_CHANGE_INDICATOR_PREF = "show_change_indicator";
	
	// Value is a boolean, true by default
	public String SHOW_REVISION_PREF = "show_revision";
	
	// Value is a boolean, true by default
	public String SHOW_DATETIME_PREF = "show_datetime";
}
