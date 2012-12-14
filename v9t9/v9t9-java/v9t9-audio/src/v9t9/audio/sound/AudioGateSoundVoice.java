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
//			long curr = machine.getCpu().getTotalCurrentCycleCount();
//			int pos = (int) (curr - baseCycles);
//			baseCycles = curr;
			int pos = machine.getCpu().getCurrentCycleCount();
//			System.out.println(pos);
			state = b;
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
	public synchronized boolean flushAudio(float[] soundGeneratorWorkBuffer, int from,
			int to, int total) {
		boolean generated = false;
		if (from < to && (true)) {
			generated = true;
			int ratio = 128 + balance;
			float sampleL = ((256 - ratio) * 1f) / 256.f;
			float sampleR = (ratio * 1f) / 256.f;
			
			int idx = 0;
			
			int totalSamps = to - from;
			
			total = 0;
			for (int i = 0; i < deltaIdx; i++)
				total += absp1(deltas[i]);
			
			
			int consumed = deltaIdx > 0 ? absp1(deltas[idx]) : 0;
			int next = from + (int) (idx < deltaIdx ? (long) consumed * totalSamps / total : totalSamps);
			idx++;
						
			boolean on = origState;
			while (from < to) {
				if (on) {
					soundGeneratorWorkBuffer[from++] += sampleL;
					soundGeneratorWorkBuffer[from++] += sampleR;
				} else {
					from += 2;
				}
				if (from >= next) {
					if (idx < deltaIdx) {
						on = (deltas[idx] > 0);
						consumed += absp1(deltas[idx++]);
						next = (int) ((long) consumed * totalSamps / total);
					} else {
						on = state;
						next = to;
					}
				}
			}
		}
		
		deltaIdx = 0;
		origState = state;
		
		return generated;
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