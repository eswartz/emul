/**
 * 
 */
package ejs.base.sound;

import javax.sound.sampled.AudioFormat;

public interface ISoundEmitter {
	void started(AudioFormat format);
	void played(SoundChunk chunk);
	void stopped();
	void waitUntilSilent();
	void setVolume(double loudness);
}