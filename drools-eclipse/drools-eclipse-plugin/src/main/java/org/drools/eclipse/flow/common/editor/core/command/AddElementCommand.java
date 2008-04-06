package org.drools.eclipse.flow.common.editor.core.command;
/*
 * Copyright 2005 JBoss Inc
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

import org.drools.eclipse.flow.common.editor.core.ElementContainer;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.eclipse.gef.commands.Command;

/**
 * A command for adding an element.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class AddElementCommand extends Command {

    private ElementWrapper child;
    private ElementContainer parent;

    public void execute() {
        parent.addElement(child);
        child.setParent(parent);
    }

    protected ElementContainer getParent() {
        return parent;
    }
    
    protected ElementWrapper getChild() {
        return child;
    }

    public void setChild(ElementWrapper newChild) {
        child = newChild;
    }

    public void setParent(ElementContainer newParent) {
        parent = newParent;
    }

    public void undo() {
        parent.removeElement(child);
        child.setParent(null);
    }

}
