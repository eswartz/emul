/**
 * 
 */
package v9t9.server.demo;

import java.io.IOException;
import java.io.OutputStream;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoOutputStream;
import v9t9.server.demo.DemoFormat.BufferType;
import v9t9.server.demo.DemoFormat.SpeechEvent;
import v9t9.server.demo.events.SoundWriteDataEvent;
import v9t9.server.demo.events.SoundWriteRegisterEvent;
import v9t9.server.demo.events.SpeechWriteEvent;
import v9t9.server.demo.events.VideoWriteDataEvent;
import v9t9.server.demo.events.VideoWriteRegisterEvent;

public class OldDemoFormatWriter extends BaseDemoFormatWriter implements IDemoOutputStream {

	public OldDemoFormatWriter(OutputStream os) throws IOException {
		super(os);
		
		os.write(DemoFormat.DEMO_MAGIC_HEADER_V910);
	}
	

	/**
	 * @throws IOException
	 */
	@Override
	protected void writeTimerTick() throws IOException {
		flushAll();
		os.write(BufferType.TICK.getCode());
	}
	
	/**
	 * @param event
	 * @throws IOException
	 */
	@Override
	protected void writeSpeechEvent(IDemoEvent event) throws IOException {
		if (!speechBuffer.isAvailable(2)) {
			speechBuffer.flush();
		}

		SpeechWriteEvent ev = (SpeechWriteEvent) event;
		speechBuffer.push((byte) ev.getEvent().getCode());
		if (ev.getEvent() == SpeechEvent.ADDING_BYTE) {
			speechBuffer.push((byte) ev.getAddedByte());
		}
	}

	/**
	 * @param event
	 * @throws IOException
	 */
	@Override
	protected void writeSoundRegisterEvent(IDemoEvent event)
			throws IOException {
		if (!soundRegsBuffer.isAvailable(3)) {
			soundRegsBuffer.flush();
		}
		SoundWriteRegisterEvent ev = (SoundWriteRegisterEvent) event;
		soundRegsBuffer.pushWord(ev.getReg());
		soundRegsBuffer.pushWord(ev.getVal());
	}

	/**
	 * @param event
	 * @throws IOException
	 */
	@Override
	protected void writeSoundDataEvent(IDemoEvent event)
			throws IOException {
		SoundWriteDataEvent ev = (SoundWriteDataEvent) event;
		byte[] data = ev.getData();
		soundDataBuffer.pushData(data, 0, ev.getLength());
	}

	/**
	 * @param event
	 * @throws IOException
	 */
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

	/**
	 * @param event
	 * @throws IOException
	 */
	@Override
	protected void writeVideoRegisterEvent(IDemoEvent event)
			throws IOException {
		if (!videoBuffer.isAvailable(2)) {
			videoBuffer.flush();
		}
		videoBuffer.pushWord(((VideoWriteRegisterEvent) event).getAddr());
	}
}
