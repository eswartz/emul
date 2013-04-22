/**
 * 
 */
package v9t9.audio.sound;

import java.util.ArrayList;
import java.util.List;

import v9t9.common.client.ISoundHandler;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.sound.ISoundGenerator;
import ejs.base.sound.ISoundOutput;
import ejs.base.sound.ISoundVoice;

/**
 * @author ejs
 *
 */
public abstract class BaseSoundGenerator implements ISoundGenerator, IRegisterAccess.IRegisterWriteListener {

	private ISoundVoice[] soundVoices;
	protected ISoundHandler soundHandler;
	protected int active;
	protected final List<ISoundVoice> soundVoicesList = new ArrayList<ISoundVoice>();

	private int lastUpdatedPos;

	private IMachine machine;

	/**
	 * @param machine 
	 * @param name 
	 * 
	 */
	public BaseSoundGenerator(IMachine machine) {
		this.machine = machine;
	}

	public ISoundVoice[] getSoundVoices() {
		if (soundVoices == null || soundVoices.length != soundVoicesList.size()) {
			soundVoices = soundVoicesList.toArray(new ISoundVoice[soundVoicesList.size()]);
		}
		return soundVoices;
	}


	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#generate(int, int)
	 */
	@Override
	public void generate(ISoundOutput output, int pos, int total) {
		int totalCount = pos;
		
		int framesPerTick = output.getSamples((1000 + machine.getTicksPerSec() - 1) / machine.getTicksPerSec());

		long ticksPos = (long) (pos * framesPerTick * getAudioFormat().getChannels() );
		int currentPos = (int) ((ticksPos + total - 1 ) / total);
		if (currentPos < 0)
			currentPos = 0;
		if (currentPos > framesPerTick)
			currentPos = framesPerTick;
		updateSoundGenerator(output, lastUpdatedPos, currentPos, totalCount);
		lastUpdatedPos = currentPos;
	}

	protected void updateSoundGenerator(ISoundOutput output, int from, int to, int totalCount) {
		if (from >= to)
			return;
	
		ISoundVoice[] vs = getSoundVoices();
		output.generate(vs, to - from);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#flushAudio(ejs.base.sound.ISoundOutput, int, int)
	 */
	@Override
	public void flushAudio(ISoundOutput output, int pos, int total) {
		if (output != null && total > 0) {
			int framesPerTick = output.getSamples((1000 + machine.getTicksPerSec() - 1) / machine.getTicksPerSec());

			int totalSoundCount = (int) (((long) pos * (framesPerTick - lastUpdatedPos + total - 1)) / total);
			updateSoundGenerator(output, lastUpdatedPos, framesPerTick, totalSoundCount);
			lastUpdatedPos = 0;
	
			ISoundVoice[] vs = getSoundVoices();
			output.flushAudio(vs, total);
		}		
	}
}