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

package org.drools.eclipse.debug;

import java.util.List;

import org.drools.eclipse.debug.AuditView.Event;

public class AuditViewContentProvider extends DroolsDebugViewContentProvider {

    protected String getEmptyString() {
        return "The selected audit log is empty.";
    }

    public Object[] getChildren(Object obj) {
        if (obj instanceof List) {
            return ((List) obj).toArray();
        }
        if (obj instanceof Event) {
            return ((Event) obj).getSubEvents();
        }
        return new Object[0];
    }
    
    public boolean hasChildren(Object obj) {
        if (obj instanceof Event) {
            return ((Event) obj).hasSubEvents();
        }
        return false;
    }
}
