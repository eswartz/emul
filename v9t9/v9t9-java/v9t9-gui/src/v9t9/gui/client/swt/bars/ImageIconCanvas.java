/*
  ImageIconCanvas.java

  (c) 2011-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.gui.client.swt.bars;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;

/**
 * An image, which is allowed to be transparent and drawn on a bar.
 * @author ejs
 *
 */
public abstract class ImageIconCanvas extends ImageBarChild {


	protected ImageIconInfo imageIconInfo;
	protected Rectangle bounds;
	protected final ImageProvider imageProvider;
	
	public ImageIconCanvas(IImageCanvas parentDrawer, int style, 
			ImageProvider imageProvider, int iconIndex, String tooltip) {
		super(parentDrawer,  style /*| SWT.NO_BACKGROUND*/);
		
		this.imageProvider = imageProvider;
		
		if (imageProvider != null)
			imageIconInfo = new ImageIconInfo(imageProvider);
		else
			imageIconInfo = null;
		
		setLayout(new FillLayout());
		
		if (tooltip != null)
			setToolTipText(tooltip);
		
		setIconIndex(iconIndex);
	}
	
	public void setIconIndex(int iconIndex) {
		
		GridData data;
		if (imageIconInfo != null) {
			imageIconInfo.setIconIndex(iconIndex);
			bounds = imageIconInfo.getBounds();
			data = new GridData(bounds.width, bounds.height);
			data.grabExcessHorizontalSpace = false;
			data.grabExcessVerticalSpace = false;
		} else {
			bounds = null;
			data = GridDataFactory.fillDefaults().grab(true, false).create();
		}
		setLayoutData(data);
	}


	protected void doPaint(PaintEvent e) {
		super.doPaint(e);
		drawImage(e);
	}

	/**
	 * @param e
	 */
	protected void drawImage(PaintEvent e) {
		Rectangle drawRect = getBounds();
		try {
			imageProvider.drawImage(e.gc, drawRect, bounds);
		} catch (IllegalArgumentException e2) {
			e2.printStackTrace();
		}
	}
}