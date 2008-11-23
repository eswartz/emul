/**
 * 
 */
package v9t9.emulator.hardware.memory.mmio;

import v9t9.engine.memory.Gpl;


public class ConsoleGramWriteArea extends ConsoleMmioWriteArea {
    public ConsoleGramWriteArea(Gpl mmio) {
        super(mmio);
    }
}