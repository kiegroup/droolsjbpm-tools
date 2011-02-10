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

import org.drools.eclipse.debug.AuditView;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;

public class FileAuditDropAdapter extends ViewerDropAdapter implements DropTargetListener {

    private AuditView view = null;

    public FileAuditDropAdapter(Viewer viewer, AuditView view) {
        super(viewer);
        this.view = view;
    }

    @Override
    public boolean performDrop(Object data) {
        String[] toDrop = (String[])data;
        if (toDrop.length>0) {
            view.setLogFile (toDrop[0]);
            return true;
        }
        return false;
    }
    @Override
    public boolean validateDrop(Object target, int operation,
            TransferData transferType) {
        return FileTransfer.getInstance().isSupportedType(transferType);

    }

}
