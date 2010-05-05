package org.drools.eclipse.rulebuilder.ui;

import org.drools.ide.common.client.modeldriven.brl.ActionRetractFact;
import org.drools.eclipse.rulebuilder.modeldriven.HumanReadable;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * 
 * @author Anton Arhipov
 *
 */
public class ActionRetractFactWidget extends Widget {

    private final ActionRetractFact fact;

    public ActionRetractFactWidget(FormToolkit toolkit,
                                   Composite parent,
                                   RuleModeller modeller,
                                   ActionRetractFact fact,
                                   int index) {

        super( parent,
               toolkit,
               modeller,
               index );

        this.fact = fact;

        GridLayout l = new GridLayout();
        l.numColumns = 4;
        l.marginBottom = 0;
        l.marginHeight = 0;
        l.marginLeft = 0;
        l.marginRight = 0;
        l.marginTop = 0;
        l.marginWidth = 0;
        l.verticalSpacing = 0;
        parent.setLayout( l );

        create();
    }

    private void create() {
        toolkit.createLabel( parent,
                             HumanReadable.getActionDisplayName( "retract" ) );
        toolkit.createLabel( parent,
                             "[" + fact.variableName + "]" );
        addDeleteRHSAction();
    }

}
