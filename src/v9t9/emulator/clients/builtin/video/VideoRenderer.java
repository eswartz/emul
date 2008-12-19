/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 20, 2006
 *
 */
package v9t9.emulator.clients.builtin.video;

/**
 * This interface is implemented to handle blitting a VdpCanvas bitmap to a real
 * hardware device. 
 * @author ejs
 *
 */
public interface VideoRenderer {
    /** Update screen (or offscreen page) from blocks in list */
    //void updateList(RedrawBlock[] blocks, int count);
    
    /** Force redraw of screen from changes above, incorporating any
     * resolution changes, blank/unblank state, etc. */
    void redraw();
    
    /** Synchronize so that screen updates are visible */
    void sync();

	VdpCanvas getCanvas();
	
	void setCanvas(VdpCanvas vdpCanvas);
	
	/** Get the System.currentTimeMillis() when the last window redraw officially finished */
	long getLastUpdateTime();
	
	boolean isIdle();

	void setZoom(int zoom);
}
