/**
 * 
 */
package v9t9.audio.sound.test;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import v9t9.audio.sound.EnhancedToneGeneratorVoice;
import v9t9.common.sound.TMS9919BConsts;

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
		

		voice.getEffectsController().setVibrato(1, 8);
		
		for (int hz = 110; hz < 880; hz *= 2) {
			voice.setFrequency(hz);
			voice.setVolume(128);
			generate(toSamples(TimeUnit.SECONDS, 2), voice);
		}



		voice.getEffectsController().setVibrato(15, 8);
		
		for (int hz = 110; hz < 880; hz *= 2) {
			voice.setFrequency(hz);
			voice.setVolume(128);
			generate(toSamples(TimeUnit.SECONDS, 2), voice);
		}

		
		voice.getEffectsController().setVibrato(1, 1);
		
		for (int hz = 110; hz < 880; hz *= 2) {
			voice.setFrequency(hz);
			voice.setVolume(128);
			generate(toSamples(TimeUnit.SECONDS, 2), voice);
		}
		
		voice.getEffectsController().setVibrato(15, 15);
		
		for (int hz = 110; hz < 880; hz *= 2) {
			voice.setFrequency(hz);
			voice.setVolume(128);
			generate(toSamples(TimeUnit.SECONDS, 2), voice);
		}

		voice.getEffectsController().setVibrato(8, 8);
		
		for (int hz = 110; hz < 880; hz *= 2) {
			voice.setFrequency(hz);
			voice.setVolume(128);
			generate(toSamples(TimeUnit.SECONDS, 2), voice);
		}

	}
	


	@Test
	public void testTremolo() {
		EnhancedToneGeneratorVoice voice = new EnhancedToneGeneratorVoice("simple", 0);
		setupVoice(voice);
		

		voice.getEffectsController().setTremolo(1, 8);
		
		for (int hz = 110; hz < 880; hz *= 2) {
			voice.setFrequency(hz);
			voice.setVolume(128);
			generate(toSamples(TimeUnit.SECONDS, 2), voice);
		}



		voice.getEffectsController().setTremolo(15, 8);
		
		for (int hz = 110; hz < 880; hz *= 2) {
			voice.setFrequency(hz);
			voice.setVolume(128);
			generate(toSamples(TimeUnit.SECONDS, 2), voice);
		}

		
		voice.getEffectsController().setTremolo(1, 1);
		
		for (int hz = 110; hz < 880; hz *= 2) {
			voice.setFrequency(hz);
			voice.setVolume(128);
			generate(toSamples(TimeUnit.SECONDS, 2), voice);
		}
		
		voice.getEffectsController().setTremolo(15, 15);
		
		for (int hz = 110; hz < 880; hz *= 2) {
			voice.setFrequency(hz);
			voice.setVolume(128);
			generate(toSamples(TimeUnit.SECONDS, 2), voice);
		}

		voice.getEffectsController().setTremolo(8, 8);
		
		for (int hz = 110; hz < 880; hz *= 2) {
			voice.setFrequency(hz);
			voice.setVolume(128);
			generate(toSamples(TimeUnit.SECONDS, 2), voice);
		}

	}
	

	@Test
	public void testWaveform() {
		EnhancedToneGeneratorVoice voice = new EnhancedToneGeneratorVoice("simple", 0);
		setupVoice(voice);
		
		for (int wave = 0; wave < 8; wave++) {
			voice.setFrequency(440);
			voice.setVolume(128);
			voice.getEffectsController().setWaveform(wave);
			generate(toSamples(TimeUnit.SECONDS, 2), voice);
		}
	}
	


	@Test
	public void testSweep() {
		EnhancedToneGeneratorVoice voice = new EnhancedToneGeneratorVoice("simple", 0);
		setupVoice(voice);
		
		voice.setFrequency(440);
		int period = voice.getPeriod();
		voice.setVolume(128);
		
		// up
		voice.getEffectsController().setSweepTarget(period / 2);
		voice.getEffectsController().setSweepTime(500);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		
		// back
		voice.getEffectsController().setSweepTarget(period);
		voice.getEffectsController().setSweepTime(500);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);

		// down
		voice.getEffectsController().setSweepTarget(period * 2);
		voice.getEffectsController().setSweepTime(500);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		
	}
}
