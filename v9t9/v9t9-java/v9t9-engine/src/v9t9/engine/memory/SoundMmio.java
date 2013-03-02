/*
  SoundMmio.java

  (c) 2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
