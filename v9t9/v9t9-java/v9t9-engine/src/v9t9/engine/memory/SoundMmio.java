/*
 * (c) Ed Swartz, 2008
 * 
 * Created on Dec 29, 2004
 *
 */
package v9t9.engine.memory;

import v9t9.base.utils.Check;
import v9t9.common.hardware.ISoundChip;

/** Sound chip entry
 * @author ejs
 */
public class SoundMmio implements IConsoleMmioWriter {

	private final ISoundChip sound;

    /**
     * @param machine
     */
    public SoundMmio(ISoundChip sound) {
    	Check.checkArg(sound);
    	
		this.sound = sound;
    }

    /**
     * @see v9t9.common.memory.Memory.IConsoleMmioWriter#write 
     */
    public void write(int addr, byte val) {
    	sound.writeSound(addr, val);
    }
    
}
