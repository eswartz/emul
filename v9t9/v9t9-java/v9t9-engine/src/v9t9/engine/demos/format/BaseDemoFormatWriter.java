/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.IOException;
import java.io.OutputStream;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoOutputStream;
import v9t9.common.events.NotifyException;
import v9t9.engine.demos.events.SoundWriteDataEvent;
import v9t9.engine.demos.events.SoundWriteRegisterEvent;
import v9t9.engine.demos.events.SpeechWriteEvent;
import v9t9.engine.demos.events.TimerTick;
import v9t9.engine.demos.events.VideoWriteDataEvent;
import v9t9.engine.demos.events.VideoWriteRegisterEvent;

/**
 * @author ejs
 *
 */
public abstract class BaseDemoFormatWriter implements IDemoOutputStream {

	protected CountingOutputStream os;
	
	protected abstract void writeTimerTick() throws IOException;

	protected abstract void writeVideoRegisterEvent(IDemoEvent event)
			throws IOException;

	protected abstract void writeVideoDataEvent(IDemoEvent event)
			throws IOException;

	protected abstract void writeSoundDataEvent(IDemoEvent event)
			throws IOException;

	protected abstract void writeSoundRegisterEvent(IDemoEvent event)
			throws IOException;

	protected abstract void writeSpeechEvent(IDemoEvent event) throws IOException;

	protected DemoOutBuffer videoBuffer;
	protected DemoOutBuffer soundDataBuffer;
	protected DemoOutBuffer soundRegsBuffer;
	protected DemoOutBuffer speechBuffer;

	/**
	 * @param os 
	 * 
	 */
	public BaseDemoFormatWriter(OutputStream os_) {
		this.os = os_ instanceof CountingOutputStream ? (CountingOutputStream) os_ : new CountingOutputStream(os_);
		
		this.videoBuffer = new DemoPacketBuffer(os,
				DemoFormat.BufferType.VIDEO, DemoFormat.VIDEO_BUFFER_SIZE);
		this.soundDataBuffer = new DemoPacketBuffer(os,
				DemoFormat.BufferType.SOUND, DemoFormat.SOUND_BUFFER_SIZE);
		this.soundRegsBuffer = new DemoPacketBuffer(os,
				DemoFormat.BufferType.SOUND_REGS,
				DemoFormat.SOUND_REGS_BUFFER_SIZE);
		this.speechBuffer = new DemoPacketBuffer(os,
				DemoFormat.BufferType.SPEECH, DemoFormat.SPEECH_BUFFER_SIZE);

	}

	public synchronized void close() throws IOException {
		flushAll();

		preClose();
		
		if (os != null) {
			os.close();
			os = null;
		}
		soundRegsBuffer = null;
		soundDataBuffer = null;
		videoBuffer = null;
		speechBuffer = null;
	}

	/**
	 * Called immediately before closing.
	 * @throws IOException
	 */
	protected void preClose() throws IOException {
		
	}


	protected boolean anythingToFlush() {
		boolean any = false;
		if (videoBuffer != null)
			any |= !videoBuffer.isEmpty();
		if (soundRegsBuffer != null)
			any |= !soundRegsBuffer.isEmpty();
		if (soundDataBuffer != null)
			any |= !soundDataBuffer.isEmpty();
		if (speechBuffer != null)
			any |= !speechBuffer.isEmpty();
		return any;
	}
	
	protected boolean flushAll() throws IOException {
		boolean wrote = false;
		if (videoBuffer != null)
			wrote |= videoBuffer.flush();
		if (soundRegsBuffer != null)
			wrote |= soundRegsBuffer.flush();
		if (soundDataBuffer != null)
			wrote |= soundDataBuffer.flush();
		if (speechBuffer != null)
			wrote |= speechBuffer.flush();
		return wrote;
	}

	@Override
	public synchronized void writeEvent(IDemoEvent event) throws NotifyException {
		try {
			if (event instanceof TimerTick) {
				writeTimerTick();
			}
			else if (event instanceof VideoWriteRegisterEvent) {
				writeVideoRegisterEvent(event);
			}
			else if (event instanceof VideoWriteDataEvent) {
				writeVideoDataEvent(event);
			}
			else if (event instanceof SoundWriteDataEvent) {
				writeSoundDataEvent(event);
			}
			else if (event instanceof SoundWriteRegisterEvent) {
				writeSoundRegisterEvent(event);
			}
			else if (event instanceof SpeechWriteEvent) {
				writeSpeechEvent(event);
			}
			else {
				throw new NotifyException(null, "unknown event type: " + event);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new NotifyException(null, "error writing demo: " + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoOutputStream#getPosition()
	 */
	@Override
	public long getPosition() {
		return os.getPosition();
	}
}