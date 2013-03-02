/*
  ISwtVideoRenderer.java

  (c) 2008-2012 Edward Swartz

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
package v9t9.gui.client.swt;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import v9t9.common.client.IVideoRenderer;

/**
 * Interface implemented by SWT-compatible video renderers.
 * @author ejs
 *
 */
public interface ISwtVideoRenderer extends IVideoRenderer {
	Control createControl(Composite parent, int flags);
	
	Control getControl();
	
	void addMouseEventListener(MouseListener listener);
	void addMouseMotionListener(MouseMoveListener listener);

	boolean isVisible();
	
	
	void setFocus();


	/**
	 * Reblit the screen (for indicator changes)
	 */
	void reblit();

	ImageData getScreenshotImageData();
	
	void addSprite(ISwtSprite sprite);
	void removeSprite(ISwtSprite sprite);
}
