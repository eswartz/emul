/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.IOException;
import java.io.OutputStream;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoOutputStream;
import v9t9.common.demo.ISpeechEvent;
import v9t9.engine.demos.events.SoundWriteDataEvent;
import v9t9.engine.demos.events.SoundWriteRegisterEvent;
import v9t9.engine.demos.events.VideoWriteDataEvent;
import v9t9.engine.demos.events.VideoWriteRegisterEvent;
import v9t9.engine.demos.format.DemoFormat.DemoHeader;
import v9t9.engine.demos.stream.BaseDemoOutputStream;

/**
 * Write demos in new format more amenable to a non-memory constrained host and
 * a variable machine model.
 * <p/>
 * The header consists of identifying information for the version of V9t9 and
 * the machine demoed, a description and timestamp, a base timing rate, and a
 * table of contents mapping registered buffer identifiers to bytes. Only the
 * tick code (0) is reserved.
 * <p/>
 * The tick code is used to schedule the demo in time. It is followed by a count
 * for the number of ticks represented (which may be zero, but it is expected
 * to be 1 or more). 
 * <p/>
 * Between ticks, zero or more buffers may appear.  Each buffer starts with
 * its code (from the TOC) and a length, and then an array of bytes of that 
 * length.  The interpretation of the contents is up to the registered buffer
 * handler.
 * 
 * 
 * @author ejs
 * 
 */
public class DemoFormatOutputStream extends BaseDemoOutputStream implements IDemoOutputStream {

	private int timerTicks;
	private int ticks;
	private DemoHeader header;
	private DemoOutputBuffer videoRegsBuffer;
	private DemoOutputBuffer videoDataBuffer;
	private DemoOutputBuffer soundRegsBuffer;
	private DemoOutputBuffer speechBuffer;
	private DemoOutputBuffer soundDataBuffer;

	public DemoFormatOutputStream(DemoFormat.DemoHeader header, OutputStream os_) throws IOException {
		super(os_);
		this.header = header;
		
		os.write(DemoFormat.DEMO_MAGIC_HEADER_V9t9);
		timerTicks = 0;
		
		this.videoRegsBuffer = allocateBuffer(DemoFormat.VIDEO_REGS);
		this.videoDataBuffer = allocateBuffer(DemoFormat.VIDEO_DATA);
		this.soundDataBuffer = allocateBuffer(DemoFormat.SOUND_DATA);
		this.soundRegsBuffer = allocateBuffer(DemoFormat.SOUND_REGS);
		this.speechBuffer = allocateBuffer(DemoFormat.SPEECH_PHRASES);

		header.write(os);

	}

	private DemoOutputBuffer allocateBuffer(String id) throws IOException {
		int code = header.findOrAllocateIdentifier(id);
		DemoOutputBuffer buffer = new DemoOutputBuffer(os, code);
		buffers.add(buffer);
		return buffer;
	}

	@Override
	public int getTimerRate() {
		return header.getTimerRate();
	}


	@Override
	public long getElapsedTime() {
		return ticks * 1000L / getTimerRate();
	}
	
	@Override
	protected void preClose() throws IOException {
		super.preClose();
		if (timerTicks > 0) {
			emitTimerTick();
		}
	}

	protected void emitTimerTick() throws IOException {
		os.write(DemoFormat.TICK);
		os.write(timerTicks);
		timerTicks = 0;
	}
	
	@Override
	protected void writeTimerTick() throws IOException {
		if (anythingToFlush() || timerTicks == 255) {
			emitTimerTick();
			flushAll();
		}
		++timerTicks;
		ticks++;
	}
	
	@Override
	protected void writeSpeechEvent(IDemoEvent event) throws IOException {
		ISpeechEvent ev = (ISpeechEvent) event;
		speechBuffer.push((byte) ev.getCode());
		if (ev.getCode() == ISpeechEvent.SPEECH_ADDING_BYTE)
			speechBuffer.push(ev.getAddedByte());
	}

	@Override
	protected void writeSoundRegisterEvent(IDemoEvent event)
			throws IOException {
		SoundWriteRegisterEvent ev = (SoundWriteRegisterEvent) event;
		soundRegsBuffer.pushVar(ev.getReg());
		soundRegsBuffer.pushVar(ev.getVal());
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
		
		videoDataBuffer.pushRleMemoryWriteData(8, we.getAddress(),
				we.getData(), we.getOffset(), we.getLength());
	}

	@Override
	protected void writeVideoRegisterEvent(IDemoEvent event)
			throws IOException {
		VideoWriteRegisterEvent ev = (VideoWriteRegisterEvent) event;
		videoRegsBuffer.pushVar(ev.getReg());
		videoRegsBuffer.pushVar(ev.getVal());
	}
	
}
