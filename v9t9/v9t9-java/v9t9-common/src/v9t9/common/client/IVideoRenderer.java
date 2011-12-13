/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 20, 2006
 *
 */
package v9t9.common.client;

import java.io.File;
import java.io.IOException;

import v9t9.base.timer.FastTimer;
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

}
