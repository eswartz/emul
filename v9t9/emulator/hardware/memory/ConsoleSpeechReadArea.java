/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.emulator.runtime.Speech;

public class ConsoleSpeechReadArea extends ConsoleMmioReadArea {
    public ConsoleSpeechReadArea(Speech mmio) {
        super(mmio);
    }
}