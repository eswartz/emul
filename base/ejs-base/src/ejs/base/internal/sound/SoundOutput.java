/*
  SoundOutput.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.internal.sound;

import java.util.ArrayList;
import java.util.List;

import ejs.base.sound.IFlushableSoundVoice;
import ejs.base.sound.ISoundEmitter;
import ejs.base.sound.ISoundMutator;
import ejs.base.sound.ISoundOutput;
import ejs.base.sound.ISoundView;
import ejs.base.sound.ISoundVoice;
import ejs.base.sound.SoundChunk;
import ejs.base.sound.SoundFileListener;
import ejs.base.sound.SoundFormat;
import ejs.base.utils.ListenerList;





/**
 * Mixing and output for sound
 * 
 * @author ejs
 * 
 */
public class SoundOutput implements ISoundOutput {

	private volatile float[] soundGeneratorWorkBuffer;

	// private boolean audioSilence;

	protected volatile int lastUpdatedPos;
	private boolean anyChanged;

	private ListenerList<ISoundMutator> mutators;
	private ListenerList<ISoundEmitter> emitters;
	
	// samples * channels
	private final int bufferSize;
	private SoundFormat format;
	private boolean wasStarted;
	
	private long clock;

	public SoundOutput(SoundFormat format, int tickRate) {
		this.format = format;
		mutators = new ListenerList<ISoundMutator>();
		emitters = new ListenerList<ISoundEmitter>();
		int b = (int) (format.getChannels() * format.getFrameRate() / tickRate);
		this.bufferSize = (b + 3) & ~3;
		soundGeneratorWorkBuffer = new float[bufferSize];
		clock  = 0;
	}

	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundOutput#getSoundFormat()
	 */
	@Override
	public SoundFormat getSoundFormat() {
		return format;
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundOutput#getClock()
	 */
	@Override
	public long getSampleClock() {
		return clock;
	}
	/**
	 * @param clock the clock to set
	 */
	public void setSampleClock(long clock) {
		this.clock = clock;
	}
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundOutput#addMutator(ejs.base.sound.ISoundMutator)
	 */
	@Override
	public void addMutator(ISoundMutator listener) {
		mutators.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundOutput#removeMutator(ejs.base.sound.ISoundMutator)
	 */
	@Override
	public void removeMutator(ISoundMutator listener) {
		mutators.remove(listener);
	}
	
	public void addEmitter(ISoundEmitter listener) {
		emitters.add(listener);
//		try {
//			listener.started(format);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	public void removeEmitter(ISoundEmitter listener) {
		try {
			listener.stopped();
		} catch (Exception e) {
			e.printStackTrace();
		}
		emitters.remove(listener);
	}
	
	public void fireListeners(ISoundView chunk) {
		if (!emitters.isEmpty()) {
			List<ISoundView> chunkList = null;
			if (!mutators.isEmpty()) {
				chunkList = new ArrayList<ISoundView>(1);
				List<ISoundView> nextList = new ArrayList<ISoundView>(1);
				
				chunkList.add(chunk);
				
				for (Object listener : mutators.toArray()) {
					try {
						for (ISoundView c : chunkList)
							((ISoundMutator)listener).editSoundChunk(c, nextList);
						
						List<ISoundView> t = chunkList;
						chunkList = nextList;
						nextList = t;
					} catch (Exception e ) {
						e.printStackTrace();
					}
				}
			}
			for (Object listener : emitters.toArray()) {
				try {
					if (chunkList == null) {
						((ISoundEmitter)listener).played(chunk);
					} else {
						for (ISoundView c : chunkList) {
							((ISoundEmitter)listener).played(c);
						}
					}
				} catch (Exception e ) {
					e.printStackTrace();
				}
			}
		}
	}

	public void setVolume(double loudness) {
		for (ISoundEmitter listener : emitters) {
			listener.setVolume(loudness);
		}
	}
	
	public void start() {
		for (ISoundEmitter listener : emitters) {
			try {
				listener.started(format);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		wasStarted = true;
	}
	
	public void stop() {
		for (ISoundEmitter listener : emitters) {
			try {
				listener.stopped();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		wasStarted = false;
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundOutput#isStarted()
	 */
	@Override
	public boolean isStarted() {
		return wasStarted;
	}
	public int getSamples(int ms) {
		int samples = (int) (((long) ms * format.getSampleRate() * format.getChannels() + 999) / 1000);
		if (format.getChannels() > 1)
			samples -= samples % format.getChannels();
		return samples;
	}
	/**
	 * Generate samples
	 */
	public void generate(ISoundVoice[] voices, int samples) {
		if (samples <= 0)
			return;
		
		synchronized (this) {
			int samplesLeft = samples * format.getChannels();
			
			while (samplesLeft > 0) {
				int endPos = lastUpdatedPos + samplesLeft;
				int to = endPos;
				if (to > bufferSize)
					to = bufferSize;
				if (lastUpdatedPos < to) {
				
					for (ISoundVoice v : voices) {
						if (v != null && v.isActive()) {
							anyChanged |= v.generate(soundGeneratorWorkBuffer, 
									lastUpdatedPos, to);
						}
					}
				}
				int generated = to - lastUpdatedPos;
				samplesLeft -= generated;
				lastUpdatedPos += generated;
				
				if (samplesLeft > 0)
					flushAudio(voices, 0);
			}
		}
	}

	public void dispose() {

	}

	public void flushAudio(ISoundVoice[] voices, int totalCount) {
		ISoundView chunk;
		chunk = createChunk(voices, totalCount);
		if (chunk != null)
			fireListeners(chunk);
	}

	private ISoundView createChunk(ISoundVoice[] voices, int totalCount) {
		SoundChunk chunk;
		synchronized (this) {
			if (soundGeneratorWorkBuffer == null || soundGeneratorWorkBuffer.length == 0 || lastUpdatedPos == 0)
				return null;
	
			for (ISoundVoice voice : voices) {
				if (voice instanceof IFlushableSoundVoice) {
					anyChanged |= ((IFlushableSoundVoice) voice).flushAudio(
							soundGeneratorWorkBuffer, 0, lastUpdatedPos, totalCount);
				}
			}
			
			if (anyChanged) {
				if (lastUpdatedPos < soundGeneratorWorkBuffer.length) {
					chunk = new SoundChunk(0, soundGeneratorWorkBuffer, 0, lastUpdatedPos, format);
					soundGeneratorWorkBuffer = new float[bufferSize];
				} else  {
					chunk = new SoundChunk(soundGeneratorWorkBuffer, format);
					soundGeneratorWorkBuffer = new float[bufferSize];
				}
			} else {
				chunk = new SoundChunk(lastUpdatedPos, format);
			}
			lastUpdatedPos = 0;
			anyChanged = false;
		}
		return chunk;
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundOutput#isRecording()
	 */
	@Override
	public boolean isRecording() {
		for (ISoundEmitter emitter : emitters) {
			if (emitter instanceof SoundFileListener && ((SoundFileListener) emitter).isStarted())
				return true;
		}
		return false;
	}
}
