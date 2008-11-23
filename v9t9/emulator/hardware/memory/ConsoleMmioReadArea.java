/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.emulator.Machine.ConsoleMmioReader;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.ZeroWordMemoryArea;

public class ConsoleMmioReadArea extends ConsoleMemoryArea {
    public ConsoleMmioReadArea(final ConsoleMmioReader reader) {
        if (reader == null) {
			throw new NullPointerException();
		}

        memory = ZeroWordMemoryArea.zeroes;

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