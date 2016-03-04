/*
  SoundEngine.java

  (c) 2012, 2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;

import ejs.base.timer.FastTimer;
import ejs.base.utils.ListenerList;


public class SoundEngine {
	
	private ISoundEmitter iSoundListener;

	private ListenerList<ISoundVoice> voices;
	private ISoundVoice[] voiceArray;
	
	private ListenerList<IMutator> mutators;
	private IMutator[] mutatorArray;
	
	/** our driving clock, measured in ms, which may be faster than real time,
	 * so we can generate sound quickly enough for the buffers to stay full.
	 * (an int clock only lasts 25 days ;)
	 */
	private long clock;

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
		
		output = SoundFactory.createSoundOutput(format, mutateRate);
		this.mutatesPerSec = mutateRate;
		
		iSoundListener = emitter;
		iSoundListener.setBlockMode(true);
		
		output.addEmitter(iSoundListener);
		//output.addListener(fileRecorder);
		clock = 0;
		
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
		voice.setFormat(format);
		
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
		
		clock = 0;
		
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
				mutate(1000 / mutatesPerSec);
				//long end = System.currentTimeMillis();
				//System.out.println("elapsed: " + (end-start));
        	}
        };
        
        soundTimer.scheduleTask(soundTimingTask, mutatesPerSec);
        
	}
	/**
	 * 
	 */
	protected void mutate(int ticks) {
		//System.out.println("mutators: " + mutators.length + "; voices = " + voices.length);
		for (IMutator mutator : mutatorArray) {
			if (!mutator.mutate(clock)) {
				synchronized (this) {
					removeMutator(mutator);
				}
			}
		}
		generate(ticks);
	}

	public void stop() {
		if (soundTimer != null)
			soundTimer.cancel();
		output.flushAudio(voiceArray, output.getSamples(0));
		output.stop();
	}
	
	public void dispose() {
		output.dispose();
	}

	/**
	 * Generate sound for the given number of milliseconds.
	 */
	public void generate(int ms) {
		output.generate(voiceArray, output.getSamples(ms));
		for (ISoundVoice voice : voiceArray) {
			if (voice.shouldDispose()) {
				removeVoice(voice);
			}
		}
		clock += ms;
	}

	public SoundFormat getSoundFormat() {
		return format;
	}

	/**
	 * Get the number of milliseconds each mutation is expected to take
	 * (rounded down).
	 * @return
	 */
	public int getMutateTime() {
		return 1000 / mutatesPerSec;
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
		
		clock = 0;
	}

	
}