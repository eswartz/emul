/*
  SpeechMmio.java

  (c) 2008-2011 Edward Swartz

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
import v9t9.common.hardware.ISpeechChip;

/** 
 * Speech chip
 * @author ejs
 */
public class SpeechMmio implements IConsoleMmioWriter, IConsoleMmioReader {


	private final ISpeechChip speech;

	public SpeechMmio(final ISpeechChip speech) {
		Check.checkArg(speech);
		this.speech = speech;
    }

    public byte read(int addrMask) {
    	return speech.read();
    }
    
    /**
     * @see v9t9.common.memory.Memory.IConsoleMmioWriter#write 
     */
    public void write(int addr, byte val) {
        speech.write(val);
    }

}
