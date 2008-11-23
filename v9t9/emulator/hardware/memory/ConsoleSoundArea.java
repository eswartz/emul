/**
 * 
 */
package v9t9.emulator.hardware.memory;


public class ConsoleSoundArea extends ConsoleMmioWriteArea {
    public ConsoleSoundArea(v9t9.emulator.runtime.Sound mmio) {
        super(mmio);
    }
}