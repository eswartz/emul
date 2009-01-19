/**
 * 
 */
package v9t9.emulator.hardware.sound;

import org.eclipse.jface.dialogs.IDialogSettings;

import v9t9.utils.Utils;

public class NoiseGeneratorVoice extends ClockedSoundVoice
{
	boolean isWhite;
	int ns1;
	private final ClockedSoundVoice pairedVoice2;
	
	public NoiseGeneratorVoice(String name, ClockedSoundVoice pairedVoice2) {
		super((name != null ? name + " " : "") + "Noise");
		this.pairedVoice2 = pairedVoice2;
	}
	protected void setupVoice()
	{
		int periodtype = getOperationNoisePeriod();
		boolean prevType = isWhite;
		boolean wasSilent = getVolume() == 0;
		isWhite = getOperationNoiseType() == SoundTMS9919.NOISE_WHITE;
		
		
		setVolume((byte) (0xf - getOperationAttenuation()));
		if (periodtype != SoundTMS9919.NOISE_PERIOD_VARIABLE) {
			period = SoundTMS9919.noise_period[periodtype];
			hertz = SoundTMS9919.periodToHertz(period);
		} else {
			period = pairedVoice2.period;
			hertz = pairedVoice2.hertz;
		}
	
		if (isWhite) {
			delta = hertz;
		} else {
			delta = hertz;
		}
		if (prevType != isWhite || (wasSilent && getVolume() != 0) || (isWhite && ns1 == 0)) {
			ns1 = (short) 0x8000;		// TODO: this should reset when the type of noise or sound changes only
			div = 0;
		}
		
		dump();
	}

	/*
	public int generate(int soundClock, int sample) {
		updateDivisor();
		if (isWhite) {
			
			// thanks to John Kortink (http://web.inter.nl.net/users/J.Kortink/home/articles/sn76489/)
			// for the exact algorithm here!
			while (div >= soundClock) {
				short rx = (short) ((ns1 ^ ((ns1 >>> 1) & 0x7fff) ));
				rx = (short) (0x4000 & (rx << 14));
				ns1 = (short) (rx | ((ns1 >>> 1) & 0x7fff) );
				div -= soundClock;
			}
			if ((ns1 & 1) != 0 ) {
				sample += sampleDelta;
			}
		} else {
			// For periodic noise, the generator is "on" 1/15 of the time.
			// The clock is the hertz / 15.
			
			// ns1 steps through 16 cycles, where 0x8000 through 0x2 are low, and 0x1 is high 
			if (ns1 <= 1) {
				sample -= sampleDelta * 2;
			}
			if (div >= soundClock) {
				if (ns1 == 1) {
					sample += sampleDelta * 4;
					ns1 = (short) 0x8000;
				}
				ns1 = (short) ((ns1 >>> 1) & 0x7fff);
				while (div >= soundClock) 
					div -= soundClock;
			}
		}
		return sample;
	}*/
	
	@Override
	public void generate(int soundClock, int[] soundGeneratorWorkBuffer,
			int from, int to) {
		int sampleL, sampleR;
		while (from < to) {
			updateDivisor();
			updateEffect();
			
			int sampleMagnitude = getCurrentMagnitude();
			int ratio = 128 + balance;
			sampleL = ((255 - ratio) * sampleMagnitude) >> 8;
			sampleR = (ratio * sampleMagnitude) >> 8;

			if (isWhite) {
				
				// thanks to John Kortink (http://web.inter.nl.net/users/J.Kortink/home/articles/sn76489/)
				// for the exact algorithm here!
				while (div >= soundClock) {
					short rx = (short) ((ns1 ^ ((ns1 >>> 1) & 0x7fff) ));
					rx = (short) (0x4000 & (rx << 14));
					ns1 = (short) (rx | ((ns1 >>> 1) & 0x7fff) );
					div -= soundClock;
				}
				if ((ns1 & 1) != 0 ) {
					soundGeneratorWorkBuffer[from] += sampleL;
					soundGeneratorWorkBuffer[from+1] += sampleR;
				}
			} else {
				// For periodic noise, the generator is "on" 1/15 of the time.
				// The clock is the hertz / 15.
				
				// ns1 steps through 16 cycles, where 0x8000 through 0x2 are low, and 0x1 is high 
				if (ns1 <= 1) {
					soundGeneratorWorkBuffer[from] -= sampleL * 2;
					soundGeneratorWorkBuffer[from+1] -= sampleR * 2;
				}
				if (div >= soundClock) {
					if (ns1 == 1) {
						soundGeneratorWorkBuffer[from] += sampleL * 4;
						soundGeneratorWorkBuffer[from + 1] += sampleR * 4;
						ns1 = (short) 0x8000;
					}
					ns1 = (short) ((ns1 >>> 1) & 0x7fff);
					while (div >= soundClock) 
						div -= soundClock;
				}
			}
			
			from += 2;
		}
	}
	
	@Override
	public void saveState(IDialogSettings settings) {
		super.saveState(settings);
		settings.put("Shifter", ns1);
	}
	
	@Override
	public void loadState(IDialogSettings settings) {
		super.loadState(settings);
		ns1 = Utils.readSavedInt(settings, "Shifter");
	}
}