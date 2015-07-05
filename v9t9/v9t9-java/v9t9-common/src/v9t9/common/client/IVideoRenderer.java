/*
  IVideoRenderer.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.client;

import java.io.File;
import java.io.IOException;

import v9t9.common.hardware.IVdpChip;
import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.IVdpCanvasRenderer;

/**
 * This interface is implemented to handle blitting a VdpCanvas bitmap to a real
 * hardware device. 
 * @author ejs
 *
 */
public interface IVideoRenderer {
	public interface IVideoRenderListener {
		/** Issued when video content has changed */
		void finishedRedraw(IVdpCanvas canvas);
	}
	
	void addListener(IVideoRenderListener listener);
	void removeListener(IVideoRenderListener listener);
	
    /** Force redraw of screen from changes from VdpHandler#update, 
     * incorporating any resolution changes, blank/unblank state, etc. */
    void queueRedraw();
    
    /** Synchronize so that screen updates are visible */
    void sync();

	/** Get the System.currentTimeMillis() when the last window redraw officially finished */
	long getLastUpdateTime();
	
	boolean isIdle();
	
	boolean isVisible();

	void saveScreenShot(File file, boolean plainBitmap) throws IOException;

	/**
	 * 
	 */
	void dispose();

	/** Get the basic canvas, before rendering */
	IVdpCanvas getCanvas();
	IVdpChip getVdpHandler();

	IVdpCanvasRenderer getCanvasHandler();

	IMonitorEffectSupport getMonitorEffectSupport();
	
}
