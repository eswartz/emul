/**
 * 
 */
package v9t9.emulator.hardware.memory.mmio;

import v9t9.emulator.Machine.ConsoleMmioReader;
import v9t9.engine.memory.MemoryArea;

public class ConsoleMmioReadArea extends ConsoleMmioArea {
    public ConsoleMmioReadArea(final ConsoleMmioReader reader) {
        if (reader == null) {
			throw new NullPointerException();
		}

        areaReadByte = new AreaReadByte() {
            public byte readByte(MemoryArea area, int addr) {
                //System.out.println("read byte from "
                //      + Integer.toHexString(addr));
                if (0 == (addr & 1)) {
					return reader.read(addr & 2);
				} else {
					return 0;
				}
            }
        };
    }
}