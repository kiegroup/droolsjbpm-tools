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

package org.drools.eclipse.editors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Drools color manager.
 */
public class ColorManager {

    private static final ColorManager INSTANCE = new ColorManager();
    
    public static final RGB SINGLE_LINE_COMMENT = new RGB(128, 128, 0);
    public static final RGB KEYWORD = new RGB(150, 0, 0);
    public static final RGB STRING = new RGB(0, 128, 0);

    protected Map colorTable = new HashMap(10);

    void dispose() {
        Iterator e = colorTable.values().iterator();
        while (e.hasNext()) {
             ((Color) e.next()).dispose();
        }
    }
    
    public static ColorManager getInstance() {
        return INSTANCE;
    }
    
    public Color getColor(RGB rgb) {
        Color color = (Color) colorTable.get(rgb);
        if (color == null) {
            color = new Color(Display.getCurrent(), rgb);
            colorTable.put(rgb, color);
        }
        return color;
    }
}
