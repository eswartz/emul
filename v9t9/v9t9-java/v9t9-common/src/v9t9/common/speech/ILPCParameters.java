/*
  ILPCParameters.java

  (c) 2012 Edward Swartz

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
package v9t9.common.speech;

import java.io.IOException;

import ejs.base.utils.BitInputStream;
import ejs.base.utils.BitOutputStream;

/**
 * Marker interface for LPC parameters for a frame of speech. 
 * @author ejs
 *
 */
public interface ILPCParameters {

	/**
	 * Convert parameters to bytes for serialization
	 * @return
	 * @throws IOException 
	 */
	byte[] toBytes() throws IOException;

	/**
	 * Convert parameters to bytes for serialization
	 * @return
	 * @throws IOException 
	 */
	void toBytes(BitOutputStream bs) throws IOException;

	/**
	 * Decode parameters from bytes 
	 * @return
	 * @throws IOException 
	 */
	void fromBytes(byte[] bytes) throws IOException;
	
	/**
	 * Decode parameters from bytes 
	 * @return
	 * @throws IOException 
	 */
	void fromBytes(BitInputStream bis) throws IOException;
	
	boolean isLast();

	boolean isSilent();

	boolean isUnvoiced();

	boolean isRepeat();
}
