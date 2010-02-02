/*
 * (c) Ed Swartz, 2008
 * 
 * Created on Dec 29, 2004
 *
 */
package v9t9.emulator.hardware.memory.mmio;

import v9t9.emulator.Machine.ConsoleMmioWriter;
import v9t9.emulator.clients.builtin.SoundProvider;

/** Sound chip entry
 * @author ejs
 */
public class SoundMmio implements ConsoleMmioWriter {

	private final SoundProvider sound;

    /**
     * @param machine
     */
    public SoundMmio(SoundProvider sound) {
		this.sound = sound;
    }

    /**
     * @see v9t9.engine.memory.Memory.ConsoleMmioWriter#write 
     */
    public void write(int addr, byte val) {
    	sound.writeSound(addr, val);
    }
    
}
