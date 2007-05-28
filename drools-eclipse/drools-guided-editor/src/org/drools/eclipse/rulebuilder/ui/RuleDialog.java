package org.drools.eclipse.rulebuilder.ui;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.widgets.Shell;

public class RuleDialog extends PopupDialog {

    public RuleDialog(Shell parent, String title, String hint) {
        super(parent,INFOPOPUPRESIZE_SHELLSTYLE,true,true,true,true,title,hint);
    }
    
}
