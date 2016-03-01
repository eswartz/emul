/*
  ISoundVoice.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;


/**
 * @author ejs
 *
 */
public interface ISoundVoice {
	/**
	 * Set the format for which the voice is generated.
	 * @param soundClock
	 */
	void setFormat(SoundFormat format);
	
	/** Generate samples from 'from' to 'to' in 'soundGeneratorWorkBuffer' 
	 * @return true if sound generated
	 */
	boolean generate(float[] soundGeneratorWorkBuffer,
			int from, int to);

	/** Tell if the voice is active (i.e. producing sound, not muted) */
	boolean isActive();

	/**
	 * Reset the voice
	 */
	void reset();

	/**
	 * If true, delete the voice 
	 * @return true once the voice is finished
	 */
	boolean shouldDispose();

}