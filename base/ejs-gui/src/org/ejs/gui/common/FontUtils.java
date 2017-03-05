/*
  FontUtils.java

  (c) 2010-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package org.ejs.gui.common;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;



/** 
 * @author ejs
 */
public class FontUtils  {
	
	/** Overcome bug in SWT */
	public static FontDescriptor getFontDescriptor(Font font) {
		// hmmm... FontRegister.createFont() is busted
		FontData[] fontData = font.getFontData();
		int len = 0;
		while (len < fontData.length && fontData[len] != null) 
			len++;
		FontData[] fontData2 = new FontData[len];
		System.arraycopy(fontData, 0, fontData2, 0, len);
		///
		
		FontDescriptor fontDescriptor = FontDescriptor.createFrom(fontData2);
		return fontDescriptor;
	}
	
	public static Point measureText(Device device, Font font, String text) {
		GC gc = new GC(device);
		Point extent = gc.textExtent(text, SWT.DRAW_DELIMITER);
		gc.dispose();
		return extent;
	}

	  /**
     * Create a new font based on another font
     * @param baseFont
     * @param height
     * @param style
     * @return new Font
     */
    public static Font createResizedFont(Font baseFont, int height, int style) {
        if (height < 1) {
                height = 1;
        }
        FontData[] fds = baseFont.getFontData();
        for (FontData fd : fds) {
                fd.setHeight(height);
                fd.setStyle(style);
        }
        return new Font(baseFont.getDevice(), fds);
    }

}
