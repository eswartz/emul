/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.emulator.clients.demo;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import v9t9.emulator.Machine;
import v9t9.emulator.handlers.CruHandler;
import v9t9.emulator.handlers.SoundHandler;
import v9t9.emulator.handlers.VdpHandler;
import v9t9.engine.Client;

/**
 * @author ejs
 */
public class CopyOfDemoClient implements Client, VdpHandler, SoundHandler, CruHandler {
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
    private Timer timer;
    private TimerTask cruTask;

    
    
    /** Construct the client as a demo running in a different V9t9 */
    public CopyOfDemoClient(Machine machine) {
        this.machine = machine;
        video = this;
        sound = this;
        cru = this;
        
        /* start a timer thread */
        timer = new Timer();

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
                // Invoke v9t9
                client = Runtime.getRuntime().exec(
                        new String[] { "/usr/local/src/V9t9/source/v9t9",
                             //   "Log Demo 4", "Log Keyboard 2",
                                //"ListenDemo " + connection.getRemoteString()
                                "ListenDemo2 " + connection.getRemoteReaderString() 
                                + " " + connection.getRemoteWriterString() 
                                });
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

        cruReadMap = new HashMap<Integer, Integer>();
        cruWriteMap = new HashMap<Integer, Integer>();
        cruToRead = new HashSet<Integer>();
        cruTask = new TimerTask() {
            @Override
			public void run() {
                refreshCru();
            } 
        };
        timer.scheduleAtFixedRate(cruTask, 0, 1000 / 60);
        
        vdpPacket = new byte[256];
        vdpPacketStart = 0;
        vdpPacketSize = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#close()
     */
    public void close() {
        try {
            if (cruTask != null) {
                cruTask.cancel();
                cruTask = null;
            }
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            connection.close();
            client.destroy();
        } catch (IOException e) {
            e.printStackTrace();
            machine.stop();
        }
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
            flushVdp();
            byte[] values = { DEMO_TYPE_VIDEO, 0x02, 0x00, val,
                    (byte) (0x80 | reg) };
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
     * @see vdp.Handler#readStatus()
     */
    public byte readVdpStatus() {
        flushVdp();
        return 0;
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
    }

    public void refresh(boolean redrawAll) {
        flushVdp();
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#getVideo()
     */
    public v9t9.emulator.handlers.VdpHandler getVideoHandler() {
        // TODO Auto-generated method stub
        return video;
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#setVideo(vdp.Handler)
     */
    public void setVideoHandler(v9t9.emulator.handlers.VdpHandler video) {
        // TODO Auto-generated method stub
        this.video = video;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
	protected void finalize() throws Throwable {
        // TODO Auto-generated method stub
        super.finalize();
        close();
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
        }
    }

    /** Map of a single address to single bit values */
    Map<Integer, Integer> cruReadMap;
    Map<Integer, Integer> cruWriteMap;
    /** Map of CRU values we need to refresh */
    Set<Integer> cruToRead;
    boolean cruChanged;
    
    /** Refresh the CRU */
    protected synchronized void refreshCru() {
        if (cruChanged) {
            for (Entry<Integer, Integer> entry : cruWriteMap.entrySet()) {
                sendWriteBitsCommand(((Integer)entry.getKey()).intValue(), ((Integer)entry.getValue()).intValue(), 1);
            }
            cruWriteMap.clear();
            cruChanged = false;
        }
        if (cruToRead.size() > 0) {
            for (Integer addr : cruToRead) {
                int val = sendReadBitsCommand(addr.intValue(), 1);
                cruReadMap.put(addr, new Integer(val));
            }
            
            cruToRead.clear();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Cru#writeBits(int, int, int)
     */
    public synchronized void writeBits(int addr, int val, int num) {
        while (num > 0) {
            Integer iAddr = new Integer(addr);
            if (cruWriteMap.containsKey(iAddr) || cruToRead.contains(iAddr)) {
                // oops, need to flush the old value (and all others while we're at it)
                refreshCru();
                sendWriteBitsCommand(addr, val, num);
                return;
            }
            cruWriteMap.put(iAddr, new Integer(val & 0x1));
            val >>= 1;
            addr += 2;
            num--;
            cruChanged = true;
        }
    }

    private synchronized void sendWriteBitsCommand(int addr, int val, int num) {
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

    public synchronized int readBits(int addr, int num) {
        int val = 0;
        int mask = 1;
        while (num > 0) {
            Integer iAddr = new Integer(addr);
            Integer value = cruReadMap.get(iAddr);
            if (value == null || cruWriteMap.containsKey(iAddr)) {
                // oops, need to read it now
                refreshCru();
                refreshReadBits(addr, num);
            } else {
                // read slightly old value
                if (value.intValue() != 0) {
					val |= mask;
				}
                
                // note we need to refresh this soon
                // TODO: per-bit info about how often it changes
                cruToRead.add(iAddr);
                
                mask <<= 1;
                addr += 2;
                num--;
            }
        }

        return val;
    }
    
    
    private void refreshReadBits(int addr, int num) {
        int val = sendReadBitsCommand(addr, num);
        while (num > 0) {
            cruReadMap.put(new Integer(addr), new Integer(val & 0x1));
            val >>= 1;
            addr += 2;
            num--;
        }
    }

    private synchronized int sendReadBitsCommand(int addr, int num) {
        
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

}

