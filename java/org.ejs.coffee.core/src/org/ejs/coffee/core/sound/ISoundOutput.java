/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 29, 2004
 *
 */
package org.ejs.coffee.core.sound;





/**
 * @author ejs
 */
public interface ISoundOutput {
	
	int getSamples(int ms);
	
	void generate(ISoundVoice[] voices, int samples);
	
	void flushAudio();
	
	void dispose();
	
	void addListener(ISoundListener listener);
	void removeListener(ISoundListener listener);
	int getSoundClock();
	
	void start();
	void stop();
}

