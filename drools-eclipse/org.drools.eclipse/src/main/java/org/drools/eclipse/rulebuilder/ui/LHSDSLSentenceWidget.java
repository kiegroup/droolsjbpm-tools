package org.drools.eclipse.rulebuilder.ui;

import org.drools.ide.common.client.modeldriven.brl.DSLSentence;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class LHSDSLSentenceWidget extends DSLSentenceWidget {

    public LHSDSLSentenceWidget(FormToolkit toolkit,
                                Composite parent,
                                DSLSentence sentence,
                                RuleModeller modeller,
                                int index) {
        super( toolkit,
               parent,
               sentence,
               modeller,
               index );

    }

    protected void updateModel() {
        if ( getModeller().getModel().removeLhsItem( index ) ) {
            getModeller().reloadLhs();
        } else {
            showMessage( "Can't remove that item as it is used in the action part of the rule." );
        }
        getModeller().reloadLhs();
        getModeller().setDirty( true );
    }

}
