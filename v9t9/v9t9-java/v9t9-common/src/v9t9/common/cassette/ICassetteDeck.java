/*
  ICassetteVoice.java

  (c) 2012-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cassette;

import java.io.File;

import v9t9.common.sound.ISoundGenerator;
import ejs.base.sound.ISoundOutput;


/**
 * This represents the physical device that can 
 * record cassette chirps or decode them.  
 * @author ejs
 *
 */
public interface ICassetteDeck {
	/**
	 * Set the cassette output state
	 * @param state
	 */
	void writeBit(boolean state);
	boolean readBit();
	
	void setMotor(boolean motor);
	
	void stopCassette();
	
	boolean canPlay();
	void playCassette();
	boolean isPlaying();
	
	boolean canRecord();
	void recordCassette();
	boolean isRecording();
	
	ISoundOutput getOutput();
	ISoundGenerator getGenerator();
	void setOutput(ISoundOutput output);
	void setGenerator(ISoundGenerator generator);

	/** Get the file for input */
	File getFile();
	/** Set the file for input */
	void setFile(File file);
	
	void setSampleRate(int rate);
	void addFloatSample(float samp);

}
