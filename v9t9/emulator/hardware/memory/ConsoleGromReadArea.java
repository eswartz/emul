/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.engine.memory.Gpl;


public class ConsoleGromReadArea extends ConsoleMmioReadArea {
    public ConsoleGromReadArea(Gpl mmio) {
        super(mmio);
    }
}