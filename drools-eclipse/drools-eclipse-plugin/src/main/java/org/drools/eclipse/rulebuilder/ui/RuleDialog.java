package org.drools.eclipse.rulebuilder.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class RuleDialog extends Dialog {

    private String title;
    private String hint;

    public RuleDialog(Shell parent,
                      String title,
                      String hint) {
        //super(parent,INFOPOPUPRESIZE_SHELLSTYLE,true,true,true,true,title,hint);
        super( (Shell) parent.getParent() );
        setShellStyle( getShellStyle() | SWT.RESIZE );
        this.title = title;
        this.hint = hint;
    }

    protected void configureShell(Shell newShell) {
        super.configureShell( newShell );
        newShell.setText( title );
        newShell.setToolTipText( hint );
    }

    protected Control createDialogArea(Composite parent) {

        Composite finalComposite = new Composite( parent,
                                                  SWT.NONE );
        finalComposite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        GridLayout finalLayout = new GridLayout();
        finalComposite.setLayout( finalLayout );

        Composite titleComposite = new Composite( finalComposite,
                                                  SWT.NONE );
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_MARGIN );
        layout.marginWidth = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_MARGIN );
        layout.verticalSpacing = 8;
        layout.horizontalSpacing = 8;
        titleComposite.setLayout( layout );
        titleComposite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        applyDialogFont( titleComposite );

        Label l = new Label( titleComposite,
                             SWT.CENTER );
        l.setToolTipText( hint );
        l.setText( title );

        Font exFont = l.getFont();

        FontData[] exfds = l.getFont().getFontData();
        if ( exfds.length > 0 ) {
            FontData fd = exfds[0];
            fd.setHeight( fd.getHeight() + 4 );
            Font f = new Font( exFont.getDevice(),
                               fd );
            l.setFont( f );
        }

        Composite contentComposite = (Composite) super.createDialogArea( finalComposite );

        return contentComposite;
    }

    protected void createButtonsForButtonBar(Composite parent) {
        // We have only cancel button
        createButton( parent,
                      IDialogConstants.CANCEL_ID,
                      IDialogConstants.CANCEL_LABEL,
                      false );

    }

    /*    
     protected Control createDialogArea(Composite parent) {
     Composite composite = new Composite(parent, SWT.NONE);
     
     GridLayout layout = new GridLayout();
     layout.marginHeight = 2;
     layout.marginWidth = 2;
     layout.verticalSpacing = 2;
     layout.horizontalSpacing = 2;
     composite.setLayout(layout);
     
     GridData gd = new GridData(GridData.FILL_BOTH);
     composite.setLayoutData(gd);
     
     return composite;
     }
     */

    protected Label createLabel(Composite composite,
                                String string) {
        Label l = new Label( composite,
                             0 );
        l.setText( string );
        return l;
    }

    protected Text createText(Composite composite,
                              String string) {
        Text t = new Text( composite,
                           0 );
        t.setText( string );
        return t;
    }

}
