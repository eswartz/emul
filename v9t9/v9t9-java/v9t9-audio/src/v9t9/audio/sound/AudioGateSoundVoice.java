/*
  AudioGateSoundVoice.java

  (c) 2009-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.audio.sound;

import java.util.Arrays;

import ejs.base.settings.ISettingSection;
import ejs.base.sound.IFlushableSoundVoice;
import ejs.base.sound.ISoundOutput;
import ejs.base.sound.SoundFormat;

/**
 * Produce sound on the audio gate, which has two states: on and off.
 * This gate can be toggled at the CPU cycle rate, leading to interesting
 * issues:
 * <ul>
 * <li>the gate may be toggled much faster than the sound generation rate</li>
 * <li>the gate's changes won't sync with the sound generation rate</li>
 * <li>the gate may be toggled much slower than the generation window</li>
 * </ul>
 * <p>
 * The first two issues mean we must antialias the gate's output:  instead of
 * containing pure 1's and 0's, it will contain up to two samples at the transition
 * to approximate the proportion of the cycle count when the sample changed.
 * </p>
 * <p>The last issue means we must track transitions that span generation windows.
 * This holds in general in any case, since transitions aren't expected to line
 * up with the generation window either.</p>  
 * <p>
 * The AudioGateVoice driver will invoke {@link #setState(float, boolean)} with
 * each transition from the CPU side.  The float is a delta from the last
 * change, in terms of seconds.  We store these transitions in 'deltas', scaled
 * by the sound clock (e.g. moving them to frame lengths).  
 * @author ejs
 *
 */
public class AudioGateSoundVoice extends SoundVoice implements IFlushableSoundVoice {

	private boolean wasSet;
	private boolean state;
	private boolean origState;
	private float prevCycleRemainder;
	private float frac;
	private float[] deltas = new float[0];
	private int deltaIdx = 0;
	private long timeout;
	private float soundClock;
	
	public AudioGateSoundVoice(String name) {
		super("Audio Gate");
	}

	/* (non-Javadoc)
	 * @see v9t9.audio.sound.SoundVoice#setOutput(ejs.base.sound.ISoundOutput)
	 */
	@Override
	public void setOutput(ISoundOutput output) {
		super.setOutput(output);
		soundClock = output.getSoundFormat().getFrameRate();
	}
	
	@Override
	public void setupVoice() {
		setVolume((byte) (state ? MAX_VOLUME : 0));
		wasSet = true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.emul.core.sound.ISoundVoice#reset()
	 */
	public synchronized void reset() {
		wasSet = false;
		origState = false;
		deltaIdx = 0;
		prevCycleRemainder = 0;
		frac = 0;
	}
	
	public synchronized void setState(float seconds, boolean newState) {
		if (state != newState) {
			state = newState;
			appendPos((newState ? seconds : -seconds) * soundClock);
		}
	}

	/**
	 * @param offs
	 * @throws AssertionError
	 */
	protected void appendPos(float offs) throws AssertionError {
		if (deltaIdx >= deltas.length) {
			int newlen = Math.max(16, deltas.length * 2);
			deltas = Arrays.copyOf(deltas, newlen);
		}

		// ignore long "off" periods when we've already been silent
		if (offs < 0 && deltaIdx == 0 && prevCycleRemainder == 0 && timeout == 0)
			return;
		
//		System.out.println(offs);
		deltas[deltaIdx++] = offs;
		
		// don't keep a high audio gate on all the time
		timeout = System.currentTimeMillis() + 1000;
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
		if (origState && System.currentTimeMillis() >= timeout) {
			deltaIdx = 0;
			prevCycleRemainder = 0;
			frac = 0;
			origState = false;
			timeout = 0;
		}
		
		if (from < to && (deltaIdx > 0 || origState)) {
			
			generated = true;
			int ratio = 128 + balance;
			float sampleL = ((256 - ratio) * 1f) / 128.f;
			float sampleR = (ratio * 1f) / 128.f;
		
			boolean on = origState;

			// frame count
			float fpos = from / 2 + frac;
			float fto = to / 2;

			int idx = 0;
			while (fpos < fto) {
				float flen;
				float fnext;
				
//				if (idx == -1) {
//					if (prevCycleRemainder != 0) {
//						// account for remainder of previous cycle
//						flen = prevCycleRemainder;
//					} else {
//						idx++;
//						continue;
//					}
//				}
//				else 
				if (idx < deltaIdx) {
					flen = Math.abs(deltas[idx]);

					on = deltas[idx] >= 0;

					if (origState != on && flen > 0 && frac != 0 && fpos < fto) {
						// handle transition from previous
						float alpha = frac;
						if (on) {
							alpha = 1 - alpha;
						}
						int spos = ((int) fpos) * 2;
						soundGeneratorWorkBuffer[spos] += alpha * sampleL; 
						soundGeneratorWorkBuffer[spos + 1] += alpha * sampleR;
						fpos++;
						flen--;
					}
					
					origState = on;
					
				} else {
					// fill remainder of transition
					flen = fto - fpos;
				}
				
				fnext = fpos + flen;
				if (fnext > fto) {
					// only getting part of this one --
					// bite off what we can chew
					
					//prevCycleRemainder = (fnext - fto);
					fnext = fto;
					flen = fnext - fpos;
					
					//System.out.println(idx+": "+flen);
					if (idx >= 0) {
						if (on) {
							deltas[idx] -= flen;
						} else {
							deltas[idx] += flen;
						}
					}
				} else {
//					prevCycleRemainder = 0;
					// will get it all!
					idx++;
				}

				frac = fnext - (int) fnext;
				

				// fill integral positions
				if (on) {
					int spos = ((int) fpos) * 2;
					int snext = ((int) fnext) * 2;
					while (spos < snext) {
						soundGeneratorWorkBuffer[spos] += sampleL; 
						soundGeneratorWorkBuffer[spos + 1] += sampleR;
						spos += 2;
					}
				}
				
				fpos = fnext;
			}
			
			if (idx < deltaIdx) {
				System.arraycopy(deltas, idx, deltas, 0, deltaIdx - idx);
				deltaIdx -= idx;
			} else {
				deltaIdx = 0;
			}
		}
		
		return generated;
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