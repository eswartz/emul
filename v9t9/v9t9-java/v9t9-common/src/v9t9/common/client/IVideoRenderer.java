/*
  IVideoRenderer.java

  (c) 2008-2013 Edward Swartz

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
package v9t9.common.client;

import java.io.File;
import java.io.IOException;

import ejs.base.timer.FastTimer;

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
    /** Force redraw of screen from changes from VdpHandler#update, 
     * incorporating any resolution changes, blank/unblank state, etc. */
    void redraw();
    
    /** Synchronize so that screen updates are visible */
    void sync();

	/** Get the System.currentTimeMillis() when the last window redraw officially finished */
	long getLastUpdateTime();
	
	boolean isIdle();

	void saveScreenShot(File file) throws IOException;

	/**
	 * 
	 */
	void dispose();

	/** Get the basic canvas, before rendering */
	IVdpCanvas getCanvas();
	IVdpChip getVdpHandler();

	IVdpCanvasRenderer getCanvasHandler();

	/**
	 * A renderer should provide a timer for video update
	 * activities
	 * @return
	 */
	FastTimer getFastTimer();

	IMonitorEffectSupport getMonitorEffectSupport();
	
}
