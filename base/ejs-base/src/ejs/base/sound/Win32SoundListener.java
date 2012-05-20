package ejs.base.sound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import org.apache.log4j.Logger;


import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;

import ejs.base.winmm.WAVEFORMATEX;
import ejs.base.winmm.WAVEHDR;
import ejs.base.winmm.WinMMLibrary;

/**
 * ALSA sound output handler.
 * 
 * This blocks on {@link #played(SoundChunk)} if data is coming too fast.
 * @author ejs
 * 
 * TODO: FIXME: need to use a semaphore to avoid ugly stuttering sound.  We're currently sending samples on a fixed basis,
 * not when the driver is ready for them.
 * 
 */
public class Win32SoundListener implements ISoundListener {

	protected static final Logger logger = Logger.getLogger(Win32SoundListener.class);
	
	private boolean stopped;
	private AudioFormat soundFormat;

	private Thread soundWritingThread;

	private BlockingQueue<AudioChunk> soundQueue;
	private int wHandle;
	private boolean blocking;
	
	private List<WAVEHDR> hdrs = new ArrayList<WAVEHDR>();
	private int hdrIndex;
	private double volume;
	
	public Win32SoundListener() {
		// init outside locks
		WinMMLibrary.INSTANCE.hashCode();
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
	public synchronized void stopped() {
		stopped = true;
		waitUntilSilent();

		if (soundWritingThread != null) {
			soundWritingThread.interrupt();
			try {
				soundWritingThread.join();
			} catch (InterruptedException e) {
				
			}
			soundWritingThread = null;
		}

		synchronized (hdrs) {
			for (WAVEHDR hdr : hdrs) {
				if (hdr != null)
					WinMMLibrary.INSTANCE.waveOutUnprepareHeader(wHandle, hdr, 0);
			}
			hdrs.clear();
			
		}

		if (wHandle != 0) {
			int res = WinMMLibrary.INSTANCE.waveOutClose(wHandle);
			if (res != WinMMLibrary.MMSYSERR_NOERROR) {
				logger.error("Could not stop sound: " + getError(res));
			}
		}
		wHandle = 0;
	}

	/* (non-Javadoc)
	 * 
	 */
	public synchronized void started(AudioFormat format) {
		if (wHandle != 0) {
			if (soundFormat.equals(format))
				return;
			stopped();
		}
		soundQueue = new LinkedBlockingQueue<AudioChunk>(32);

		soundFormat = format;
		
		int res;

		WAVEFORMATEX wfx = new WAVEFORMATEX();
		
		int rate = format.getFrameRate() != AudioSystem.NOT_SPECIFIED ? (int) format.getFrameRate() : 48000;

		wfx.wFormatTag = WinMMLibrary.WAVE_FORMAT_PCM;
		wfx.nChannels = (short) format.getChannels();
		wfx.wBitsPerSample = (short) format.getSampleSizeInBits();
		wfx.nSamplesPerSec = rate;
		wfx.nBlockAlign = (short) (wfx.wBitsPerSample * wfx.nChannels / 8);
		wfx.nAvgBytesPerSec = wfx.nSamplesPerSec * wfx.nBlockAlign;
		wfx.cbSize = 0;

		logger.debug("Sound format: " + soundFormat);
    	
		wHandle = 0;
		
		hdrs = new ArrayList<WAVEHDR>(32);
		synchronized (hdrs) {
			for (int i = 0; i < 32; i++) {
				hdrs.add(null);
			}
		}
		
		IntByReference wHandleRef = new IntByReference();
		res = WinMMLibrary.INSTANCE.waveOutOpen(wHandleRef, 
					WinMMLibrary.WAVE_MAPPER, 
					wfx, 
					null,
					0, 
					WinMMLibrary.CALLBACK_NULL | (blocking ? WinMMLibrary.WAVE_ALLOWSYNC : 0));
		if (res != WinMMLibrary.MMSYSERR_NOERROR) {
			wHandle = 0;
			throw new IllegalArgumentException("Could not open a wave device:" +
					getError(res));
		}
		wHandle = wHandleRef.getValue();
		
		soundWritingThread = new Thread(new Runnable() {

			public void run() {
				while (true) {

					if (stopped)
						return;
					
					AudioChunk chunk = null;

					try {
						chunk = soundQueue.take();
					} catch (InterruptedException e2) {
						return;
					}
					
					//if (chunk != null) dft(chunk.soundToWrite);
					
					if (wHandle == 0 || stopped)
						return;
					
					if (chunk.soundData != null) {
						//logger.debug("Wrt chunk " + chunk + " at " + System.currentTimeMillis());
						int size = chunk.soundData.length;
						
						WAVEHDR hdr = null;
						
						synchronized (hdrs) {
							hdr = hdrs.get(hdrIndex);
							
							if (hdr != null) {
								waitForHeader(hdr);
								
								if (((Memory) hdr.lpData).size() != size) {
									WinMMLibrary.INSTANCE.waveOutUnprepareHeader(
											wHandle, hdr, hdr.size());
									hdr = null;
								}
							}
							
							if (hdr == null) {
								try {
									hdr = createWaveHeader(size);
								} catch (IOException e) {
									e.printStackTrace();
									return;
								}
								hdrs.set(hdrIndex, hdr);
							}
							
							hdrIndex = (hdrIndex + 1) % hdrs.size();
						}
						
						hdr.lpData.write(0, chunk.soundData, 0, size);

						//WinMMLibrary.INSTANCE.waveOutPause(wHandle);
						int res = WinMMLibrary.INSTANCE.waveOutWrite(
								wHandle, hdr, hdr.size());
						if (res != WinMMLibrary.MMSYSERR_NOERROR) {
							logger.error("Error in waveOutWrite: " + getError(res));
							WinMMLibrary.INSTANCE.waveOutReset(wHandle);
							soundQueue.clear();
						}
						//WinMMLibrary.INSTANCE.waveOutRestart(wHandle);
					}
				}
			}

			

		}, "Sound Writing");
		
		stopped = false;
		
		res = WinMMLibrary.INSTANCE.waveOutRestart(wHandle);
		if (res != WinMMLibrary.MMSYSERR_NOERROR) {
			logger.error("Could not restart sound: " + getError(res));
		}

		soundWritingThread.setDaemon(true);
		soundWritingThread.start();


		//soundGeneratorLine.start();
		
		//soundSamplesPerTick = soundFormat.getFrameSize() * soundFramesPerTick / 2;
	}

	protected void waitForHeader(WAVEHDR hdr) {
		hdr.autoRead();
		while ((hdr.dwFlags & WinMMLibrary.WHDR_DONE) == 0) {
			//Kernel32Library.INSTANCE.SleepEx(10, true);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			hdr.autoRead();
		}
		
	}

	private String getError(int res) {
		char[] buf = new char[256];
		WinMMLibrary.INSTANCE.waveOutGetErrorTextW(res, buf, buf.length);
		int idx = 0;
		while (idx < buf.length && buf[idx] != 0)
			idx++;
		return new String(buf, 0, idx);
	}

	/* (non-Javadoc)
	 * 
	 */
	public synchronized void played(SoundChunk chunk) {
		try {
			if (soundQueue.remainingCapacity() == 0)
				soundQueue.remove();
			// will block if sound is too fast
			AudioChunk o = new AudioChunk(chunk, volume);
			soundQueue.put(o);
		} catch (InterruptedException e) {
		}
	}

	protected void playChunk() {}
	/**
	 * 
	 */
	public void waitUntilEmpty() {
		if (wHandle != 0) {
			while (!soundQueue.isEmpty()) {
				try {
					Thread.sleep(100);
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
		if (wHandle == 0 || hdrs.size() == 0)
			return;
		synchronized (hdrs) {
			for (WAVEHDR hdr : hdrs) {
				if (hdr != null)
					waitForHeader(hdr);
			}
		}
	}

	private WAVEHDR createWaveHeader(int size) throws IOException {
		WAVEHDR hdr;
		hdr = new WAVEHDR();
		hdr.lpData = new Memory(size);
		hdr.dwFlags = 0; //WinMMLibrary.WHDR_DONE;
		hdr.dwLoops = 0;
		hdr.dwBufferLength = size;
		
		int res = WinMMLibrary.INSTANCE.waveOutPrepareHeader(
				wHandle, hdr, hdr.size());
		if (res != WinMMLibrary.MMSYSERR_NOERROR) {
			throw new IOException("failed in waveOutPrepareHeader: " + getError(res));
		}
		return hdr;
	}

}
