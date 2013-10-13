/*
  SoundEngine.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;

import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;

import ejs.base.sound.ISoundEmitter;
import ejs.base.sound.ISoundOutput;
import ejs.base.sound.ISoundVoice;
import ejs.base.sound.SoundFactory;
import ejs.base.sound.SoundFileListener;
import ejs.base.timer.FastTimer;


public class SoundEngine {
	
	private ISoundEmitter iSoundListener;

	private ISoundVoice[] voices;
	
	private IMutator[] mutators;
	
	/** our driving clock, measured in ms, which may be faster than real time,
	 * so we can generate sound quickly enough for the buffers to stay full.
	 * (an int clock only lasts 25 days ;)
	 */
	private long clock;

	private SoundFileListener fileRecorder;

	private ISoundOutput output;

	private AudioFormat format;
	
	/**
	 */
	public SoundEngine(AudioFormat format, int mutateRate) {
		mutators = new IMutator[0];
		voices = new ISoundVoice[0];
		
		this.format = format;
		
		output = SoundFactory.createSoundOutput(format, mutateRate);
		
		iSoundListener = SoundFactory.createAudioListener();
		iSoundListener.setBlockMode(true);
		
		fileRecorder = new SoundFileListener();
		
		output.addEmitter(iSoundListener);
		//output.addListener(fileRecorder);
		clock = 0;
		
	}
	
	/**
	 * @return
	 */
	public boolean isMutating() {
		return mutators.length > 0;
	}

	/**
	 * @param mutator
	 */
	public void addMutator(IMutator mutator) {
		synchronized (this) {
			ArrayList<IMutator> moreMutators = new ArrayList<IMutator>(mutators.length + 1);
			moreMutators.addAll(Arrays.asList(mutators));
			moreMutators.add(mutator);
			mutators = (IMutator[]) moreMutators
					.toArray(new IMutator[moreMutators.size()]); 
		}
	}

	/**
	 * @param voice
	 */
	public void addVoice(ISoundVoice voice) {
		voice.setFormat(format);
		
		synchronized (this) {
			ArrayList<ISoundVoice> moreVoices = new ArrayList<ISoundVoice>(voices.length + 1);
			moreVoices.addAll(Arrays.asList(voices));
			moreVoices.add(voice);
			voices = (ISoundVoice[]) moreVoices
					.toArray(new ISoundVoice[moreVoices.size()]); 
		}
	}

	/**
	 * @param voice
	 */
	public void removeVoice(ISoundVoice voice) {
		synchronized (this) {
			ArrayList<ISoundVoice> fewerVoices = new ArrayList<ISoundVoice>(Arrays.asList(voices));
			fewerVoices.remove(voice);
			voices = (ISoundVoice[]) fewerVoices
					.toArray(new ISoundVoice[fewerVoices.size()]); 
		}
	}
	
	/**
	 * @return
	 */
	public ISoundVoice[] getVoices() {
		return voices;
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
		synchronized (this) {
			for (IMutator mutator : mutators) {
				if (!mutator.mutate(clock)) {
					removeMutator(mutator);
				}
			}
		}
		generate(ticks);
	}

	/**
	 * @param mutator
	 */
	public void removeMutator(IMutator mutator) {
		synchronized (this) {
			ArrayList<IMutator> fewerMutators = new ArrayList<IMutator>(mutators.length);
			fewerMutators.addAll(Arrays.asList(mutators));
			fewerMutators.remove(mutator);
			mutators = (IMutator[]) fewerMutators
					.toArray(new IMutator[fewerMutators.size()]); 
		}
	}

	public void stop() {
		if (soundTimer != null)
			soundTimer.cancel();
		output.flushAudio(voices, output.getSamples(0));
		output.stop();
	}
	
	public void dispose() {
		output.dispose();
	}

	/**
	 * @param diff
	 */
	public void generate(int ticks) {
		output.generate(voices, output.getSamples(ticks));
		for (ISoundVoice voice : voices) {
			if (voice.shouldDispose()) {
				removeVoice(voice);
			}
		}
		clock += ticks;
	}

	/**
	 * @return
	 */
	public int getSoundClock() {
		return (int) format.getFrameRate();
	}

	/**
	 * @return the fileRecorder
	 */
	public SoundFileListener getFileRecorder() {
		return fileRecorder;
	}
	/**
	 * @return
	 */
	public int getMutateTime() {
		return 1000 / mutatesPerSec;
	}

	/**
	 * @return
	 */
	public ISoundOutput getSoundOutput() {
		return output;
	}

	/**
	 * 
	 */
	public void reset() {
		synchronized (this) {
			voices = new ISoundVoice[0];
			mutators = new IMutator[0];
			clock = 0;
		}
	}

	
}