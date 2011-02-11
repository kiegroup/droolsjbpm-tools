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

package org.drools.eclipse.debug.actions;


import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.debug.AuditView;
import org.drools.eclipse.debug.AuditView.Event;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

/**
 * Action to show the cause event of an audit event.
 */
public class ShowEventCauseAction extends Action {
    
    private AuditView view;

    public ShowEventCauseAction(AuditView view) {
        super(null, IAction.AS_PUSH_BUTTON);
        this.view = view;
        setToolTipText("Show Cause");
        setText("Show Cause");
        setId(DroolsEclipsePlugin.getUniqueIdentifier() + ".ShowEventCause");
    }

    public void run() {
        Event event = view.getSelectedEvent();
        if (event != null) {
            Event cause = event.getCauseEvent();
            if (cause != null) {
                view.showEvent(cause);
            }
        }
    }
}
