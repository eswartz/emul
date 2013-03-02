/*
  ILPCParameters.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
