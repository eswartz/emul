package org.ejs.coffee.core.sound;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;


import com.sun.jna.ptr.IntByReference;



/**
 * Pulseaudio sound output handler.
 * 
 * This blocks on {@link #played(SoundChunk)} if data is coming too fast.
 * @author ejs
 * 
 */
public class PulseSoundListener implements ISoundListener {

	private PulseAudioLibrary.pa_simple simple;
	private PulseAudioLibrary.pa_sample_spec sampleFormat;
	private AudioFormat soundFormat;

	private Thread soundWritingThread;

	private BlockingQueue<AudioChunk> soundQueue;

	public PulseSoundListener(int ticksPerSec) {
		//this.ticksPerSec = ticksPerSec;
		
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.SoundListener#stopped()
	 */
	public synchronized void stopped() {
		waitUntilSilent();
		
		if (simple != null) {
			PulseAudioLibrary.INSTANCE.pa_simple_free(simple);
			simple = null;
		}
		
		if (soundWritingThread != null) {
			soundWritingThread.interrupt();
			try {
				soundWritingThread.join();
			} catch (InterruptedException e) {
				
			}
			soundWritingThread = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.SoundListener#started(javax.sound.sampled.AudioFormat)
	 */
	public void started(AudioFormat format) {
		soundQueue = new LinkedBlockingQueue<AudioChunk>(20);

		soundFormat = format;
		sampleFormat = new PulseAudioLibrary.pa_sample_spec();
		switch (format.getSampleSizeInBits()) {
		case 8:
			sampleFormat.format = PulseAudioLibrary.PA_SAMPLE_U8;
			if (format.getEncoding() != Encoding.PCM_UNSIGNED)
				throw new IllegalArgumentException("Cannot handle signed 8-bit");
			break;
		case 16:
			if (format.getEncoding() == Encoding.PCM_UNSIGNED)
				throw new IllegalArgumentException("Cannot handle unsigned 16-bit");
			if (format.isBigEndian())
				sampleFormat.format = PulseAudioLibrary.PA_SAMPLE_S16BE;
			else
				sampleFormat.format = PulseAudioLibrary.PA_SAMPLE_S16LE;
			break;
		default:
			throw new IllegalArgumentException("Cannot handle format " + format);
		}
		
		sampleFormat.rate = (int) format.getFrameRate();
		sampleFormat.channels = (byte) format.getChannels();
    	
		//soundFramesPerTick = (int) (soundFormat.getFrameRate() / ticksPerSec);
		
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
			System.err.println("Error contacting pulse: " + 
					PulseAudioLibrary.INSTANCE.pa_strerror(error.getValue()));
			simple = null;
			return;
		}
		//soundGeneratorLine.open(soundFormat, soundFramesPerTick * 20 * 4);
		
		System.out.println("Sound format: " + soundFormat);

		soundWritingThread = new Thread(new Runnable() {

			public void run() {
				IntByReference error = new IntByReference();
				
				while (true) {
					AudioChunk chunk = null;

					try {
						chunk = soundQueue.take();
					} catch (InterruptedException e2) {
						return;
					}
					
					//if (chunk != null) dft(chunk.soundToWrite);
					
					// toss extra chunks if too many arrive
					if (false) {
						while (chunk != null && soundQueue.size() > 2) {
							chunk = soundQueue.poll();
						}
					}

					if (simple == null)
						return;
					
					if (chunk.soundData != null) {
						//System.out.println("Wrt chunk " + chunk + " at " + System.currentTimeMillis());
						PulseAudioLibrary.INSTANCE.pa_simple_write(
								simple, chunk.soundData, chunk.soundData.length, error);
					}
				}
			}

			

		}, "Sound Writing");
		soundWritingThread.start();

		//soundGeneratorLine.start();
		
		//soundSamplesPerTick = soundFormat.getFrameSize() * soundFramesPerTick / 2;
	}

	public void dispose() {
		waitUntilSilent();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.SoundListener#played(org.ejs.chiprocksynth.AudioChunk)
	 */
	public void played(SoundChunk chunk) {
		try {
			if (soundWritingThread == null) {
				if (soundQueue.remainingCapacity() == 0)
					soundQueue.remove();
			}
			// will block if sound is too fast
			AudioChunk o = new AudioChunk(chunk);
			//if (o.isEmpty())
			//	return;
			//System.out.println("Got chunk " + o + " at " + System.currentTimeMillis());
			soundQueue.put(o);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * 
	 */
	public void waitUntilEmpty() {
		if (simple != null)
			soundQueue.clear();
		while (!soundQueue.isEmpty()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				break;
			}
		}
		
	}

	/**
	 * 
	 */
	public void waitUntilSilent() {
		if (simple == null)
			return;
		IntByReference error = new IntByReference();
		PulseAudioLibrary.INSTANCE.pa_simple_drain(simple, error);
	}

}
