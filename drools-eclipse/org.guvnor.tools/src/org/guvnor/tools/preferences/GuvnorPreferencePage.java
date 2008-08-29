package org.guvnor.tools.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.guvnor.tools.Activator;
import org.guvnor.tools.Messages;
import org.guvnor.tools.utils.PlatformUtils;

/**
 * Page for setting Guvnor preferences.
 * @author jgraham
 */
public class GuvnorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private Text guvnorURLTemplate;
	private Button savePassword;
	private Combo decorationIconLoc;
	private Button incChangeIndicator;
	private Button incRevision;
	private Button incDateStamp;
	
	public GuvnorPreferencePage() {
	}

	public GuvnorPreferencePage(String title) {
		super(title);
	}

	public GuvnorPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = PlatformUtils.createComposite(parent, 1);
		
		Group group = new Group(composite, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);
		group.setText(Messages.getString("prepage.repository.connections")); //$NON-NLS-1$
		
		Composite doubleLine = PlatformUtils.createComposite(group, 2);
		new Label(doubleLine, SWT.NONE).setText(Messages.getString("prepage.guvnor.url.template")); //$NON-NLS-1$
		guvnorURLTemplate = new Text(doubleLine, SWT.BORDER);
		guvnorURLTemplate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		guvnorURLTemplate.setText(getGuvnorTemplatePref());
		
		savePassword = new Button(group, SWT.CHECK);
		savePassword.setText(Messages.getString("prepage.save.passwords")); //$NON-NLS-1$
		savePassword.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		savePassword.setSelection(true);
		
		group = new Group(composite, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);
		layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);
		group.setText(Messages.getString("prepage.file.decoration")); //$NON-NLS-1$
		
		doubleLine = PlatformUtils.createComposite(group, 2);
		new Label(doubleLine, SWT.NONE).setText(Messages.getString("prepage.decoration.location")); //$NON-NLS-1$
		decorationIconLoc = new Combo(doubleLine, SWT.BORDER | SWT.DROP_DOWN);
		String[] locs = IGuvnorPreferenceConstants.OVERLAY_LOCATIONS;
		for (int i = 0; i < locs.length; i++) {
			decorationIconLoc.add(locs[i]);
		}
		decorationIconLoc.select(getOverlayLocationPref());
		
		Group textDec = new Group(group, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		textDec.setLayoutData(data);
		layout = new GridLayout();
		layout.numColumns = 1;
		textDec.setLayout(layout);
		textDec.setText(Messages.getString("prepage.decoration.text")); //$NON-NLS-1$
		
		incChangeIndicator = new Button(textDec, SWT.CHECK);
		incChangeIndicator.setText(Messages.getString("prepage.include.change.indicator")); //$NON-NLS-1$
		incChangeIndicator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		incChangeIndicator.setSelection(shouldShowChangeIndicator());
		
		incRevision = new Button(textDec, SWT.CHECK);
		incRevision.setText(Messages.getString("prepage.include.revision")); //$NON-NLS-1$
		incRevision.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		incRevision.setSelection(shouldShowRevision());
		
		incDateStamp = new Button(textDec, SWT.CHECK);
		incDateStamp.setText(Messages.getString("prepage.include.date.time.stamp")); //$NON-NLS-1$
		incDateStamp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		incDateStamp.setSelection(shouldShowTimeDateStamp());
		
		return composite;
	}

	
	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	public void init(IWorkbench workbench) { }

	@Override
	protected void performDefaults() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		store.putValue(IGuvnorPreferenceConstants.GUVNOR_LOC_TEMPLATE_PREF, 
				      IGuvnorPreferenceConstants.GUVNOR_LOC_TEMPLATE_DEFAULT);
		guvnorURLTemplate.setText(IGuvnorPreferenceConstants.GUVNOR_LOC_TEMPLATE_DEFAULT);
		
		store.putValue(IGuvnorPreferenceConstants.SAVE_PASSWORDS_PREF, 
				      String.valueOf(true));
		savePassword.setSelection(true);
		
		store.putValue(IGuvnorPreferenceConstants.OVERLAY_LOCATION_PREF, 
				      String.valueOf(IGuvnorPreferenceConstants.OVERLAY_LOCATION_DEFAULT));
		decorationIconLoc.select(IGuvnorPreferenceConstants.OVERLAY_LOCATION_DEFAULT);
		
		store.putValue(IGuvnorPreferenceConstants.SHOW_CHANGE_INDICATOR_PREF, 
				      String.valueOf(true));
		incChangeIndicator.setSelection(true);
		
		store.putValue(IGuvnorPreferenceConstants.SHOW_REVISION_PREF, 
				      String.valueOf(true));
		incRevision.setSelection(true);
		
		store.putValue(IGuvnorPreferenceConstants.SHOW_DATETIME_PREF, 
				      String.valueOf(true));
		incDateStamp.setSelection(true);
		
		PlatformUtils.updateDecoration();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		store.putValue(IGuvnorPreferenceConstants.GUVNOR_LOC_TEMPLATE_PREF, 
				      guvnorURLTemplate.getText());
		
		store.putValue(IGuvnorPreferenceConstants.SAVE_PASSWORDS_PREF, 
					  String.valueOf(savePassword.getSelection()));
		
		store.putValue(IGuvnorPreferenceConstants.OVERLAY_LOCATION_PREF, 
				      String.valueOf(decorationIconLoc.getSelectionIndex()));
		
		store.putValue(IGuvnorPreferenceConstants.SHOW_CHANGE_INDICATOR_PREF, 
				      String.valueOf(incChangeIndicator.getSelection()));
		
		store.putValue(IGuvnorPreferenceConstants.SHOW_REVISION_PREF, 
				      String.valueOf(incRevision.getSelection()));
		
		store.putValue(IGuvnorPreferenceConstants.SHOW_DATETIME_PREF, 
				      String.valueOf(incDateStamp.getSelection()));
		
		PlatformUtils.updateDecoration();
		return super.performOk();
	}
	
	public static String getGuvnorTemplatePref() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String res = null;
		if (!store.contains(IGuvnorPreferenceConstants.GUVNOR_LOC_TEMPLATE_PREF)) {
			res = IGuvnorPreferenceConstants.GUVNOR_LOC_TEMPLATE_DEFAULT;
			store.putValue(IGuvnorPreferenceConstants.GUVNOR_LOC_TEMPLATE_PREF, res);
		} else {
			res = store.getString(IGuvnorPreferenceConstants.GUVNOR_LOC_TEMPLATE_PREF);
		}
		return res;
	}
	
	public static boolean shouldSavePasswords() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean res = true;
		if (!store.contains(IGuvnorPreferenceConstants.SAVE_PASSWORDS_PREF)) {
			store.putValue(IGuvnorPreferenceConstants.SAVE_PASSWORDS_PREF, String.valueOf(true));
		} else {
			res = store.getBoolean(IGuvnorPreferenceConstants.SAVE_PASSWORDS_PREF);
		}
		return res;
	}
	
	public static int getOverlayLocationPref() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		int res = 0;
		if (!store.contains(IGuvnorPreferenceConstants.OVERLAY_LOCATION_PREF)) {
			store.putValue(IGuvnorPreferenceConstants.OVERLAY_LOCATION_PREF, 
					      String.valueOf(IGuvnorPreferenceConstants.OVERLAY_LOCATION_DEFAULT));
			res = IGuvnorPreferenceConstants.OVERLAY_LOCATION_DEFAULT;
		} else {
			res = store.getInt(IGuvnorPreferenceConstants.OVERLAY_LOCATION_PREF);
		}
		return res;
	}
	
	public static boolean shouldShowChangeIndicator() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean res = true;
		if (!store.contains(IGuvnorPreferenceConstants.SHOW_CHANGE_INDICATOR_PREF)) {
			store.putValue(IGuvnorPreferenceConstants.SHOW_CHANGE_INDICATOR_PREF, String.valueOf(true));
		} else {
			res = store.getBoolean(IGuvnorPreferenceConstants.SHOW_CHANGE_INDICATOR_PREF);
		}
		return res;
	}
	
	public static boolean shouldShowRevision() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean res = true;
		if (!store.contains(IGuvnorPreferenceConstants.SHOW_REVISION_PREF)) {
			store.putValue(IGuvnorPreferenceConstants.SHOW_REVISION_PREF, String.valueOf(true));
		} else {
			res = store.getBoolean(IGuvnorPreferenceConstants.SHOW_REVISION_PREF);
		}
		return res;
	}
	
	public static boolean shouldShowTimeDateStamp() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean res = true;
		if (!store.contains(IGuvnorPreferenceConstants.SHOW_DATETIME_PREF)) {
			store.putValue(IGuvnorPreferenceConstants.SHOW_DATETIME_PREF, String.valueOf(true));
		} else {
			res = store.getBoolean(IGuvnorPreferenceConstants.SHOW_DATETIME_PREF);
		}
		return res;
	}
}
