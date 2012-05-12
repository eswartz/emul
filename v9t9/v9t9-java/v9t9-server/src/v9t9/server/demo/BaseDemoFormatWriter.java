/**
 * 
 */
package v9t9.server.demo;

import java.io.IOException;
import java.io.OutputStream;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoOutputStream;
import v9t9.common.events.NotifyException;
import v9t9.server.demo.events.SoundWriteDataEvent;
import v9t9.server.demo.events.SoundWriteRegisterEvent;
import v9t9.server.demo.events.SpeechWriteEvent;
import v9t9.server.demo.events.TimerTick;
import v9t9.server.demo.events.VideoWriteDataEvent;
import v9t9.server.demo.events.VideoWriteRegisterEvent;

/**
 * @author ejs
 *
 */
public abstract class BaseDemoFormatWriter implements IDemoOutputStream {

	protected OutputStream os;

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
	public BaseDemoFormatWriter(OutputStream os) {
		this.os = os;
		
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
	 * @throws IOException
	 */
	protected void flushAll() throws IOException {
		if (videoBuffer != null)
			videoBuffer.flush();
		if (soundRegsBuffer != null)
			soundRegsBuffer.flush();
		if (soundDataBuffer != null)
			soundDataBuffer.flush();
		if (speechBuffer != null)
			speechBuffer.flush();
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

}