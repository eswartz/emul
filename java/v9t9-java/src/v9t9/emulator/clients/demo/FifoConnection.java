/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Aug 27, 2005
 *
 */
package v9t9.emulator.clients.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FifoConnection implements Connection {
    String rfifoName, wfifoName;
    File rfifo, wfifo;
    InputStream in_;
    OutputStream out_;
    int nReads, nWrites;
    long millis;
    
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
        if (!rfifo.exists()) {
			throw new IOException("cannot create fifo");
		}
        wfifo = new File(wfifoName);
        if (!wfifo.exists()) {
			throw new IOException("cannot create fifo");
		}
    }

    public void connect() throws IOException {
        out_ = new FileOutputStream(wfifo);
        in_ = new FileInputStream(rfifo);
    }

    public synchronized void close() throws IOException {
        in_.close();
        out_.close();
        rfifo.delete();
        wfifo.delete();
        
        System.out.println("# reads: " + nReads + "\n# writes: " + nWrites + "\ntime: " + millis);
    }

    public synchronized void write(byte[] bytes, int offset, int length) throws IOException {
        long start = System.currentTimeMillis();
        nWrites++;
        out_.write(bytes, offset, length);
        out_.flush();
        millis += System.currentTimeMillis() - start;
    }

    public synchronized void write(byte[] bytes) throws IOException  {
        long start = System.currentTimeMillis();
        nWrites++;
        out_.write(bytes, 0, bytes.length);
        out_.flush();
        millis += System.currentTimeMillis() - start;
    }

    public synchronized void read(byte[] bytes, int offset, int length) throws IOException {
        long start = System.currentTimeMillis();
        nReads++;
        in_.read(bytes, offset, length);
        millis += System.currentTimeMillis() - start;
    }

    public synchronized void read(byte[] bytes) throws IOException  {
        long start = System.currentTimeMillis();
        nReads++;
        in_.read(bytes, 0, bytes.length);
        millis += System.currentTimeMillis() - start;
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