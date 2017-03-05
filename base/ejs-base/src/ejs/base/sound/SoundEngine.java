/*
  SoundEngine.java

  (c) 2012-2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;

import ejs.base.internal.sound.SoundOutput;
import ejs.base.timer.FastTimer;
import ejs.base.utils.ListenerList;


public class SoundEngine {
	
	private ISoundEmitter iSoundListener;

	private ListenerList<ISoundVoice> voices;
	private ISoundVoice[] voiceArray;
	
	private ListenerList<IMutator> mutators;
	private IMutator[] mutatorArray;

	private ISoundOutput output;

	private SoundFormat format;

	
	/**
	 */
	public SoundEngine(SoundFormat format, int mutateRate, ISoundEmitter emitter) {
		mutators = new ListenerList<IMutator>();
		mutatorArray = mutators.toArray(IMutator.class);
		
		voices = new ListenerList<ISoundVoice>();
		voiceArray = voices.toArray(ISoundVoice.class);
		
		this.format = format;
		
		//output = SoundFactory.createSoundOutput(format, mutateRate);
		output = new SoundOutput(format, mutateRate > 0 ? mutateRate : 100);
		this.mutatesPerSec = mutateRate;
		
		iSoundListener = emitter;
		iSoundListener.setBlockMode(true);
		
		output.addEmitter(iSoundListener);
		//output.addListener(fileRecorder);
		
	}
	
	/**
	 * @return
	 */
	public boolean isMutating() {
		return !mutators.isEmpty();
	}

	/**
	 * @param mutator
	 */
	public void addMutator(IMutator mutator) {
		mutators.add(mutator);
		mutatorArray = mutators.toArray(IMutator.class);
	}

	/**
	 * @param mutator
	 */
	public void removeMutator(IMutator mutator) {
		mutators.remove(mutator);
		mutatorArray = mutators.toArray(IMutator.class);
	}

	/**
	 * @param voice
	 */
	public void addVoice(ISoundVoice voice) {
		voice.setOutput(output);
		
		voices.add(voice);
		voiceArray = voices.toArray(ISoundVoice.class);
	}

	/**
	 * @param voice
	 */
	public void removeVoice(ISoundVoice voice) {
		voices.remove(voice);
		voiceArray = voices.toArray(ISoundVoice.class);
	}
	
	/**
	 * @return
	 */
	public ISoundVoice[] getVoices() {
		return voiceArray;
	}

	/**
	 * 
	 */
	public void waitUntilEmpty() {
		iSoundListener.waitUntilSilent();
	}

	private FastTimer soundTimer;
	private Runnable soundTimingTask;

	private int mutatesPerSec = 100;

	public void setMutateRate(int mutatesPerSec) {
		this.mutatesPerSec = mutatesPerSec;
		
	}
	public void start() {
		if (mutatesPerSec == 0)
			return;
		
		output.setSampleClock(0);
		
		// give some buffer to start with
		//soundHandler.generate(new ISoundVoice[0], 500);
		
			//try {
			//	Thread.sleep(ticks);
			//} catch (InterruptedException e) {
			//}
    	soundTimer = new FastTimer();

        soundTimingTask = new Runnable() {

        	public void run() {
				// generate more data so we keep buffers full;
		        // the task is expected to block some of the time 

				//long start = System.currentTimeMillis();
				mutate((int) (format.getSampleRate() / mutatesPerSec));
				//long end = System.currentTimeMillis();
				//System.out.println("elapsed: " + (end-start));
        	}
        };
        
        soundTimer.scheduleTask(soundTimingTask, mutatesPerSec);
        
	}
	/**
	 * 
	 */
	protected void mutate(int samples) {
		//System.out.println("mutators: " + mutators.length + "; voices = " + voices.length);
		for (IMutator mutator : mutatorArray) {
			if (!mutator.mutate(output.getSampleClock())) {
				synchronized (this) {
					removeMutator(mutator);
				}
			}
		}
		generateSamples(samples);
	}

	public void stop() {
		if (soundTimer != null)
			soundTimer.cancel();
		output.flushAudio(voiceArray, 0);
		output.stop();
	}
	
	public void dispose() {
		output.dispose();
	}

	/**
	 * Generate sound for the given number of milliseconds.
	 */
	public void generate(int ms) {
		generateSamples(output.getSamples(ms));
	}
	/**
	 * Generate sound for the given number of samples (channel count ignored)
	 */
	public void generateSamples(int samples) {
		
		output.generate(voiceArray, samples);
		
		for (ISoundVoice voice : voiceArray) {
			if (voice.shouldDispose()) {
				removeVoice(voice);
			}
		}
	}


    public void generateSamplesInto(int samples, float[] data) {
		if (samples <= 0)
			return;

        int samplesLeft = samples * format.getChannels();
        assert samplesLeft == data.length;

        for (ISoundVoice v : voices) {
            if (v != null && v.isActive()) {
                v.generate(data, 0, samplesLeft);
            }
        }

		for (ISoundVoice voice : voiceArray) {
			if (voice.shouldDispose()) {
				removeVoice(voice);
			}
		}
    }

	public SoundFormat getSoundFormat() {
		return format;
	}

	public ISoundOutput getSoundOutput() {
		return output;
	}

	/**
	 * Remove all the voices and mutators and set the clock to 0.
	 */
	public void reset() {
		voices.clear();
		voiceArray = voices.toArray(ISoundVoice.class);
		
		mutators.clear();
		mutatorArray = mutators.toArray(IMutator.class);
	}

}