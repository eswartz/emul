/**
 * 
 */
package v9t9.engine.demos.format.old;

import java.io.IOException;
import java.io.OutputStream;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoOutputStream;
import v9t9.common.demo.ISpeechEvent;
import v9t9.engine.demos.events.SoundWriteDataEvent;
import v9t9.engine.demos.events.VideoWriteDataEvent;
import v9t9.engine.demos.events.VideoWriteRegisterEvent;
import v9t9.engine.demos.stream.BaseDemoOutputStream;

/**
 * Writer for TI Emulator v6.0 & V9t9 demo formats
 * @author ejs
 *
 */
public class OldDemoFormatOutputStream extends BaseDemoOutputStream implements IDemoOutputStream {

	private int ticks60;

	protected OldDemoPacketBuffer videoBuffer;
	protected OldDemoPacketBuffer soundDataBuffer;
	protected OldDemoPacketBuffer speechBuffer;

	public OldDemoFormatOutputStream(OutputStream os) throws IOException {
		super(os);
		
		os.write(OldDemoFormat.DEMO_MAGIC_HEADER_V910);
		
		this.videoBuffer = new OldDemoPacketBuffer(os,
				OldDemoFormat.VIDEO, OldDemoFormat.VIDEO_BUFFER_SIZE);
		this.soundDataBuffer = new OldDemoPacketBuffer(os,
				OldDemoFormat.SOUND, OldDemoFormat.SOUND_BUFFER_SIZE);
		this.speechBuffer = new OldDemoPacketBuffer(os,
				OldDemoFormat.SPEECH, OldDemoFormat.SPEECH_BUFFER_SIZE);

		buffers.add(videoBuffer);
		buffers.add(soundDataBuffer);
		buffers.add(speechBuffer);
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoOutputStream#getTimerRate()
	 */
	@Override
	public int getTimerRate() {
		return 60;
	}

	@Override
	protected void writeTimerTick() throws IOException {
		flushAll();
		os.write(OldDemoFormat.TICK);
		ticks60++;
	}
	
	@Override
	protected void writeSpeechEvent(IDemoEvent event) throws IOException {
		ISpeechEvent ev = (ISpeechEvent) event;
		if (ev.getCode() != ISpeechEvent.SPEECH_ADDING_BYTE || !speechBuffer.isAvailable(2)) {
			speechBuffer.flush();
		}

		speechBuffer.push((byte) ev.getCode());
		speechBuffer.push(ev.getAddedByte());
	}

	@Override
	protected void writeSoundRegisterEvent(IDemoEvent event)
			throws IOException {
		throw new IOException("this demo format does not support sound registers");
	}

	@Override
	protected void writeSoundDataEvent(IDemoEvent event)
			throws IOException {
		SoundWriteDataEvent ev = (SoundWriteDataEvent) event;
		byte[] data = ev.getData();
		soundDataBuffer.pushData(data, 0, ev.getLength());
	}

	@Override
	protected void writeVideoDataEvent(IDemoEvent event)
			throws IOException {
		VideoWriteDataEvent we = (VideoWriteDataEvent) event;
		int len = we.getLength();
		int offs = 0;
		while (len > 0) {
			int toUse = Math.min(255, len);
			if (!videoBuffer.isAvailable(toUse + 3)) {
				videoBuffer.flush();
			}
			videoBuffer.pushWord(we.getAddress() + offs);
			videoBuffer.push((byte) toUse);
			videoBuffer.pushData(we.getData(), offs + we.getOffset(), toUse);
			
			len -= toUse;
			offs += toUse;
		}
	}

	@Override
	protected void writeVideoRegisterEvent(IDemoEvent event)
			throws IOException {
		if (!videoBuffer.isAvailable(2)) {
			videoBuffer.flush();
		}
		videoBuffer.pushWord(((VideoWriteRegisterEvent) event).getAddr());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoOutputStream#getElapsedTime()
	 */
	@Override
	public long getElapsedTime() {
		return ticks60 * 1000 / 60;
	}
}
