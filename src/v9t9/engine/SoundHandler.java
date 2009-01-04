/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 29, 2004
 *
 */
package v9t9.engine;



/**
 * @author ejs
 */
public interface SoundHandler {
	
	void updateVoice(int pos, int total);
	
	void flushAudio();
	
	void dispose();

	void speech(short sample);
	
}

