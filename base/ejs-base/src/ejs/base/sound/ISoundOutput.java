/*
  ISoundOutput.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;





/**
 * @author ejs
 */
public interface ISoundOutput {

	/**
	 * Get the format to which the output mixes
	 * and to which all sound voices should generate.
	 * @return
	 */
	SoundFormat getSoundFormat();

	int getSamples(int ms);
	
	/**
	 * Generate and mix content for the given voices
	 * @param voices
	 * @param samples # of samples (as a time measure, not based on channel count)
	 */
	void generate(ISoundVoice[] voices, int samples);
	
	/**
	 * Ensure any data left in sound buffers is flushed
	 * @param voices
	 * @param totalCount FIXME, what is this for?
	 */
	void flushAudio(ISoundVoice[] voices, int totalCount);
	
	void dispose();
	
	void addEmitter(ISoundEmitter listener);
	void removeEmitter(ISoundEmitter listener);
	
	void addMutator(ISoundMutator listener);
	void removeMutator(ISoundMutator listener);
	
	void start();
	void stop();
	
	void setVolume(double loudness);

	/**
	 * @return
	 */
	boolean isStarted();

	/**
	 * Tell whether any emitters are SoundFileListeners currently saving
	 * @return
	 */
	boolean isRecording();

	/**
	 * Get number of samples processed
	 * @return
	 */
	long getSampleClock();

	/**
	 * Set number of samples processed
	 * @param clock
	 */
	void setSampleClock(long clock);
}

