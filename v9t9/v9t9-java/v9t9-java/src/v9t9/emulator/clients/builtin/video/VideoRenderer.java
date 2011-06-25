/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 20, 2006
 *
 */
package v9t9.emulator.clients.builtin.video;

import java.io.File;
import java.io.IOException;

/**
 * This interface is implemented to handle blitting a VdpCanvas bitmap to a real
 * hardware device. 
 * @author ejs
 *
 */
public interface VideoRenderer {
    
    /** Force redraw of screen from changes from VdpHandler#update, 
     * incorporating any resolution changes, blank/unblank state, etc. */
    void redraw();
    
    /** Synchronize so that screen updates are visible */
    void sync();

	/** Get the System.currentTimeMillis() when the last window redraw officially finished */
	long getLastUpdateTime();
	
	boolean isIdle();

	void setZoom(int zoom);
	int getZoom();

	void saveScreenShot(File file) throws IOException;

	/**
	 * 
	 */
	void dispose();

}
