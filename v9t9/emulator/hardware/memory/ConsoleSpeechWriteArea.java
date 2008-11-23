/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.emulator.runtime.Speech;

public class ConsoleSpeechWriteArea extends ConsoleMmioWriteArea {
    public ConsoleSpeechWriteArea(Speech mmio) {
        super(mmio);
    }
}