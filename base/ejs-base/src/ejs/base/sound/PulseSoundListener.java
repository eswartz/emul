package ejs.base.sound;

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

	private volatile PulseAudioLibrary.pa_simple simple;
	private PulseAudioLibrary.pa_sample_spec sampleFormat;
	private AudioFormat soundFormat;

	private Thread soundWritingThread;

	private BlockingQueue<AudioChunk> soundQueue;
	private double volume;

	public PulseSoundListener(int ticksPerSec) {
		//this.ticksPerSec = ticksPerSec;
		this.volume = 1.0;
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.SoundListener#stopped()
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
	 * @see org.ejs.chiprocksynth.SoundListener#started(javax.sound.sampled.AudioFormat)
	 */
	public synchronized void started(AudioFormat format) {
		if (simple != null) {
			if (soundFormat.equals(format))
				return;
			stopped();
		}
		
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
		
		//System.out.println("Sound format: " + soundFormat);

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
							//System.out.println("Wrt chunk " + chunk + " at " + System.currentTimeMillis());
							PulseAudioLibrary.INSTANCE.pa_simple_write(
									simple, chunk.soundData, chunk.soundData.length, error);
						}
					}
				}
			}

			

		}, "Sound Writing");
		soundWritingThread.setDaemon(true);
		soundWritingThread.start();

		//soundGeneratorLine.start();
		
		//soundSamplesPerTick = soundFormat.getFrameSize() * soundFramesPerTick / 2;
	}

	public synchronized void dispose() {
		waitUntilSilent();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.SoundListener#played(org.ejs.chiprocksynth.AudioChunk)
	 */
	public synchronized void played(SoundChunk chunk) {
		try {
			if (soundWritingThread == null) {
				if (soundQueue.remainingCapacity() == 0)
					soundQueue.remove();
			}
			// will block if sound is too fast
			AudioChunk o = new AudioChunk(chunk, volume);
			//if (o.isEmpty())
			//	return;
			//System.out.println("Got chunk " + o + " at " + System.currentTimeMillis());
			soundQueue.put(o);
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
				Thread.sleep(100);
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

}
