package org.guvnor.tools.preferences;

import org.guvnor.tools.Messages;

/**
 * Constants for Guvnor tools preference names and values.
 * @author jgraham
 */
public interface IGuvnorPreferenceConstants {
	
	// Value is a string, specified in GUVNOR_LOC_TEMPLATE_DEFAULT 
	public String GUVNOR_LOC_TEMPLATE_PREF = "guvnor_loc_template"; //$NON-NLS-1$
	
	public String GUVNOR_LOC_TEMPLATE_DEFAULT = "http://localhost:8080/jboss-brms/org.drools.guvnor.Guvnor/webdav"; //$NON-NLS-1$
	
	public String SAVE_PASSWORDS_PREF = "save_passwords"; //$NON-NLS-1$
	
	// Value it an integer, specified in OVERLAY_LOCATIONS 
	public String OVERLAY_LOCATION_PREF = "overlay_location"; //$NON-NLS-1$
	
	// Text for the corresponding IDecoration overlay locations, 0 -> 3. 4 is for "don't display"
	public String[] OVERLAY_LOCATIONS = { Messages.getString("top.left"),  //$NON-NLS-1$
			                              Messages.getString("top.right"),  //$NON-NLS-1$
			                              Messages.getString("bottom.left"),  //$NON-NLS-1$
			                              Messages.getString("bottom.right"),  //$NON-NLS-1$
			                              Messages.getString("no.overlay") }; //$NON-NLS-1$
	
	public int NO_OVERLAY = 4;
	
	// The default location is top right
	public int OVERLAY_LOCATION_DEFAULT = 1;
	
	// Value is a boolean, true by default
	public String SHOW_CHANGE_INDICATOR_PREF = "show_change_indicator"; //$NON-NLS-1$
	
	// Value is a boolean, true by default
	public String SHOW_REVISION_PREF = "show_revision"; //$NON-NLS-1$
	
	// Value is a boolean, true by default
	public String SHOW_DATETIME_PREF = "show_datetime"; //$NON-NLS-1$
}
