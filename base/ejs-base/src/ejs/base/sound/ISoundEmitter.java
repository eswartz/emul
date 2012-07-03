/**
 * 
 */
package ejs.base.sound;

import javax.sound.sampled.AudioFormat;

public interface ISoundEmitter {
	void started(AudioFormat format);
	void played(ISoundView view);
	void stopped();
	void waitUntilSilent();
	void setVolume(double loudness);
}