/**
 * 
 */
package v9t9.base.sound;

import javax.sound.sampled.AudioFormat;

public interface ISoundListener {
	void started(AudioFormat format);
	void played(SoundChunk chunk);
	void stopped();
	void waitUntilSilent();
	void setVolume(double loudness);
}