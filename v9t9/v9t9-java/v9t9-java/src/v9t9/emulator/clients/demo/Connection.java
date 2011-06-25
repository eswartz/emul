/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Aug 27, 2005
 *
 */
package v9t9.emulator.clients.demo;

import java.io.IOException;

public interface Connection {
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