package org.ejs.coffee.core.sound;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat.Encoding;

import org.ejs.coffee.core.sound.AlsaLibrary.snd_pcm_sw_params_t;

import com.sun.jna.ptr.IntByReference;

import static org.ejs.coffee.core.sound.AlsaLibrary.INSTANCE;

/**
 * ALSA sound output handler.
 * 
 * This blocks on {@link #played(SoundChunk)} if data is coming too fast.
 * @author ejs
 * 
 */
public class AlsaSoundListener implements ISoundListener {

	private AlsaLibrary.snd_pcm_t handle;
	private boolean stopped;
	private AudioFormat soundFormat;

	private Thread soundWritingThread;

	private BlockingQueue<AudioChunk> soundQueue;
	private final String device;
	private boolean blocking;
	private double volume;

	public AlsaSoundListener(String device) {
		// init outside locks
		AlsaLibrary.INSTANCE.hashCode();

		this.device = device != null ? device : "default";
		volume = 1.0;
	}

	public void setBlockMode(boolean blocking) {
		this.blocking = blocking;
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.sound.ISoundListener#setVolume(double)
	 */
	public void setVolume(double loudness) {
		this.volume = Math.max(0.0, Math.min(1.0, loudness));
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.SoundListener#stopped()
	 */
	public synchronized void stopped() {
		stopped = true;
		waitUntilSilent();
		
		if (handle != null) {
			INSTANCE.snd_pcm_close(handle);
			handle = null;
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
	public synchronized void started(AudioFormat format) {
		if (handle != null) {
			if (soundFormat.equals(format))
				return;
			stopped();
		}
		soundQueue = new LinkedBlockingQueue<AudioChunk>(50);

		soundFormat = format;
		
		int pcmFormat;
		switch (format.getSampleSizeInBits()) {
		case 8:
			if (format.getEncoding() == Encoding.PCM_UNSIGNED)
				pcmFormat = AlsaLibrary.SND_PCM_FORMAT_U8;
			else
				pcmFormat = AlsaLibrary.SND_PCM_FORMAT_S8;
			break;
		case 16:
			if (format.getEncoding() == Encoding.PCM_UNSIGNED)
				if (format.isBigEndian())
					pcmFormat = AlsaLibrary.SND_PCM_FORMAT_U16_BE;
				else
					pcmFormat = AlsaLibrary.SND_PCM_FORMAT_U16_LE;
			else
				if (format.isBigEndian())
					pcmFormat = AlsaLibrary.SND_PCM_FORMAT_S16_BE;
				else
					pcmFormat = AlsaLibrary.SND_PCM_FORMAT_S16_LE;
			break;
		default:
			throw new IllegalArgumentException("Cannot handle format " + format);
		}
		
		AlsaLibrary.snd_pcm_t.Ref pcmref = new AlsaLibrary.snd_pcm_t.Ref();
		int err = AlsaLibrary.INSTANCE.snd_pcm_open(
				pcmref,
				device,
				AlsaLibrary.SND_PCM_STREAM_PLAYBACK,
				AlsaLibrary.SND_PCM_NONBLOCK);
		if (err < 0) {
			System.err.println("Error creating ALSA PCM: " +
					INSTANCE.snd_strerror(err));
			handle = null;
			return;
		}
		handle = pcmref.get();
		
		System.out.println("Sound format: " + soundFormat);

		/*
    	snd_pcm_hw_params_t.Ref hwparamsRef = new snd_pcm_hw_params_t.Ref();
    	AlsaLibrary.INSTANCE.snd_pcm_hw_params_malloc(hwparamsRef);
    	snd_pcm_hw_params_t hwparams = hwparamsRef.get();
    	if (hwparams != null) {
    		IntByReference size = new IntByReference();
    		IntByReference dir = new IntByReference();
    		err = AlsaLibrary.INSTANCE.snd_pcm_hw_params_get_period_size(hwparams, size, dir);
    		System.out.println("Period size: " + size.getValue() + "; err = " + err);
    		AlsaLibrary.INSTANCE.snd_pcm_hw_params_free(hwparams);
    	}
		 */
    	
		int rate = format.getFrameRate() != AudioSystem.NOT_SPECIFIED ? (int) format.getFrameRate() : 48000;
    	err = AlsaLibrary.INSTANCE.snd_pcm_set_params(
    			handle, pcmFormat, 
    			AlsaLibrary.SND_PCM_ACCESS_RW_INTERLEAVED, 
    			format.getChannels(), rate, 1, 
    			100000);
    	if (err < 0) {
    		System.err.println(AlsaLibrary.INSTANCE.snd_strerror(err));
    		System.exit(1);
    	}
    	
    	snd_pcm_sw_params_t.Ref paramsRef = new snd_pcm_sw_params_t.Ref();
    	AlsaLibrary.INSTANCE.snd_pcm_sw_params_malloc(paramsRef);
    	snd_pcm_sw_params_t params = paramsRef.get();
    	if (params != null) {
    		if (AlsaLibrary.INSTANCE.snd_pcm_sw_params_current(handle, params) == 0) {
    			IntByReference boundary = new IntByReference();
    			err = AlsaLibrary.INSTANCE.snd_pcm_sw_params_get_boundary(params, boundary);
    			if (err == 0) {
	    			int frames = (int) (soundFormat.getSampleRate() * 0.1);
	    			//int frames = boundary.getValue() / 10;
	    			//err = AlsaLibrary.INSTANCE.snd_pcm_sw_params_set_start_threshold(handle, params, frames);
	    			//err = AlsaLibrary.INSTANCE.snd_pcm_sw_params_set_silence_threshold(handle, params, boundary.getValue());
	    			//err = AlsaLibrary.INSTANCE.snd_pcm_sw_params_set_silence_threshold(handle, params, 0);
	    			//err = AlsaLibrary.INSTANCE.snd_pcm_sw_params_set_stop_threshold(handle, params, boundary.getValue());
	    			err = AlsaLibrary.INSTANCE.snd_pcm_sw_params_set_avail_min(handle, params, frames);
	    			if (err == 0) {
	    				err = AlsaLibrary.INSTANCE.snd_pcm_sw_params(handle, params);
	    			}
    			}
    			if (err < 0) {
    				System.err.println("Error setting up ALSA PCM: " +
    						INSTANCE.snd_strerror(err));
    			}
    		}
    		AlsaLibrary.INSTANCE.snd_pcm_sw_params_free(params);
    	}
    	
		soundWritingThread = new Thread(new Runnable() {

			public void run() {
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

					if (handle == null || stopped)
						return;
					
					if (chunk.soundData != null) {
						//System.out.println("Wrt chunk " + chunk + " at " + System.currentTimeMillis());
						int size = chunk.soundData.length / soundFormat.getFrameSize();
						do {
							if (stopped)
								return;
							int err = INSTANCE.snd_pcm_writei(
									handle, chunk.soundData, size);
							if (err < 0 ) {
								if (err == -32) /* EPIPE */ {
									// underrun
									/*try {
										soundQueue.add(chunk);
									} catch (IllegalStateException e) {
										
									}*/
									err = AlsaLibrary.INSTANCE.snd_pcm_recover(handle, err, 0);
								} else {
									err = AlsaLibrary.INSTANCE.snd_pcm_recover(handle, err, 0);
								}
								if (err == 0)
									continue;
								if (err == -11) /* EAGAIN */ {
									// going too fast
									if (!blocking) {
										while (chunk != null && soundQueue.size() > 2) {
											chunk = soundQueue.poll();
										}
									}
									try {
										Thread.sleep(50);
									} catch (InterruptedException e) {
									}
									continue;
								}
								if (err < 0) {
									System.err.println("snd_pcm_writei failed: " + AlsaLibrary.INSTANCE.snd_strerror(err));
									err = AlsaLibrary.INSTANCE.snd_pcm_prepare(handle);
									err = AlsaLibrary.INSTANCE.snd_pcm_start(handle);
								}
				    		}
				    		break;
						} while (true);
					}
				}
			}

			

		}, "Sound Writing");
		
		stopped = false;
		
		soundWritingThread.start();

		//soundGeneratorLine.start();
		
		//soundSamplesPerTick = soundFormat.getFrameSize() * soundFramesPerTick / 2;
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.SoundListener#played(org.ejs.chiprocksynth.AudioChunk)
	 */
	public synchronized void played(SoundChunk chunk) {
		try {
			if (!blocking && soundQueue.remainingCapacity() == 0)
				soundQueue.remove();
			// will block if sound is too fast
			AudioChunk o = new AudioChunk(chunk, volume);
			soundQueue.put(o);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * 
	 */
	public void waitUntilEmpty() {
		if (handle != null) {
			soundQueue.clear();
			while (!soundQueue.isEmpty()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
		
	}

	/**
	 * 
	 */
	public void waitUntilSilent() {
		if (handle == null)
			return;
		INSTANCE.snd_pcm_drain(handle);
	}

}
