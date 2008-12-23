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
	
	int UPDATE_PITCH = 1;
	int UPDATE_NOISE = 2;
	int UPDATE_VOLUME = 4;
	
	void updateVoice(int vn, int updateFlags);
	
	void audioGate(int bit);
	
	void dispose();
	
}

