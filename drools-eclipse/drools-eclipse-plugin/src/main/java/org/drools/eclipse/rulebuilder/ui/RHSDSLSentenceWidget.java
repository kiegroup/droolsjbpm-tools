package org.drools.eclipse.rulebuilder.ui;

import org.drools.guvnor.client.modeldriven.brl.DSLSentence;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class RHSDSLSentenceWidget extends DSLSentenceWidget {

    public RHSDSLSentenceWidget(FormToolkit toolkit,
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
        getModeller().getModel().removeRhsItem( index );
        getModeller().reloadRhs();
        getModeller().setDirty( true );
    }

}
