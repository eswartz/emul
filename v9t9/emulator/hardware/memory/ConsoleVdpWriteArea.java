/**
 * 
 */
package v9t9.emulator.hardware.memory;


public class ConsoleVdpWriteArea extends ConsoleMmioWriteArea {
    public ConsoleVdpWriteArea(v9t9.emulator.runtime.Vdp mmio) {
        super(mmio);
    }
}