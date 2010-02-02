/**
 * 
 */
package org.ejs.coffee.core.sound;

import javax.sound.sampled.AudioFormat;

public interface ISoundListener {
	void started(AudioFormat format);
	void played(SoundChunk chunk);
	void stopped();
	void waitUntilSilent();
}