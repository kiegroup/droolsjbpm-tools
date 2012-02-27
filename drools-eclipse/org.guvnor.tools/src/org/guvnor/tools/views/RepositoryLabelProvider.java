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

package org.guvnor.tools.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.PendingUpdateAdapter;
import org.guvnor.tools.Activator;
import org.guvnor.tools.Messages;
import org.guvnor.tools.views.model.TreeObject;
import org.guvnor.tools.views.model.TreeParent;

/**
 * Label providers for Guvnor structure.
 */
public class RepositoryLabelProvider extends LabelProvider {
    private Map<ImageDescriptor, Image> images = new HashMap<ImageDescriptor, Image>();

    public RepositoryLabelProvider() {
    }

    public String getText(Object obj) {
        if (obj instanceof PendingUpdateAdapter) {
            return Messages.getString("pending"); //$NON-NLS-1$
        } else if (obj instanceof TreeParent) {
            switch (((TreeParent)obj).getNodeType()) {
            case GLOBALS:
                return "Global Area";
            case SNAPSHOTS:
                return "Snapshots";
            case PACKAGES:
                return "Packages";
            default:
                return stripTrailingSlash(obj.toString());
            }
        } else {
            return obj.toString();
        }
    }
    public Image getImage(Object obj) {
        Image res = null;
        if (obj instanceof TreeObject) {
            switch (((TreeObject)obj).getNodeType()) {
            case REPOSITORY:
                res = Activator.getDefault().getImageRegistry().get(Activator.IMG_GUVREP);
                break;
            case GLOBALS:
            case PACKAGES:
            case SNAPSHOTS:
                res = Activator.getDefault().getImageRegistry().get(Activator.IMG_GUVFOLDER);
                break;
            case PACKAGE:
                res = Activator.getDefault().getImageRegistry().get(Activator.IMG_GUVPACK);
                break;
            case SNAPSHOT_PACKAGE:
                res = Activator.getDefault().getImageRegistry().get(Activator.IMG_GUVSNAPPACK);
                break;
            case SNAPSHOT:
                res = Activator.getDefault().getImageRegistry().get(Activator.IMG_GUVSNAPSHOT);
                break;
            case RESOURCE:
                ImageDescriptor descriptor = PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(patchExtension(((TreeObject)obj).getName()));
                if (descriptor == null) {
                    res = PlatformUI.getWorkbench().getSharedImages().
                            getImage(ISharedImages.IMG_OBJ_FILE);
                } else {
                    res = images.get(descriptor);
                    if (res == null) {
                        res = descriptor.createImage();
                        images.put(descriptor, res);
                    }
                    res = descriptor.createImage();
                }
                break;
            }
        }
        return res != null?res:PlatformUI.getWorkbench().getSharedImages().
                                                getImage(ISharedImages.IMG_OBJ_ELEMENT);
    }

    @Override
    public void dispose() {
        for (Image image : images.values()) {
            try {
                image.dispose();
            } catch (Exception e) {
            }
        }
        images.clear();
        super.dispose();
    }

    private String patchExtension(String name) {
        int index = name.lastIndexOf(".xmlschema");
        if (index > -1) {
            return name.substring(0,index) +".xsd";
        }
        return name;
    }
    
    private String stripTrailingSlash(String name) {
        if (name.endsWith("/")) {
            return name.substring(0, name.length() - 1);
        }
        return name;
    }
}
