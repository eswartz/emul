/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.emulator.clients.builtin;

import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.emulator.clients.builtin.video.ImageDataCanvas24Bit;
import v9t9.emulator.clients.builtin.video.InternalVdp;
import v9t9.emulator.clients.builtin.video.SwtVideoRenderer;
import v9t9.emulator.clients.builtin.video.VideoRenderer;
import v9t9.engine.Client;
import v9t9.engine.CruHandler;
import v9t9.engine.KeyboardHandler;
import v9t9.engine.SoundHandler;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.MemoryDomain;
import v9t9.keyboard.KeyboardState;

/**
 * This client does all its own dang work!
 * @author ejs
 */
public class PureJavaClient implements Client {
    VdpHandler video;
    CruHandler cru;
    private Machine machine;
	private SoundHandler sound;
	private KeyboardState keyboardState;
	private KeyboardHandler keyboardHandler;
	private VideoRenderer videoRenderer;

    public PureJavaClient(final Machine machine, MemoryDomain videoMemory, Display display) {
    	
        this.machine = machine;
        
        ImageDataCanvas canvas = new ImageDataCanvas24Bit();
        videoRenderer = new SwtVideoRenderer(display, canvas);
        video = new InternalVdp(videoMemory, canvas);
        
        ((SwtVideoRenderer) videoRenderer).getShell().addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
		        if (machine.isRunning()) {
		        	machine.setNotRunning();
		        }
			}
		});

        keyboardState = new KeyboardState();
        cru = new InternalCru(machine, keyboardState);
        
        sound = new SoundHandler() {

			public void writeSound(byte val) {
				
			}
        	
        };
        
        keyboardHandler = new SwtKeyboardHandler(((SwtVideoRenderer) videoRenderer).getShell(), keyboardState);
    }

	/** Send VDP register update */
    public void writeVdpReg(byte reg, byte val) {
    	video.writeVdpReg(reg, val);
    }

    /*
     * (non-Javadoc)
     * 
     * @see vdp.Handler#readStatus()
     */
    public byte readVdpStatus() {
    	return video.readVdpStatus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see vdp.Handler#writeVal(short, byte)
     */
    public void writeVdpMemory(short vdpaddr, byte val) {
        video.writeVdpMemory(vdpaddr, val);
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#close()
     */
    public void close() {
    	machine.stop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#getVideo()
     */
    public v9t9.engine.VdpHandler getVideoHandler() {
        return video;
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#setVideo(vdp.Handler)
     */
    public void setVideoHandler(v9t9.engine.VdpHandler video) {
        this.video = video;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
	protected void finalize() throws Throwable {
    	close();
    	super.finalize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#timerTick()
     */
    public void timerInterrupt() {
    	video.update();
    	videoRenderer.redraw();
    	keyboardHandler.scan(keyboardState);
    	//video.update();
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Cru#writeBits(int, int, int)
     */
    public void writeBits(int addr, int val, int num) {
    	cru.writeBits(addr, val, num);
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Cru#readBits(int, int)
     */
    public int readBits(int addr, int num) {
        return cru.readBits(addr, num);
    }

    /* (non-Javadoc)
     * @see v9t9.Client#getSound()
     */
    public SoundHandler getSoundHandler() {
        return sound;
    }

    /*
     *  (non-Javadoc)
     * @see v9t9.Client#setSoundHandler(v9t9.sound.SoundHandler)
     */
    public void setSoundHandler(SoundHandler handler) {
        this.sound = handler;
    }
    
    /* (non-Javadoc)
     * @see sound.Handler#writeSound(byte)
     */
    public void writeSound(byte val) {
    	if (sound != null)
    		sound.writeSound(val);
    }
    
    public CruHandler getCruHandler() {
        return cru;
    }
    
    public void setCruHandler(CruHandler handler) {
        this.cru = handler;
    }
}

