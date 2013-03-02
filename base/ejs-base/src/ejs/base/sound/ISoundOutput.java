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
	
	int getSamples(int ms);
	
	void generate(ISoundVoice[] voices, int samples);
	
	void flushAudio(ISoundVoice[] voices, int totalCount);
	
	void dispose();
	
	void addEmitter(ISoundEmitter listener);
	void removeEmitter(ISoundEmitter listener);
	
	void addMutator(ISoundMutator listener);
	void removeMutator(ISoundMutator listener);
	
	int getSoundClock();
	
	void start();
	void stop();
	
	void setVolume(double loudness);

	/**
	 * @return
	 */
	boolean isStarted();
}

