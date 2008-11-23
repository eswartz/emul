/**
 * 
 */
package v9t9.emulator.hardware.memory;


public class ConsoleVdpReadArea extends ConsoleMmioReadArea {
    public ConsoleVdpReadArea(v9t9.emulator.runtime.Vdp mmio) {
        super(mmio);
    }
}