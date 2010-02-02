/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.emulator.Machine.ConsoleMmioReader;
import v9t9.emulator.Machine.ConsoleMmioWriter;

public class DummyConsoleMmioHandler implements ConsoleMmioReader,
        ConsoleMmioWriter {

    public byte read(int addrMask) {
        return 0;
    }

    public void write(int addrMask, byte val) {

    }
}