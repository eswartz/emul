/*
  AlsaSoundListener.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;

import static ejs.base.sound.AlsaLibrary.INSTANCE;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;

import org.apache.log4j.Logger;

import com.sun.jna.ptr.IntByReference;

import ejs.base.sound.AlsaLibrary.snd_pcm_sw_params_t;

/**
 * ALSA sound output handler.
 * 
 * This blocks on {@link #played(ISoundView)} if data is coming too fast.
 * @author ejs
 * 
 */
public class AlsaSoundListener implements ISoundEmitter {

	private static final Logger logger = Logger.getLogger(AlsaSoundListener.class);
	
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
	 * @see v9t9.base.core.sound.ISoundListener#setVolume(double)
	 */
	public void setVolume(double loudness) {
		this.volume = Math.max(0.0, Math.min(1.0, loudness));
	}
	
	/* (non-Javadoc)
	 * 
	 */
	public void stopped() {
		synchronized (this) {
			stopped = true;
			waitUntilSilent();
		
			if (handle != null) {
				INSTANCE.snd_pcm_close(handle);
				handle = null;
			}
		}
		
		if (soundWritingThread != null) {
			soundWritingThread.interrupt();
			while (soundWritingThread.isAlive()) {
				try {
					soundWritingThread.join(100);
				} catch (InterruptedException e) {
					break;
				}
			}
			soundWritingThread = null;
		}
	}

	/* (non-Javadoc)
	 * 
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
			logger.error("Error creating ALSA PCM: " +
					INSTANCE.snd_strerror(err));
			handle = null;
			return;
		}
		handle = pcmref.get();
		

		logger.debug("Sound format: " + soundFormat);
		
		/*
    	snd_pcm_hw_params_t.Ref hwparamsRef = new snd_pcm_hw_params_t.Ref();
    	AlsaLibrary.INSTANCE.snd_pcm_hw_params_malloc(hwparamsRef);
    	snd_pcm_hw_params_t hwparams = hwparamsRef.get();
    	if (hwparams != null) {
    		IntByReference size = new IntByReference();
    		IntByReference dir = new IntByReference();
    		err = AlsaLibrary.INSTANCE.snd_pcm_hw_params_get_period_size(hwparams, size, dir);
    		logger.debug("Period size: " + size.getValue() + "; err = " + err);
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
    		logger.error(AlsaLibrary.INSTANCE.snd_strerror(err));
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
    				logger.error("Error setting up ALSA PCM: " +
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
					/*
					while (chunk != null && soundQueue.size() > 2) {
						chunk = soundQueue.poll();
					}
					*/

					synchronized (AlsaSoundListener.this) {
						if (handle == null || stopped)
							return;
					}
					
					if (chunk.soundData != null) {
						//logger.debug("Wrt chunk " + chunk + " at " + System.currentTimeMillis());
						int size = chunk.soundData.length / soundFormat.getFrameSize();
						synchronized (AlsaSoundListener.this) {
							do {
								if (stopped)
									return;
								if (sendChunk(chunk, size))
									break;
							} while (true);
							
							
						}
					}
				}
			}

			/**
			 * @param chunk
			 * @param size
			 */
			protected boolean sendChunk(AudioChunk chunk, int size) {
				int err = INSTANCE.snd_pcm_writei(
						handle, chunk.soundData, size);
				if (err >= 0)
					return true;
				
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
					return true;
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
					return false;
				}
				if (err < 0 && handle != null) {
					String snd_strerror = AlsaLibrary.INSTANCE.snd_strerror(err);
					logger.error("snd_pcm_writei failed: " + snd_strerror);
					err = AlsaLibrary.INSTANCE.snd_pcm_prepare(handle);
					err = AlsaLibrary.INSTANCE.snd_pcm_start(handle);
				}
				return false;
			}

			

		}, "Sound Writing");
		
		stopped = false;
		
		soundWritingThread.setDaemon(true);
		soundWritingThread.start();

		//soundGeneratorLine.start();
		
		//soundSamplesPerTick = soundFormat.getFrameSize() * soundFramesPerTick / 2;
	}

	/* (non-Javadoc)
	 * 
	 */
	public void played(ISoundView view) {
		synchronized (this) {
			if (handle == null)
				return;
		}
		try {
			if (!blocking && soundQueue.remainingCapacity() == 0)
				soundQueue.remove();
			// will block if sound is too fast
			AudioChunk o = new AudioChunk(view, volume);
			soundQueue.put(o);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * 
	 */
	public synchronized void waitUntilEmpty() {
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
	public synchronized void waitUntilSilent() {
		if (handle == null)
			return;
		INSTANCE.snd_pcm_drain(handle);
	}
}
