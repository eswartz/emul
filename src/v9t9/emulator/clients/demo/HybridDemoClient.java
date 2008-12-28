/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.emulator.clients.demo;

import java.io.IOException;

import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.SwtWindow;
import v9t9.emulator.clients.builtin.video.SwtVideoRenderer;
import v9t9.emulator.clients.builtin.video.VideoRenderer;
import v9t9.engine.Client;
import v9t9.engine.CruHandler;
import v9t9.engine.KeyboardHandler;
import v9t9.engine.SoundHandler;
import v9t9.engine.VdpHandler;

/**
 * This demo client writes everything to the v9t9 client running a demo,
 * and also mirrors VDP to the builtin handler.
 * @author ejs
 */
public class HybridDemoClient implements Client, SoundHandler, CruHandler {
	VdpHandler video;
    SoundHandler sound;
    CruHandler cru;
    
    /*
     * The demo file format is very rudimentary.
     * 
     * Header: 'V910' bytes
     * 
     * Followed by a list of sections for various demo_types. Each section
     * starts with one byte (demo_type) and is followed by nothing (for the
     * timer) or by a buffer length (little-endian, 16 bits) which is passed to
     * the event handler.
     * 
     * Video has 16-bit little-endian addresses followed (if the address does
     * not have the 0x8000 bit set, which is a register write) by a 16-bit
     * little-endian length and data bytes.
     * 
     * Sound has a series of data bytes.
     * 
     * Speech has a series of demo_speech_event bytes, and the
     * demo_speech_adding_byte event is followed by that byte.
     * 
     * We add commands for CRU manipulation to hack this interface for now
     */

    static final int DEMO_TYPE_TICK = 0;

    static final int DEMO_TYPE_VIDEO = 1;

    static final int DEMO_TYPE_SOUND = 2;

    static final int DEMO_TYPE_SPEECH = 3;

    static final int DEMO_TYPE_CRU_WRITE = 4;

    static final int DEMO_TYPE_CRU_READ = 5;

    Process client;

    int vdpPacketSize;

    int vdpPacketStart;

    byte[] vdpPacket;

    private Machine machine;

    Connection connection;
	private VideoRenderer videoRenderer;
	private SwtWindow window;

    /** Construct the client as a demo running in a different TI994A 
     * @param display */
    public HybridDemoClient(final Machine machine, VdpHandler vdp, Display display) {
    	
        this.machine = machine;
       
		videoRenderer = new SwtVideoRenderer();

        window = new SwtWindow(display, (SwtVideoRenderer) videoRenderer, machine);

        video.setCanvas(videoRenderer.getCanvas());
        video = vdp;
        
        window.getShell().addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
		        if (machine.isRunning()) {
		        	machine.setNotRunning();
		        }
			}
		});

        sound = this;
        cru = this;
        
        try {
            //connection = new SocketConnection();
            connection = new FifoConnection();
        } catch (IOException e2) {
            e2.printStackTrace();
            System.exit(1);
        }

        if (false) {
            System.out.println("connect to port " + connection.getRemoteReaderString()
                    	+ " " + connection.getRemoteWriterString());
        } else {
            try {
            	System.out.println("running TI994A and waiting for connection");
                // Invoke v9t9
                client = Runtime.getRuntime().exec(
                        new String[] { "/usr/local/src/TI994A/source/v9t9",
                             //   "Log Demo 4", "Log Keyboard 2",
                                //"ListenDemoPort " + connection.getRemoteString()
                                "ListenDemoFifo " + connection.getRemoteReaderString() 
                                + " " + connection.getRemoteWriterString() 
                                });
                try {
                	client.exitValue();
                	throw new IOException("Cannot find executable");
                } catch (IllegalThreadStateException e) {
                }
            } catch (IOException e1) {
                e1.printStackTrace();
                System.exit(1);
            }
        }

        /* connect */
        try {
            connection.connect();
        } catch (IOException e) {
            System.err.println("Couldn't accept");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            /* send header */
            byte header[] = { 'V', '9', '1', '0' };
            connection.write(header);

        } catch (IOException e) {
            System.err.println("Couldn't accept connection");
            System.exit(1);
        }
        
        vdpPacket = new byte[256];
        vdpPacketStart = 0;
        vdpPacketSize = 0;
    }

    private void flushVdp() {
        if (vdpPacketSize == 0) {
			return;
		}

        byte[] header = { DEMO_TYPE_VIDEO, 0, 0, 0, 0, 0 };
        header[1] = (byte) (vdpPacketSize + 3);
        header[2] = (byte) (vdpPacketSize + 3 >> 8);
        header[3] = (byte) vdpPacketStart;
        header[4] = (byte) (vdpPacketStart >> 8 | 0x40);
        header[5] = (byte) vdpPacketSize;

        try {
            connection.write(header);
            connection.write(vdpPacket, 0, vdpPacketSize);
            //in.read(header, 0, 1);
            vdpPacketSize = 0;
            vdpPacketStart = 0;
        } catch (IOException e) {
            e.printStackTrace();
            machine.stop();
        }
   }

    /** Send VDP register update */
    public void writeVdpReg(byte reg, byte val) {
        try {
            //flushVdp();
            byte[] values = { DEMO_TYPE_VIDEO, 0x02, 0x00, val,
                    (byte) (0x80 | reg) };
            connection.write(values);
            
            video.writeVdpReg(reg, val);
            //in.read(values, 0, 1);
        } catch (IOException e) {
            e.printStackTrace();
            machine.stop();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see vdp.Handler#readStatus()
     */
    public byte readVdpStatus() {
        //flushVdp();
    	return video.readVdpStatus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see vdp.Handler#writeVal(short, byte)
     */
    public void writeVdpMemory(short vdpaddr, byte val) {
        if (vdpPacketSize >= 255
                || (vdpaddr & 0x3fff) != vdpPacketStart + vdpPacketSize) {
            flushVdp();
            vdpPacketStart = vdpaddr & 0x3fff;
        }
        vdpPacket[vdpPacketSize++] = val;
        video.touchAbsoluteVdpMemory(vdpaddr, val);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#close()
     */
    public void close() {
        try {
            connection.close();
            client.destroy();
        } catch (IOException e) {
            e.printStackTrace();
            machine.stop();
        }
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
    	if (client != null) {
    		client.destroy();
    	}
    	close();
    	super.finalize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#timerTick()
     */
    public void timerInterrupt() {
        try {
            flushVdp();
            byte[] values = { DEMO_TYPE_TICK };
            connection.write(values);
            //in.read(values, 0, 1);
            video.update();
            videoRenderer.redraw();
     
        } catch (IOException e) {
            e.printStackTrace();
            machine.stop();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Cru#writeBits(int, int, int)
     */
    public void writeBits(int addr, int val, int num) {
        try {
            byte[] values = { DEMO_TYPE_CRU_WRITE, (byte) addr,
                    (byte) (addr >> 8), (byte) num, (byte) val,
                    (byte) (val >> 8) };
            connection.write(values);
            //in.read(values, 0, 1);
        } catch (IOException e) {
            e.printStackTrace();
            machine.stop();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Cru#readBits(int, int)
     */
    public int readBits(int addr, int num) {
        
        try {
            byte[] values = { DEMO_TYPE_CRU_READ, (byte) addr,
                    (byte) (addr >> 8), (byte) num, 0, 0 };
            connection.write(values, 0, 4);
            //in.read(values, 0, 1);

            // response
            connection.read(values, 0, 6);
            if (values[0] != DEMO_TYPE_CRU_WRITE || values[1] != (byte) addr
                    || values[2] != (byte) (addr >> 8)
                    || values[3] != (byte) num) {
                throw new AssertionError("bad CRU protocol");
            }

            int value = values[4] & 0xff | (values[5] & 0xff) << 8;
            return value;

        } catch (IOException e) {
            //e.printStackTrace();
            machine.stop();
        }
        
        return  0;
        
        //return (new Random()).nextInt((1<<(num-1)));
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
        try {
            byte[] values = { DEMO_TYPE_SOUND, 0x01, 0x00, val };
            connection.write(values);
        } catch (IOException e) {
            e.printStackTrace();
            machine.stop();
        }
    }
    
    public CruHandler getCruHandler() {
        return cru;
    }
    
    public void setCruHandler(CruHandler handler) {
        this.cru = handler;
    }

    public void handleEvents() {
    	Shell shell = window.getShell();
    	while (shell.getDisplay().readAndDispatch()) ;
    }
    
    public boolean isAlive() {
    	return !window.getShell().isDisposed();
    }
    
    public void updateVideo() {
    	// TODO Auto-generated method stub
    	
    }
    
    public KeyboardHandler getKeyboardHandler() {
    	return null;
    }

	public void updateVoice(int vn, int updateFlags) {
		// TODO Auto-generated method stub
		
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void audioGate(int bit, int pos, int total) {
		// TODO Auto-generated method stub
		
	}

	public void flush() {
		// TODO Auto-generated method stub
		
	}
    
}

