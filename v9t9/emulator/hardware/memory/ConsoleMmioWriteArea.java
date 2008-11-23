/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.emulator.Machine.ConsoleMmioWriter;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.ZeroWordMemoryArea;

public class ConsoleMmioWriteArea extends ConsoleMemoryArea {
    ConsoleMmioWriteArea(final ConsoleMmioWriter writer) {
        if (writer == null) {
			throw new NullPointerException();
		}

        memory = ZeroWordMemoryArea.zeroes;

        areaWriteByte = new AreaWriteByte() {
            public void writeByte(MemoryArea area, int addr, byte val) {
                //System.out.println("wrote addr " + Integer.toHexString(addr)
                // + "="
                //  + Integer.toHexString(val));
                if (0 == (addr & 1)) {
                    writer.write((addr & 2), val);
                }
            }
        };
    };
}