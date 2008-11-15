package org.drools.eclipse.editors;

/*
 * Copyright 2006 JBoss Inc
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

import org.eclipse.draw2d.ScalableFigure;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ZoomInAction;

/**
 * Similar to ZoomInAction but adds setZoomManager(..) functionality.
 * 
 * ZoomInAction2 provides default constructor for initializing
 * ZoomInAction without the need for ZoomManager.
 * 
 * Please note that ZoomInAction2 is not very functional until it has
 * correct zoomManager set by setZoomManager(ZoomManager manager).<br/>
 * 
 * setZoomManager(ZoomManager manager) can be used several times.
 * 
 * @author Ahti Kitsik
 *
 */
public class ZoomInAction2 extends ZoomInAction {

    final private static ZoomManager FAKE_ZOOM_MANAGER = new ZoomManager( (ScalableFigure) null,
                                                                          null );

    /**
     * Default constructor to allow ZoomInActions without specified
     * ZoomManager.
     */
    public ZoomInAction2() {
        super( FAKE_ZOOM_MANAGER );
    }

    /**
     * Replaces existing zoomManager with the new one.
     * 
     * Implementation is null-safe.
     * 
     * @param newManager new zoom manager
     */
    public void setZoomManager(ZoomManager newManager) {

        if ( zoomManager != null ) {
            zoomManager.removeZoomListener( this );
        }

        zoomManager = newManager;

        if ( zoomManager != null ) {
            zoomManager.addZoomListener( this );
        }

    }

}
