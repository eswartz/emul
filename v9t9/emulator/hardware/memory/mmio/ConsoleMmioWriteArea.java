/**
 * 
 */
package v9t9.emulator.hardware.memory.mmio;

import v9t9.emulator.Machine.ConsoleMmioWriter;
import v9t9.engine.memory.MemoryArea;

public class ConsoleMmioWriteArea extends ConsoleMmioArea {
    protected final ConsoleMmioWriter writer;

	ConsoleMmioWriteArea(ConsoleMmioWriter writer) {
        this.writer = writer;
		if (writer == null) {
			throw new NullPointerException();
		}
        
        areaWriteByte = new AreaWriteByte() {
            public void writeByte(MemoryArea area, int addr, byte val) {
                //System.out.println("wrote addr " + Integer.toHexString(addr)
                // + "="
                //  + Integer.toHexString(val));
                if (0 == (addr & 1)) {
                    ConsoleMmioWriteArea.this.writer.write(addr, val);
                }
            }
        };
    };
}