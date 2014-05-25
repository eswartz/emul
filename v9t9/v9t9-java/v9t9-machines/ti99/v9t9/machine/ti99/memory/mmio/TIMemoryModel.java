/*
  TIMemoryModel.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.memory.mmio;

import v9t9.common.memory.IMemoryModel;
import v9t9.engine.memory.GplMmio;
import v9t9.engine.memory.VdpMmio;

/**
 * @author ejs
 *
 */
public interface TIMemoryModel extends IMemoryModel {

	/**
	 * Get the VDP MMIO. 
	 */
	VdpMmio getVdpMmio();
	
	/**
	 * Get the GPL MMIO. 
	 */
	GplMmio getGplMmio();
	
	/**
	 * Get the sound MMIO. 
	 */
	SoundMmio getSoundMmio();
	
	/**
	 * Get the speech MMIO. 
	 */
	SpeechMmio getSpeechMmio();
}
