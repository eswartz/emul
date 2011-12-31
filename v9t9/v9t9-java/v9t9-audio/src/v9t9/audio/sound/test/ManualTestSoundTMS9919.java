/**
 * 
 */
package v9t9.audio.sound.test;

import java.util.concurrent.TimeUnit;


import org.junit.Test;

import v9t9.audio.sound.NoiseGeneratorVoice;
import v9t9.audio.sound.ToneGeneratorVoice;
import v9t9.common.sound.TMS9919Consts;


/**
 * @author ejs
 *
 */
public class ManualTestSoundTMS9919 extends BaseSoundTest {

	@Test
	public void testTonesSimple() {
		ToneGeneratorVoice toneVoice = new ToneGeneratorVoice("simple", 0);
		setupVoice(toneVoice);
		
		toneVoice.setFrequency(110);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		toneVoice.setFrequency(440);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		toneVoice.setFrequency(880);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		
	}


	@Test
	public void testTonesStd() {
		ToneGeneratorVoice toneVoice = new ToneGeneratorVoice("simple", 0);
		setupVoice(toneVoice);
		
		toneVoice.setFrequency(1398);
		generate(toSamples(TimeUnit.SECONDS, 2), toneVoice);
		toneVoice.setFrequency(110);
		generate(toSamples(TimeUnit.SECONDS, 2), toneVoice);
		
	}
	
	
	@Test
	public void testTonesHigh() {
		ToneGeneratorVoice toneVoice = new ToneGeneratorVoice("simple", 0);
		setupVoice(toneVoice);
		
		toneVoice.setFrequency(5500);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		toneVoice.setFrequency(11000);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		toneVoice.setFrequency(22000);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		toneVoice.setFrequency(44000);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		toneVoice.setFrequency(55930);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		toneVoice.setFrequency(77777);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		
	}
	
	@Test
	public void testTonesLow() {
		ToneGeneratorVoice toneVoice = new ToneGeneratorVoice("simple", 0);
		setupVoice(toneVoice);
		
		toneVoice.setFrequency(110);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		toneVoice.setFrequency(55);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		toneVoice.setFrequency(33);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		toneVoice.setFrequency(22);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		toneVoice.setFrequency(11);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		toneVoice.setFrequency(2);
		generate(toSamples(TimeUnit.SECONDS, 1), toneVoice);
		
	}
	

	@Test
	public void testToneSweep() {
		ToneGeneratorVoice toneVoice = new ToneGeneratorVoice("simple", 0);
		setupVoice(toneVoice);
		
		for (int period = 0; period < 1024; period++) {
			toneVoice.setPeriod(period);
			generate(toSamples(TimeUnit.MILLISECONDS, 10), toneVoice);
		}		
	}
	
	

	@Test
	public void testNoisesSimple() {
		NoiseGeneratorVoice voice = new NoiseGeneratorVoice("simple");
		setupVoice(voice);
		
		voice.setNoiseControl(TMS9919Consts.NOISE_FEEDBACK_PERIODIC | TMS9919Consts.NOISE_PERIOD_FIXED_0);
		generate(toSamples(TimeUnit.SECONDS, 2), voice);
		voice.setNoiseControl(TMS9919Consts.NOISE_FEEDBACK_PERIODIC | TMS9919Consts.NOISE_PERIOD_FIXED_1);
		generate(toSamples(TimeUnit.SECONDS, 2), voice);
		voice.setNoiseControl(TMS9919Consts.NOISE_FEEDBACK_PERIODIC | TMS9919Consts.NOISE_PERIOD_FIXED_2);
		generate(toSamples(TimeUnit.SECONDS, 2), voice);
		
		voice.setNoiseControl(TMS9919Consts.NOISE_FEEDBACK_WHITE | TMS9919Consts.NOISE_PERIOD_FIXED_0);
		generate(toSamples(TimeUnit.SECONDS, 2), voice);
		voice.setNoiseControl(TMS9919Consts.NOISE_FEEDBACK_WHITE | TMS9919Consts.NOISE_PERIOD_FIXED_1);
		generate(toSamples(TimeUnit.SECONDS, 2), voice);
		voice.setNoiseControl(TMS9919Consts.NOISE_FEEDBACK_WHITE | TMS9919Consts.NOISE_PERIOD_FIXED_2);
		generate(toSamples(TimeUnit.SECONDS, 2), voice);
		
	}

	@Test
	public void testNoisesHigh() {
		NoiseGeneratorVoice voice = new NoiseGeneratorVoice("simple");
		setupVoice(voice);
		
		voice.setNoiseControl(TMS9919Consts.NOISE_FEEDBACK_PERIODIC);
		
		voice.setFrequency(5500);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(11000);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(22000);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(44000);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(55930);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(77777);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		

		voice.setNoiseControl(TMS9919Consts.NOISE_FEEDBACK_WHITE);
		
		voice.setFrequency(5500);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(11000);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(22000);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(44000);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(55930);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(77777);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
	}
	
	@Test
	public void testNoisesLow() {
		NoiseGeneratorVoice voice = new NoiseGeneratorVoice("simple");
		setupVoice(voice);
		

		voice.setNoiseControl(TMS9919Consts.NOISE_FEEDBACK_PERIODIC);
		
		voice.setFrequency(110);
		generate(toSamples(TimeUnit.SECONDS, 5), voice);
		voice.setFrequency(55);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(33);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(22);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(11);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(2);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		
		
		voice.setNoiseControl(TMS9919Consts.NOISE_FEEDBACK_WHITE);
		
		voice.setFrequency(110);
		generate(toSamples(TimeUnit.SECONDS, 5), voice);
		voice.setFrequency(55);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(33);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(22);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(11);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		voice.setFrequency(2);
		generate(toSamples(TimeUnit.SECONDS, 1), voice);
		
	}
	

	@Test
	public void testNoiseSweep() {
		NoiseGeneratorVoice voice = new NoiseGeneratorVoice("simple");
		setupVoice(voice);

		voice.setNoiseControl(TMS9919Consts.NOISE_FEEDBACK_PERIODIC);
		
		for (int period = 0; period < 1024; period++) {
			voice.setPeriod(period);
			generate(toSamples(TimeUnit.MILLISECONDS, 10), voice);
		}
		
		voice.setNoiseControl(TMS9919Consts.NOISE_FEEDBACK_WHITE);
		
		for (int period = 0; period < 1024; period++) {
			voice.setPeriod(period);
			generate(toSamples(TimeUnit.MILLISECONDS, 10), voice);
		}		
	}
	
}
