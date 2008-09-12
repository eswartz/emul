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
    void updateList(RedrawBlock[] blocks, int count);
    
    /** Force redraw of entire screen */
    void redraw();
    
    /** Synchronize so that screen updates are visible */
    void sync();
    
    /** Resize the screen to this size in pixels
        (usually 256x192 or 240x192 for text mode) */
    void resize(int width, int height);

    /** Update color 0 and color 16 (as appearing in the main
        bitmap) to these TI colors (which may be 0, interpret as black) */
    void setForegroundAndBackground(int bg, int fg);

    /** Blank or unblank the screen (using the current background color) */
    void setBlank(boolean blank);
}
