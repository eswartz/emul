/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Aug 27, 2005
 *
 */
package v9t9.emulator.clients.demo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class SocketConnection implements Connection {
    ServerSocket server;

    Socket socket;

    public SocketConnection() throws IOException {
        /* make a server */
        server = new ServerSocket(0);
        server.setReuseAddress(true);
        // not in Classpath
        //server.setPerformancePreferences(0, 1, 2);

    }

    public void connect() throws IOException {
        socket = server.accept();
    }

    public synchronized void close() throws IOException {
        socket.close();
        server.close();
    }

    public synchronized void write(byte[] bytes, int offset, int length) throws IOException {
        socket.getOutputStream().write(bytes, offset, length);
    }

    public synchronized void write(byte[] bytes) throws IOException  {
        socket.getOutputStream().write(bytes, 0, bytes.length);
    }

    public synchronized void read(byte[] bytes, int offset, int length) throws IOException {
        socket.getInputStream().read(bytes, offset, length);
    }

    public synchronized void read(byte[] bytes) throws IOException  {
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