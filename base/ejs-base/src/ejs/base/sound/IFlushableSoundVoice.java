/*
  IFlushableSoundVoice.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;


/**
 * This interface announces that a voice should be
 * synchronized every time a packet of audio is flushed.
 * It may, for example, rely on the specific amount of work
 * done during a packet rather than being generated in
 * real time.
 * @author ejs
 *
 */
public interface IFlushableSoundVoice extends ISoundVoice {

	/**
	 * Update the audio buffer.
	 * @param soundGeneratorWorkBuffer
	 * @param startPos
	 * @param lastUpdatedPos
	 * @param totalCount same parameter passed to {@link ISoundOutput#flushAudio(ISoundVoice[], int)}
	 * @return true if any sound generated
	 */
	boolean flushAudio(float[] soundGeneratorWorkBuffer, int startPos, int lastUpdatedPos, int totalCount);

}
