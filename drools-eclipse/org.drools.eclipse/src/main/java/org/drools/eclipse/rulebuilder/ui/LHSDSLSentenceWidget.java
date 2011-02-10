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
