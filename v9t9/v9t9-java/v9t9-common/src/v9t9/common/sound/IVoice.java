/*
  IVoice.java

  (c) 2011-2012 Edward Swartz

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
package v9t9.common.sound;

import ejs.base.properties.IPersistable;

/**
 * This represents the parameters controlling a single voice
 * @author ejs
 *
 */
public interface IVoice extends IPersistable {
	/** Get the identifier for the voice, in register naming */
	String getId();
	/**
	 * Get the name or description of the voice
	 */
	String getName();
	
	/** Get base register number for voice */
	int getBaseRegister();
	/** Get register count for voice */
	int getRegisterCount();

	int getRegister(int reg);
	void setRegister(int reg, int newValue);
}
