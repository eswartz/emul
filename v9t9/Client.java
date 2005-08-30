/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9;

import v9t9.sound.SoundHandler;
import v9t9.vdp.VdpHandler;

/** The client the emulated machine interacts with.  This could
 * be the emulator itself, hosting a window, keyboard, etc., 
 * or it could be a demo running, or it could be a remote host.
 * 
 * @author ejs
 */
public interface Client {
    void close();
    
    VdpHandler getVideoHandler();
    void setVideoHandler(VdpHandler video);

    SoundHandler getSoundHandler();
    void setSoundHandler(SoundHandler handler);
    
    void timerTick();
}
