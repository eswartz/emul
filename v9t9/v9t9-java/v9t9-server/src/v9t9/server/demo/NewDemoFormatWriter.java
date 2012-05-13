/**
 * 
 */
package v9t9.server.demo;

import java.io.IOException;
import java.io.OutputStream;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoOutputStream;
import v9t9.server.demo.DemoFormat.BufferType;
import v9t9.server.demo.events.SoundWriteRegisterEvent;
import v9t9.server.demo.events.VideoWriteDataEvent;
import v9t9.server.demo.events.VideoWriteRegisterEvent;

/**
 * Write demos in new format more amenable to a variable
 * machine model:  register values are written in a variable-length
 * encoding to ensure they can be any length, but without making
 * every register write large.
 * @author ejs
 *
 */
public class NewDemoFormatWriter extends BaseDemoFormatWriter implements IDemoOutputStream {

	public NewDemoFormatWriter(OutputStream os) throws IOException {
		super(os);
		
		os.write(DemoFormat.DEMO_MAGIC_HEADER_V9t9);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoOutputStream#getTimerRate()
	 */
	@Override
	public int getTimerRate() {
		return 100;
	}

	@Override
	protected void writeTimerTick() throws IOException {
		flushAll();
		os.write(BufferType.TICK.getCode());
	}
	
	@Override
	protected void writeSpeechEvent(IDemoEvent event) throws IOException {
		CommonDemoFormat.writeSpeechEvent(event, speechBuffer);
	}

	@Override
	protected void writeSoundRegisterEvent(IDemoEvent event)
			throws IOException {
		if (!soundRegsBuffer.isAvailable(3)) {
			soundRegsBuffer.flush();
		}
		SoundWriteRegisterEvent ev = (SoundWriteRegisterEvent) event;
		soundRegsBuffer.pushVar(ev.getReg());
		soundRegsBuffer.pushVar(ev.getVal());
	}

	@Override
	protected void writeSoundDataEvent(IDemoEvent event)
			throws IOException {
		CommonDemoFormat.writeSoundDataEvent(event, soundDataBuffer);
	}

	@Override
	protected void writeVideoDataEvent(IDemoEvent event)
			throws IOException {
		VideoWriteDataEvent we = (VideoWriteDataEvent) event;
		int len = we.getLength();
		int offs = 0;
		while (len > 0) {
			int toUse = Math.min(255, len);
			videoBuffer.pushVar(we.getAddress() + offs);
			videoBuffer.push((byte) toUse);
			if (!videoBuffer.isAvailable(toUse)) {
				videoBuffer.flush();
			}
			videoBuffer.pushData(we.getData(), offs + we.getOffset(), toUse);
			
			len -= toUse;
			offs += toUse;
		}
	}

	@Override
	protected void writeVideoRegisterEvent(IDemoEvent event)
			throws IOException {
		if (!videoBuffer.isAvailable(4)) {
			videoBuffer.flush();
		}
		VideoWriteRegisterEvent ev = (VideoWriteRegisterEvent) event;
		videoBuffer.pushVar(ev.getReg());
		videoBuffer.push((byte) 0);	// this is the cue that it's a register write
		videoBuffer.pushVar(ev.getVal());
	}
}