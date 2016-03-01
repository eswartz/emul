/*
  PulseSoundListener.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;

import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.sun.jna.ptr.IntByReference;



/**
 * Pulseaudio sound output handler.
 * 
 * This blocks on {@link #played(ISoundView)} if data is coming too fast.
 * @author ejs
 * 
 */
public class PulseSoundListener implements ISoundEmitter {

	/**
	 * 
	 */
	private static final int SOUND_QUEUE_SIZE = 32;

	private static final Logger logger = Logger.getLogger(PulseSoundListener.class);
	
	private volatile PulseAudioLibrary.pa_simple simple;
	private PulseAudioLibrary.pa_sample_spec sampleFormat;
	private SoundFormat soundFormat;

	private Thread soundWritingThread;

	private BlockingQueue<AudioChunk> soundQueue;
	private double volume;

	private boolean block;

	public PulseSoundListener(int ticksPerSec) {
		//this.ticksPerSec = ticksPerSec;
		this.volume = 1.0;
	}

	/* (non-Javadoc)
	 * 
	 */
	public synchronized void stopped() {
		
		if (simple != null) {
			IntByReference error = new IntByReference();
			PulseAudioLibrary.INSTANCE.pa_simple_flush(simple, error);
			PulseAudioLibrary.INSTANCE.pa_simple_free(simple);
			simple = null;
		}
		
		if (soundWritingThread != null) {
			soundWritingThread.interrupt();
			long timeout= System.currentTimeMillis();
			while (System.currentTimeMillis() < timeout) {
				try {
					soundWritingThread.join(1000);
					break;
				} catch (InterruptedException e) {
					
				}
			}
			soundWritingThread = null;
		}
	}

	/* (non-Javadoc)
	 * 
	 */
	public synchronized void started(SoundFormat format) {
		if (simple != null) {
			if (soundFormat.equals(format))
				return;
			stopped();
		}
		
		soundQueue = new LinkedBlockingQueue<AudioChunk>(SOUND_QUEUE_SIZE);

		soundFormat = format;
		sampleFormat = new PulseAudioLibrary.pa_sample_spec();
		switch (format.getType()) {
		case UNSIGNED_8:
			sampleFormat.format = PulseAudioLibrary.PA_SAMPLE_U8;
			break;
		case SIGNED_8:
			throw new IllegalArgumentException("Cannot handle signed 8-bit");
		case SIGNED_16_BE:
			sampleFormat.format = PulseAudioLibrary.PA_SAMPLE_S16BE;
			break;
		case SIGNED_16_LE:
			sampleFormat.format = PulseAudioLibrary.PA_SAMPLE_S16LE;
			break;
		case FLOAT_32_LE:
			sampleFormat.format = PulseAudioLibrary.PA_SAMPLE_FLOAT32LE;
			break;
		default:
			throw new IllegalArgumentException("Cannot handle format " + format);
		}
		
		sampleFormat.rate = (int) format.getFrameRate();
		sampleFormat.channels = (byte) format.getChannels();
    	
		//soundFramesPerTick = (int) (soundFormat.getFrameRate() / ticksPerSec);
		
		//soundGeneratorLine.open(soundFormat, soundFramesPerTick * 20 * 4);
		
		logger.info("Sound format: " + soundFormat);

		soundWritingThread = new Thread(new Runnable() {

			public void run() {
				// Pulse needs to be created and used on the same thread
				IntByReference error = new IntByReference();

				simple = PulseAudioLibrary.INSTANCE.pa_simple_new(
						null,
						"PulseSoundListener",
						PulseAudioLibrary.PA_STREAM_PLAYBACK,
						null, // dev
						"PulseSoundListener",
						sampleFormat,
						null,
						null, //buffer
						error);
				
				if (simple == null || error.getValue() != 0) {
					logger.error("Error contacting pulse: " + 
							PulseAudioLibrary.INSTANCE.pa_strerror(error.getValue()));
					simple = null;
					
					synchronized (soundWritingThread) {
						// done
						soundWritingThread.notifyAll();
					}
					return;
				}

				synchronized (soundWritingThread) {
					// done
					soundWritingThread.notifyAll();
				}

				while (true) {
					AudioChunk chunk = null;

					try {
						chunk = soundQueue.take();
					} catch (InterruptedException e2) {
						return;
					}
					
					//if (chunk != null) dft(chunk.soundToWrite);
					
					/*
					// toss extra chunks if too many arrive
					while (chunk != null && soundQueue.size() > 2) {
						chunk = soundQueue.poll();
					}
					 */
					
					synchronized (PulseSoundListener.this) {
						if (simple == null)
							return;
						
						if (chunk.soundData != null) {
							//logger.debug("Wrt chunk " + chunk + " at " + System.currentTimeMillis());
							PulseAudioLibrary.INSTANCE.pa_simple_write(
									simple, chunk.soundData, chunk.soundData.length, error);
						}
					}
				}
			}

			

		}, "Sound Writing");
		soundWritingThread.setDaemon(true);
		
		// don't attempt to create the thread twice
		synchronized (soundWritingThread) {
			soundWritingThread.start();
			try {
				soundWritingThread.wait(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}


		//soundGeneratorLine.start();
		
		//soundSamplesPerTick = soundFormat.getFrameSize() * soundFramesPerTick / 2;
	}

	public synchronized void dispose() {
		waitUntilSilent();
	}
	
	/* (non-Javadoc)
	 * 
	 */
	public void played(ISoundView view) {
		synchronized (this) {
			if (simple == null)
				return;
		}
		try {
//			if (!block) {
//				soundQueue.drainTo(new ArrayList<AudioChunk>(1), SOUND_QUEUE_SIZE - 1);
//			}
			if (!block && soundQueue.remainingCapacity() == 0)
				soundQueue.remove();

			// will block if sound is too fast
			AudioChunk o = new AudioChunk(view, volume);
			soundQueue.put(o);
		} catch (NoSuchElementException e) {
		} catch (InterruptedException e) {
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.base.core.sound.ISoundListener#setVolume(double)
	 */
	public synchronized void setVolume(double loudness) {
		this.volume = Math.max(0.0, Math.min(1.0, loudness));
	}
	
	/**
	 * 
	 */
	public synchronized void waitUntilEmpty() {
		if (simple != null)
			soundQueue.clear();
		while (!soundQueue.isEmpty()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				break;
			}
		}
		
	}

	/**
	 * 
	 */
	public synchronized void waitUntilSilent() {
		if (simple == null)
			return;
		IntByReference error = new IntByReference();
		PulseAudioLibrary.INSTANCE.pa_simple_drain(simple, error);
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundEmitter#setBlockMode(boolean)
	 */
	@Override
	public void setBlockMode(boolean block) {
		this.block = block;
	}

}
