/**
 * 
 */
package v9t9.engine.sound;

import ejs.base.settings.ISettingSection;
import static v9t9.common.sound.TMS9919Consts.*;

public class NoiseGeneratorVoice extends ClockedSoundVoice
{
	boolean isWhite;
	volatile int ns1;
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
		isWhite = getOperationNoiseType() == NOISE_WHITE;
		
		
		setVolume((byte) (0xf - getOperationAttenuation()));
		if (periodtype != NOISE_PERIOD_VARIABLE) {
			period16 = noise_period[periodtype] * soundClock;
			hertz = period16ToHertz(period16);
		} else {
			period16 = pairedVoice2.period16;
			hertz = pairedVoice2.hertz;
		}
	
		incr = hertz;
		if (prevType != isWhite || (wasSilent && getVolume() != 0) || (isWhite && ns1 == 0)) {
			ns1 = (short) 0x8000;		// TODO: this should reset when the type of noise or sound changes only
			accum = 0;
		}
		
		dump();
	}

	public boolean generate(float[] soundGeneratorWorkBuffer, int from,
			int to) {
		int ratio = 128 + balance;
		boolean any = false;
		while (from < to) {
			updateEffect();
			
			float sampleMagnitude;
			float sampleL = 0;
			float sampleR = 0;
			
			sampleMagnitude = getCurrentMagnitude();
			if (sampleMagnitude != 0.0f) {
				any = true;
				sampleL = ((256 - ratio) * sampleMagnitude) / 256.f;
				sampleR = (ratio * sampleMagnitude) / 256.f;
				updateAccumulator();
				
				if (isWhite) {
					if ((ns1 & 1) != 0 ) {
						soundGeneratorWorkBuffer[from] += sampleL;
						soundGeneratorWorkBuffer[from+1] += sampleR;
					}
					
					// thanks to John Kortink (http://web.inter.nl.net/users/J.Kortink/home/articles/sn76489/)
					// for the exact algorithm here!
					while (accum >= soundClock) {
						short rx = (short) ((ns1 ^ ((ns1 >>> 1) & 0x7fff) ));
						rx = (short) (0x4000 & (rx << 14));
						ns1 = (short) (rx | ((ns1 >>> 1) & 0x7fff) );
						accum -= soundClock;
					}
				} else {
					// For periodic noise, the generator is "on" 1/15 of the time.
					// The clock is the hertz / 15.
					
					if (ns1 <= 1) {
						soundGeneratorWorkBuffer[from] -= sampleL * 2;
						soundGeneratorWorkBuffer[from+1] -= sampleR * 2;
						ns1 = (short) 0x8000;
					}
					if (accum >= soundClock) {
						ns1 = (short) ((ns1 >>> 1) & 0x7fff);
						while (accum >= soundClock) 
							accum -= soundClock;
					}
				}
			}
			
			from += 2;
		}
		return any;
	}
	
	@Override
	public void saveState(ISettingSection settings) {
		super.saveState(settings);
		settings.put("Shifter", ns1);
	}
	
	@Override
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		super.loadState(settings);
		ns1 = settings.getInt("Shifter");
	}
}