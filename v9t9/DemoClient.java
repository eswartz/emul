/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9;

import java.io.*;
import java.net.*;
import java.util.Random;

import sound.Handler;

/**
 * @author ejs
 */
public class DemoClient extends Client implements v9t9.vdp.Handler, sound.Handler, Cru {
    v9t9.vdp.Handler video;
    sound.Handler sound;
    
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

    static final int demo_type_tick = 0;

    static final int demo_type_video = 1;

    static final int demo_type_sound = 2;

    static final int demo_type_speech = 3;

    static final int demo_type_cru_write = 4;

    static final int demo_type_cru_read = 5;

    Process client;

    int vdpPacketSize;

    int vdpPacketStart;

    byte[] vdpPacket;

    private Machine machine;

    Connection connection;

    interface Connection {
        public void connect() throws IOException;

        public void close() throws IOException;

        public String getRemoteString();
        public String getRemoteReaderString();
        public String getRemoteWriterString();
        
        public void write(byte[] bytes, int offset, int length) throws IOException;
        public void write(byte[] bytes) throws IOException;
        public void read(byte[] bytes, int offset, int length) throws IOException;
        public void read(byte[] bytes) throws IOException;
        
    }    


    class SocketConnection implements Connection {
        ServerSocket server;

        Socket socket;

        public SocketConnection() throws IOException {
            /* make a server */
            server = new ServerSocket(0);
            server.setReuseAddress(true);
            server.setPerformancePreferences(0, 1, 2);

        }

        public void connect() throws IOException {
            socket = server.accept();
        }

        public void close() throws IOException {
            socket.close();
            server.close();
        }

        public void write(byte[] bytes, int offset, int length) throws IOException {
            socket.getOutputStream().write(bytes, offset, length);
        }

        public void write(byte[] bytes) throws IOException  {
            socket.getOutputStream().write(bytes, 0, bytes.length);
        }

        public void read(byte[] bytes, int offset, int length) throws IOException {
            socket.getInputStream().read(bytes, offset, length);
        }

        public void read(byte[] bytes) throws IOException  {
            socket.getInputStream().read(bytes, 0, bytes.length);
        }

        public String getRemoteString() {
            return Integer.toString(server.getLocalPort());            
        }
        public String getRemoteReaderString() {
            return Integer.toString(server.getLocalPort());            
        }
        public String getRemoteWriterString() {
            return Integer.toString(server.getLocalPort());            
        }
    }

    /* The FIFO is much faster than a socket since it doesn't incur
     * the Nagle algorithm.
     * @author ejs
     */
    class FifoConnection implements Connection {
        String rfifoName, wfifoName;
        File rfifo, wfifo;
        InputStream in_;
        OutputStream out_;
        
        public FifoConnection() throws IOException {
            /* make a fifo */
            rfifoName = "/tmp/r_" + Runtime.getRuntime().toString();
            Process client_ = Runtime.getRuntime().exec(
                    new String[] { "/usr/bin/mkfifo",
                    rfifoName });
            try {
                client_.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new IOException("cannot run mkfifo");
            }
            client_.destroy();

            wfifoName = "/tmp/w_" + Runtime.getRuntime().toString();
            client_ = Runtime.getRuntime().exec(
                    new String[] { "/usr/bin/mkfifo",
                    wfifoName });
            try {
                client_.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new IOException("cannot run mkfifo");
            }
            client_.destroy();

            rfifo = new File(rfifoName);
            if (!rfifo.exists())
                throw new IOException("cannot create fifo");
            wfifo = new File(wfifoName);
            if (!wfifo.exists())
                throw new IOException("cannot create fifo");
        }

        public void connect() throws IOException {
            out_ = new FileOutputStream(wfifo);
            in_ = new FileInputStream(rfifo);
        }

        public void close() throws IOException {
            in_.close();
            out_.close();
            rfifo.delete();
            wfifo.delete();
        }

        public void write(byte[] bytes, int offset, int length) throws IOException {
            out_.write(bytes, offset, length);
            out_.flush();
        }

        public void write(byte[] bytes) throws IOException  {
            out_.write(bytes, 0, bytes.length);
            out_.flush();
        }

        public void read(byte[] bytes, int offset, int length) throws IOException {
            in_.read(bytes, offset, length);
        }

        public void read(byte[] bytes) throws IOException  {
            in_.read(bytes, 0, bytes.length);
        }

        public String getRemoteString() {
            return wfifoName;            
        }
        public String getRemoteReaderString() {
            return wfifoName;            
        }
        public String getRemoteWriterString() {
            return rfifoName;            
        }
    }

    /** Construct the client as a demo running in a different V9t9 */
    public DemoClient(Machine machine) {
        this.machine = machine;
        video = this;
        sound = this;
        
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

        vdpPacket = new byte[256];
        vdpPacketStart = 0;
        vdpPacketSize = 0;
    }

    private void flushVdp() {
        if (vdpPacketSize == 0)
            return;

        byte[] header = { demo_type_video, 0, 0, 0, 0, 0 };
        header[1] = (byte) (vdpPacketSize + 3);
        header[2] = (byte) ((vdpPacketSize + 3) >> 8);
        header[3] = (byte) (vdpPacketStart);
        header[4] = (byte) ((vdpPacketStart >> 8) | 0x40);
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
    public void writeVdpReg(byte reg, byte val, byte old) {
        try {
            flushVdp();
            byte[] values = { demo_type_video, 0x02, 0x00, (byte) val,
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
            vdpPacketStart = (vdpaddr & 0x3fff);
        }
        vdpPacket[vdpPacketSize++] = val;
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#close()
     */
    void close() {
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
    public v9t9.vdp.Handler getVideo() {
        // TODO Auto-generated method stub
        return video;
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#setVideo(vdp.Handler)
     */
    public void setVideo(v9t9.vdp.Handler video) {
        // TODO Auto-generated method stub
        this.video = video;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
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
    public void timerTick() {
        try {
            flushVdp();
            byte[] values = { demo_type_tick };
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
     * @see v9t9.Cru#writeBits(int, int, int)
     */
    public void writeBits(int addr, int val, int num) {
        try {
            byte[] values = { demo_type_cru_write, (byte) addr,
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
            byte[] values = { demo_type_cru_read, (byte) addr,
                    (byte) (addr >> 8), (byte) num, 0, 0 };
            connection.write(values, 0, 4);
            //in.read(values, 0, 1);

            // response
            connection.read(values, 0, 6);
            if (values[0] != demo_type_cru_write || values[1] != (byte) addr
                    || values[2] != (byte) (addr >> 8)
                    || values[3] != (byte) num)
                throw new AssertionError("bad CRU protocol");

            int value = (values[4] & 0xff) | ((values[5] & 0xff) << 8);
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
    public Handler getSound() {
        return sound;
    }

    /* (non-Javadoc)
     * @see sound.Handler#writeSound(byte)
     */
    public void writeSound(byte val) {
        try {
            byte[] values = { demo_type_sound, 0x01, 0x00, (byte) val };
            connection.write(values);
        } catch (IOException e) {
            e.printStackTrace();
            machine.stop();
        }
    }
}

