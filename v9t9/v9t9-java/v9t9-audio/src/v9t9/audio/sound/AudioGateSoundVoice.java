/**
 * 
 */
package v9t9.audio.sound;

import java.util.Arrays;

import ejs.base.settings.ISettingSection;
import ejs.base.sound.IFlushableSoundVoice;

import v9t9.common.machine.IMachine;

public class AudioGateSoundVoice extends SoundVoice implements IFlushableSoundVoice {

	private boolean wasSet;
	private boolean state;
	private boolean origState;
	private int[] deltas = new int[0];
	private int deltaIdx = 0;
	
	public AudioGateSoundVoice(String name) {
		super((name != null ? name + " " : "") + "Audio Gate");
	}
	
	@Override
	public void setupVoice() {
		setVolume((byte) (state ? MAX_VOLUME : 0));
		wasSet = true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.emul.core.sound.ISoundVoice#setSoundClock(int)
	 */
	public void setSoundClock(int soundClock) {
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.emul.core.sound.ISoundVoice#reset()
	 */
	public void reset() {
		wasSet = false;
		origState = false;
		deltaIdx = 0;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.SoundVoice#isActive()
	 */
	@Override
	public boolean isActive() {
		return super.isActive();
	}
	public synchronized void setState(IMachine machine, boolean b) {
		if (state != b) {
			state = b;
			
			int pos = machine.getCpu().getCurrentCycleCount();
			appendPos(state ? pos : -pos-1);
		}
	}

	/**
	 * @param pos
	 * @throws AssertionError
	 */
	protected void appendPos(int pos) throws AssertionError {
		if (deltaIdx > 0 && deltas[deltaIdx - 1] == pos)
			return;
		
		if (deltaIdx >= deltas.length) {
			int newlen = deltas.length * 2;
			if (newlen < 16)
				newlen = 16;
			deltas = Arrays.copyOf(deltas, newlen);
		}
		
		deltas[deltaIdx++] = pos;
	}

	public boolean generate(float[] soundGeneratorWorkBuffer, int from,
			int to) {
		
		//appendPos(state ? totalCount : -totalCount-1);
		return wasSet;
	}

	/* (non-Javadoc)
	 * @see v9t9.base.sound.ITimeAdjustSoundVoice#flushAudio(float[], int, int)
	 */
	@Override
	public synchronized void flushAudio(float[] soundGeneratorWorkBuffer, int from,
			int to, int total) {
		if (from < to && deltaIdx > 0) {
			int ratio = 128 + balance;
			float sampleMagnitude = origState ? 1f : 0f;
			float sampleL = ((256 - ratio) * sampleMagnitude) / 256.f;
			float sampleR = (ratio * sampleMagnitude) / 256.f;
			
			int idx = 0;
			
			while (from < to) {
				if (idx < deltaIdx && (long) from * total / to >= absp1(deltas[idx])) {
					//System.out.print("@" + deltas[idx] +":" + from +" ");
					
					sampleMagnitude = deltas[idx] >= 0 ? 1f : 0f;
					sampleL = ((256 - ratio) * sampleMagnitude) / 256.f;
					sampleR = (ratio * sampleMagnitude) / 256.f;
					
					idx++;
				}
				
				soundGeneratorWorkBuffer[from++] += sampleL;
				soundGeneratorWorkBuffer[from++] += sampleR;
			}
			//System.out.println(from + "! ");
		}
		
		deltaIdx = 0;
		origState = state;
	}
	
	/**
	 * @param i
	 * @return
	 */
	private int absp1(int i) {
		return i < 0 ? -(i+1) : i;
	}

	@Override
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		super.loadState(settings);
		setVolume((byte) (settings.getBoolean("State") ? MAX_VOLUME : 0));
	}
	
	@Override
	public void saveState(ISettingSection settings) {
		super.saveState(settings);
		settings.put("State", Boolean.toString(getVolume() != 0));
	}
	
}