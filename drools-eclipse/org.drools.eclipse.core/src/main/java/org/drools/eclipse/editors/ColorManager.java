package org.drools.eclipse.editors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Drools color manager.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
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
