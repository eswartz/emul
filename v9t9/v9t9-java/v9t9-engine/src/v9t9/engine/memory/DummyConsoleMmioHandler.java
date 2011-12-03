/**
 * 
 */
package v9t9.engine.memory;


public class DummyConsoleMmioHandler implements IConsoleMmioReader,
        IConsoleMmioWriter {

    public byte read(int addrMask) {
        return 0;
    }

    public void write(int addrMask, byte val) {

    }
}