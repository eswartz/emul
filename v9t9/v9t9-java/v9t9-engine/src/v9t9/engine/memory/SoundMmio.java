/*
  SoundMmio.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.memory;

import ejs.base.utils.Check;
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
