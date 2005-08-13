/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9;

/** The client the emulated machine interacts with.  This could
 * be the emulator itself, hosting a window, keyboard, etc., 
 * or it could be a demo running, or it could be a remote host.
 * 
 * @author ejs
 */
public abstract class Client {
    abstract void close();
    
    abstract public v9t9.vdp.Handler getVideo();
    abstract public void setVideo(v9t9.vdp.Handler video);

    abstract public void timerTick();

    abstract public sound.Handler getSound();
}
