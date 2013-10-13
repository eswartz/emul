/*
  ISwtVideoRenderer.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import v9t9.common.client.IVideoRenderer;
import v9t9.gui.common.BaseEmulatorWindow;

/**
 * Interface implemented by SWT-compatible video renderers.
 * @author ejs
 *
 */
public interface ISwtVideoRenderer extends IVideoRenderer {
	Control createControl(BaseEmulatorWindow window, Composite parent, int flags);
	
	Control getControl();
	
	void addMouseEventListener(MouseListener listener);
	void addMouseMotionListener(MouseMoveListener listener);

	boolean isVisible();
	
	
	void setFocus();


	/**
	 * Reblit the screen (for indicator changes)
	 */
	void reblit();

	ImageData getPlainScreenshotImageData();
	ImageData getActualScreenshotImageData();
	
	void addSprite(ISwtSprite sprite);
	void removeSprite(ISwtSprite sprite);
	
	BaseEmulatorWindow getWindow();
}
