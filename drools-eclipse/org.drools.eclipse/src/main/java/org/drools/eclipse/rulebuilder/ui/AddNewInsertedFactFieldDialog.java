/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.eclipse.rulebuilder.ui;

import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.brl.ActionFieldValue;
import org.drools.ide.common.client.modeldriven.brl.ActionInsertFact;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class AddNewInsertedFactFieldDialog extends RuleDialog {

    private RuleModeller modeller;

    private final ActionInsertFact fact;

    public AddNewInsertedFactFieldDialog(Shell parent, RuleModeller modeller,
            ActionInsertFact fact) {
        super(parent, "Add new condition to the rule",
                "Pick the values from combos and confirm the selection.");
        this.modeller = modeller;
        this.fact = fact;
    }

    protected Control createDialogArea(final Composite parent) {

        Composite composite = (Composite) super.createDialogArea(parent);

        createLabel(composite, "Field:");

        final Combo factsCombo = new Combo(composite, SWT.READ_ONLY);

        String[] fields = getCompletion().getFieldCompletions(fact.factType);
        factsCombo.add("...");
        for (int i = 0; i < fields.length; i++) {
            factsCombo.add(fields[i]);
        }
        factsCombo.select(0);

        factsCombo.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {

                if (factsCombo.getSelectionIndex() == 0) {
                    return;
                }

                String fieldType = modeller.getSuggestionCompletionEngine()
                        .getFieldType(fact.factType, factsCombo.getText());
                fact.addFieldValue(new ActionFieldValue(factsCombo.getText(),
                        "", fieldType));

                modeller.setDirty(true);
                modeller.reloadRhs();
                close();
            }
        });

        return composite;
    }

    public SuggestionCompletionEngine getCompletion() {
        return modeller.getSuggestionCompletionEngine();
    }

}
