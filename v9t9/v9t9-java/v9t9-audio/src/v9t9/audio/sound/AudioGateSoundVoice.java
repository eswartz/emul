/**
 * 
 */
package v9t9.audio.sound;

import java.util.Arrays;

import ejs.base.settings.ISettingSection;
import ejs.base.sound.IFlushableSoundVoice;

public class AudioGateSoundVoice extends SoundVoice implements IFlushableSoundVoice {

	private boolean wasSet;
	private boolean state;
	private boolean origState;
	private int[] deltas = new int[0];
	private int deltaIdx = 0;
	private int baseCycles;
	
	public AudioGateSoundVoice(String name) {
		super("Audio Gate");
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
		baseCycles = 0;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.SoundVoice#isActive()
	 */
	@Override
	public boolean isActive() {
		return super.isActive();
	}
	public synchronized void setState(int curr) {
		boolean newState = curr >= 0;
		if (state != newState) {
			int offs = absp1(curr) - baseCycles;
			if (offs < 0)
				offs = curr;
			baseCycles = curr;
			state = newState;
			appendPos(state ? offs : -offs-1);
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
			int to, int total_unused) {
		boolean generated = false;
		if (from < to && (true)) {
			
			generated = true;
			int ratio = 128 + balance;
			float sampleL = ((256 - ratio) * 1f) / 256.f;
			float sampleR = (ratio * 1f) / 256.f;
			
			
			int totalSamps = to - from;
			
			int total = 0;
			for (int i = 0; i < deltaIdx; i++)
				total += absp1(deltas[i]);

			if (total == 0)
				total = 1;

			//StringBuilder sb = new StringBuilder();
			
			int idx = 0;
			int consumed = deltaIdx > 0 ? absp1(deltas[idx]) : 0;
			int next = from + (int) ((long) consumed * totalSamps / total);
			idx++;
			//sb.append(next - from).append(',');
			
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
					//sb.append(next - from).append(',');
				}
			}
			//System.out.println(sb.toString());
			//sb.setLength(0);
		}
		
		deltaIdx = 0;
		origState = state;
		baseCycles = total_unused;
		
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