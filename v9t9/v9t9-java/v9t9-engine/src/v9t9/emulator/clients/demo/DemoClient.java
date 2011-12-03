/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.emulator.clients.demo;

import java.io.IOException;


import v9t9.base.settings.ISettingSection;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyEvent;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.memory.MemoryDomain;
import v9t9.engine.client.IClient;
import v9t9.engine.client.IKeyboardHandler;
import v9t9.engine.client.ISoundHandler;
import v9t9.engine.client.IVideoRenderer;
import v9t9.engine.events.BaseEventNotifier;
import v9t9.engine.hardware.ICruChip;
import v9t9.engine.hardware.VdpChip;
import v9t9.engine.machine.IMachine;
import v9t9.engine.memory.VdpMmio;
import v9t9.engine.sound.SoundVoice;
import v9t9.engine.video.VdpCanvas;
import v9t9.engine.video.VdpModeRedrawHandler;

/**
 * @author ejs
 */
public class DemoClient implements IClient, VdpChip, ISoundHandler, ICruChip {
	String ID = "Demo";
	
    VdpChip video;
    ISoundHandler sound;
    ICruChip cru;
    
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

    private IMachine machine;

    Connection connection;
	private boolean isAlive;
	private IEventNotifier eventNotifier;

    /** Construct the client as a demo running in a different TI994A */
    public DemoClient(IMachine machine) {
        this.machine = machine;
        video = this;
        sound = this;
        cru = this;
        
        eventNotifier = new BaseEventNotifier() {
        	{
        		startConsumerThread();
        	}
        	/* (non-Javadoc)
        	 * @see v9t9.emulator.BaseEventNotifier#consumeEvent(v9t9.emulator.clients.builtin.IEventNotifier.NotifyEvent)
        	 */
        	@Override
        	protected void consumeEvent(NotifyEvent event) {
        		event.print(System.out);
			}
		};
		
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
        
        isAlive = true;
        
        vdpPacket = new byte[256];
        vdpPacketStart = 0;
        vdpPacketSize = 0;
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.Client#getIdentifier()
     */
    @Override
    public String getIdentifier() {
    	return ID;
    }
    /* (non-Javadoc)
     * @see v9t9.engine.Client#getEventNotifier()
     */
    @Override
    public IEventNotifier getEventNotifier() {
    	return eventNotifier;
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
            isAlive = false;
        }

    }

    /** Send VDP register update */
    public void writeVdpReg(int reg, byte val) {
        try {
            //flushVdp();
            byte[] values = { DEMO_TYPE_VIDEO, 0x02, 0x00, val,
                    (byte) (0x80 | reg) };
            connection.write(values);
            //in.read(values, 0, 1);
        } catch (IOException e) {
            e.printStackTrace();
            machine.stop();
            isAlive = false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see vdp.Handler#readStatus()
     */
    public byte readVdpStatus() {
        //flushVdp();
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vdp.Handler#writeVal(short, byte)
     */
    public void touchAbsoluteVdpMemory(int vdpaddr, byte val) {
        if (vdpPacketSize >= 255
                || (vdpaddr & 0x3fff) != vdpPacketStart + vdpPacketSize) {
            flushVdp();
            vdpPacketStart = vdpaddr & 0x3fff;
        }
        vdpPacket[vdpPacketSize++] = val;
    }
    
    public boolean update() {
    	return false;
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
            isAlive = false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#getVideo()
     */
    public v9t9.engine.hardware.VdpChip getVideoHandler() {
        return video;
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#setVideo(vdp.Handler)
     */
    public void setVideoHandler(v9t9.engine.hardware.VdpChip video) {
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
        } catch (IOException e) {
            e.printStackTrace();
            machine.stop();
            isAlive = false;
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
            isAlive = false;
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
            e.printStackTrace();
            machine.stop();
            isAlive = false;
        }
        
        return  0;
        
        //return (new Random()).nextInt((1<<(num-1)));
    }

    /* (non-Javadoc)
     * @see v9t9.Client#getSound()
     */
    public ISoundHandler getSoundHandler() {
        return sound;
    }

    /*
     *  (non-Javadoc)
     * @see v9t9.Client#setSoundHandler(v9t9.sound.SoundHandler)
     */
    public void setSoundHandler(ISoundHandler handler) {
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
            isAlive = false;
        }
    }
    
    public ICruChip getCruHandler() {
        return cru;
    }
    
    public void setCruHandler(ICruChip handler) {
        this.cru = handler;
    }

    public void handleEvents() {
    	
    }
    
    public boolean isAlive() {
    	return isAlive;
    }
    
    public VdpMmio getVdpMmio() {
    	return video.getVdpMmio();
    }
    
    public byte readAbsoluteVdpMemory(int vdpaddr) {
    	return video.readAbsoluteVdpMemory(vdpaddr);
    }
    
    public void writeAbsoluteVdpMemory(int vdpaddr, byte byt) {
    	video.writeAbsoluteVdpMemory(vdpaddr, byt);
    }
    
    public ByteMemoryAccess getByteReadMemoryAccess(int vdpaddr) {
    	return video.getByteReadMemoryAccess(vdpaddr);
    }
    
    public byte readVdpReg(int reg) {
    	return video.readVdpReg(reg);
    }
    
    public MemoryDomain getVideoMemory() {
    	return video.getVideoMemory();
    }
    
    public void setVdpMmio(VdpMmio mmio) {
    	video.setVdpMmio(mmio);
    }
    
    public void tick() {
    	video.tick();
    	
    }
    
    public boolean isThrottled() {
    	return video.isThrottled();
    }
    public void updateVideo() {
    }
    public void work() {
    	video.work();
    }
    
    public void setCanvas(VdpCanvas canvas) {
    	video.setCanvas(canvas);
    }
    
    public VdpCanvas getCanvas() {
    	return video.getCanvas();
    }
    public IKeyboardHandler getKeyboardHandler() {
    	return null;
    }
    
    public void loadState(ISettingSection section) {
    	// TODO Auto-generated method stub
    	
    }
    
    public void saveState(ISettingSection section) {
    	// TODO Auto-generated method stub
    	
    }

	/**
	 * @param vn  
	 * @param v 
	 */
	public void updateVoice(int vn, SoundVoice v) {
		// TODO Auto-generated method stub
		
	}
	
	public void updateSound() {
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void generateSound(int vn, int updateFlags) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param bit  
	 * @param pos 
	 * @param total 
	 */
	public void audioGate(int bit, int pos, int total) {
		// TODO Auto-generated method stub
		
	}

	public void flush() {
		// TODO Auto-generated method stub
		
	}

	public void addCpuCycles(int cycles) {
		// TODO Auto-generated method stub
		
	}
	public void flushAudio(int pos, int total) {
		// TODO Auto-generated method stub
		
	}
	public void speech(short sample) {
		// TODO Auto-generated method stub
		
	}
	public void syncVdpInterrupt(IMachine machine) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.VdpHandler#getVdpModeRedrawHandler()
	 */
	@Override
	public VdpModeRedrawHandler getVdpModeRedrawHandler() {
		return video.getVdpModeRedrawHandler();
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.VdpHandler#getRegisterCount()
	 */
	@Override
	public int getRegisterCount() {
		return video.getRegisterCount();
	}
	
	public byte getRegister(int reg) {
		return video.getRegister(reg);
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.VdpHandler#setRegister(int, byte)
	 */
	@Override
	public void setRegister(int reg, byte value) {
		video.setRegister(reg, value);
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.VdpHandler#getRegisterName(int)
	 */
	@Override
	public String getRegisterName(int reg) {
		return video.getRegisterName(reg);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.VdpHandler#getRegisterTooltip(int)
	 */
	@Override
	public String getRegisterTooltip(int reg) {
		return video.getRegisterTooltip(reg);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.Client#getVideoRenderer()
	 */
	@Override
	public IVideoRenderer getVideoRenderer() {
		return null;
	}
}


