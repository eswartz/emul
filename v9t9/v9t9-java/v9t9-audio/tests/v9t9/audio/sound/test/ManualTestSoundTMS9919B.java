/**
 * 
 */
package v9t9.audio.sound.test;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import v9t9.audio.sound.ClockedSoundVoice;
import v9t9.audio.sound.EnhancedNoiseGeneratorVoice;
import v9t9.audio.sound.EnhancedToneGeneratorVoice;
import v9t9.audio.sound.EnhancedVoice;
import v9t9.audio.sound.NoiseGeneratorVoice;
import v9t9.audio.sound.SoundVoice;
import v9t9.common.sound.TMS9919BConsts;
import v9t9.common.sound.TMS9919Consts;

/**
 * @author ejs
 *
 */
public class ManualTestSoundTMS9919B extends BaseSoundTest {
	
	@Test
	public void testTonesSimple() {
		EnhancedToneGeneratorVoice toneVoice = new EnhancedToneGeneratorVoice("simple", 0);
		setupVoice(toneVoice);
		
		toneVoice.setFrequency(110);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		toneVoice.setFrequency(440);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		toneVoice.setFrequency(880);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		
	}


	@Test
	public void testTonesVolumes() {
		EnhancedToneGeneratorVoice toneVoice = new EnhancedToneGeneratorVoice("simple", 0);
		setupVoice(toneVoice);

		toneVoice.setFrequency(110);
		for (int vol = 0; vol < 256; vol+=4) {
			toneVoice.setVolume(vol);
			generate(toSamples(TimeUnit.MILLISECONDS, 50), toneVoice);
		}
		
	}

	@Test
	public void testEnvelope() {
		EnhancedToneGeneratorVoice voice = new EnhancedToneGeneratorVoice("simple", 0);
		setupVoice(voice);
		
		voice.getEffectsController().setADSR(TMS9919BConsts.OP_ATTACK, 2);
		voice.getEffectsController().setADSR(TMS9919BConsts.OP_DECAY, 4);
		voice.getEffectsController().setADSR(TMS9919BConsts.OP_HOLD, 10);
		voice.getEffectsController().setADSR(TMS9919BConsts.OP_RELEASE, 10);
		voice.getEffectsController().setSustain(64);
		
		for (int hz = 110; hz < 880; hz *= 2) {
			voice.setFrequency(hz);
			voice.setVolume(128);
			generate(toSamples(TimeUnit.SECONDS, 2), voice);
		}

	}
	

	@Test
	public void testVibrato() {
		EnhancedToneGeneratorVoice voice = new EnhancedToneGeneratorVoice("simple", 0);
		setupVoice(voice);
		EnhancedToneGeneratorVoice voice2 = new EnhancedToneGeneratorVoice("simple", 0);
		setupVoice(voice2);
		

		vibrato(1, 8, voice, voice2);
		vibrato(15, 8, voice, voice2);
		vibrato(1, 1, voice, voice2);
		vibrato(15, 15, voice, voice2);
		vibrato(8, 8, voice, voice2);
		

	}

	@Test
	public void testVibratoPeriodicNoise() {
		EnhancedNoiseGeneratorVoice voice = new EnhancedNoiseGeneratorVoice("simple");
		setupVoice(voice);
		EnhancedNoiseGeneratorVoice voice2 = new EnhancedNoiseGeneratorVoice("simple");
		setupVoice(voice2);
		

		vibrato(15, 8, voice, voice2);
		vibrato(15, 15, voice, voice2);
		vibrato(8, 8, voice, voice2);
	}
	@Test
	public void testVibratoWhiteNoise() {
		EnhancedNoiseGeneratorVoice voice = new EnhancedNoiseGeneratorVoice("simple");
		setupVoice(voice);
		voice.setNoiseControl(TMS9919Consts.NOISE_FEEDBACK_WHITE);
		EnhancedNoiseGeneratorVoice voice2 = new EnhancedNoiseGeneratorVoice("simple");
		setupVoice(voice2);
		voice2.setNoiseControl(TMS9919Consts.NOISE_FEEDBACK_WHITE);
		

		vibrato(1, 8, voice, voice2);
		vibrato(15, 15, voice, voice2);
		vibrato(8, 8, voice, voice2);
	}
	


	/**
	 * @param i
	 * @param j
	 */
	private void vibrato(int amount, int rate, EnhancedVoice... voices) {
		System.out.println("rate="+rate+"; amount="+amount);
		for (EnhancedVoice voice : voices)
			voice.getEffectsController().setVibrato(amount, rate);
		
		for (int hz = 110; hz < 880; hz *= 2) {
			for (int i = 0; i < voices.length; i++) {
				ClockedSoundVoice voice = ((ClockedSoundVoice) voices[i]);
				if (voice instanceof NoiseGeneratorVoice)
					voice.setFrequency(8 * (hz << i));
				else
					voice.setFrequency(hz << i);
				voice.setVolume(128);
			}
			generate(toSamples(TimeUnit.SECONDS, 2), voices);
		}

		
	}


	@Test
	public void testTremolo() {
		EnhancedToneGeneratorVoice voice = new EnhancedToneGeneratorVoice("simple", 0);
		setupVoice(voice);
		voice.setBalance((byte) -100);
		EnhancedToneGeneratorVoice voice2 = new EnhancedToneGeneratorVoice("simple", 0);
		setupVoice(voice2);
		voice2.setBalance((byte) 100);
		

		tremolo(1, 8, voice, voice2);
		tremolo(15, 8, voice, voice2);
		tremolo(1, 3, voice, voice2);
		tremolo(8, 3, voice, voice2);
		tremolo(15, 15, voice, voice2);

	}


	@Test
	public void testTremoloPeriodicNoise() {
		EnhancedNoiseGeneratorVoice voice = new EnhancedNoiseGeneratorVoice("simple");
		setupVoice(voice);
		voice.setBalance((byte) -100);
		EnhancedNoiseGeneratorVoice voice2 = new EnhancedNoiseGeneratorVoice("simple");
		setupVoice(voice2);
		voice2.setBalance((byte) 100);
		
		tremolo(1, 8, voice, voice2);
		tremolo(15, 8, voice, voice2);
		tremolo(1, 3, voice, voice2);

	}

	@Test
	public void testTremoloWhiteNoise() {
		EnhancedNoiseGeneratorVoice voice = new EnhancedNoiseGeneratorVoice("simple");
		setupVoice(voice);
		voice.setNoiseControl(TMS9919Consts.NOISE_FEEDBACK_WHITE);
		voice.setBalance((byte) -100);
		EnhancedNoiseGeneratorVoice voice2 = new EnhancedNoiseGeneratorVoice("simple");
		setupVoice(voice2);
		voice2.setNoiseControl(TMS9919Consts.NOISE_FEEDBACK_WHITE);
		voice2.setBalance((byte) 100);
		
		tremolo(1, 3, voice, voice2);
		tremolo(8, 3, voice, voice2);
		tremolo(15, 15, voice, voice2);

	}
	/**
	 * @param voice
	 */
	protected void tremolo(int amount, int rate, EnhancedVoice... voices) {
		System.out.println("amount="+amount+"; rate="+rate);
		for (EnhancedVoice voice : voices)
			voice.getEffectsController().setTremolo(amount, rate);
		
		for (int hz = 110; hz < 880; hz *= 2) {
			for (int i = 0; i < voices.length; i++) {
				if (voices[i] instanceof NoiseGeneratorVoice)
					((ClockedSoundVoice) voices[i]).setFrequency(16 * (hz << i));
				else
					((ClockedSoundVoice) voices[i]).setFrequency(hz << i);
				((SoundVoice) voices[i]).setVolume(128);
			}
			generate(toSamples(TimeUnit.SECONDS, 2), voices);
		}
	}
	

	@Test
	public void testWaveform() {
		EnhancedToneGeneratorVoice voice = new EnhancedToneGeneratorVoice("simple", 0);
		setupVoice(voice);
		
		voice.setFrequency(440);
		voice.setVolume(128);
		for (int wave = 0; wave < 8; wave++) {
			voice.getEffectsController().setWaveform(wave);
			generate(toSamples(TimeUnit.SECONDS, 2), voice);
		}
		
		EnhancedToneGeneratorVoice voice2 = new EnhancedToneGeneratorVoice("simple", 0);
		setupVoice(voice2);
		
		voice2.setFrequency(1402);
		voice2.setVolume(128);
		for (int wave = 0; wave < 8; wave++) {
			voice.getEffectsController().setWaveform(wave);
			voice2.getEffectsController().setWaveform(7 - wave);
			generate(toSamples(TimeUnit.SECONDS, 2), voice, voice2);
		}
	}
	


	@Test
	public void testSweep() {
		EnhancedToneGeneratorVoice voice = new EnhancedToneGeneratorVoice("simple", 0);
		setupVoice(voice);
		EnhancedToneGeneratorVoice voice2 = new EnhancedToneGeneratorVoice("simple", 0);
		setupVoice(voice2);
		
		voice.setFrequency(440);
		voice2.setFrequency(262);
		int period = voice.getPeriod();
		int period2 = voice2.getPeriod();
		voice.setVolume(128);
		voice2.setVolume(128);
		
		// up
		voice.getEffectsController().setSweepTarget(period / 2);
		voice.getEffectsController().setSweepTime(500);
		voice2.getEffectsController().setSweepTarget(period2 / 2);
		voice2.getEffectsController().setSweepTime(500);
		generate(toSamples(TimeUnit.SECONDS, 1), voice, voice2);
		
		// back
		voice.getEffectsController().setSweepTarget(period);
		voice.getEffectsController().setSweepTime(500);
		voice2.getEffectsController().setSweepTarget(period2);
		voice2.getEffectsController().setSweepTime(500);
		generate(toSamples(TimeUnit.SECONDS, 1), voice, voice2);

		// down
		voice.getEffectsController().setSweepTarget(period * 2);
		voice.getEffectsController().setSweepTime(500);
		voice2.getEffectsController().setSweepTarget(period2 * 2);
		voice2.getEffectsController().setSweepTime(500);
		generate(toSamples(TimeUnit.SECONDS, 1), voice, voice2);
		
	}
	

	@Test
	public void testBalance() {
		EnhancedToneGeneratorVoice voice = new EnhancedToneGeneratorVoice("simple", 0);
		setupVoice(voice);
		EnhancedToneGeneratorVoice voice2 = new EnhancedToneGeneratorVoice("simple", 0);
		setupVoice(voice2);
		
		voice.setFrequency(440);
		voice.setVolume(128);
		voice2.setFrequency(110);
		voice2.setVolume(128);

		for (int pan = -128; pan < 128; pan++) {
			voice.setBalance((byte) pan);
			voice2.setBalance((byte) (255 - pan));
			generate(toSamples(TimeUnit.MILLISECONDS, 25), voice, voice2);

		}
		
	}
}
