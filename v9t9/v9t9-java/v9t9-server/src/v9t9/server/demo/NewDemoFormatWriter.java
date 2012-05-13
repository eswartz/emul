/**
 * 
 */
package v9t9.server.demo;

import java.io.IOException;
import java.io.OutputStream;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoOutputStream;
import v9t9.common.machine.IMachine;
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

	private int timerTicks;

	public NewDemoFormatWriter(IMachine machine, OutputStream os) throws IOException {
		super(os);
		
		os.write(DemoFormat.DEMO_MAGIC_HEADER_V9t9);
		timerTicks = 0;
		
		// machine identifier
		os.write(0x7f);
		byte[] id = machine.getModel().getIdentifier().getBytes();
		os.write(id);
		os.write(0);
		
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoOutputStream#getTimerRate()
	 */
	@Override
	public int getTimerRate() {
		return 100;
	}

	/* (non-Javadoc)
	 * @see v9t9.server.demo.BaseDemoFormatWriter#purge()
	 */
	@Override
	protected void purge() throws IOException {
		super.purge();
		emitTimerTick();
	}

	/**
	 * @throws IOException
	 */
	protected void emitTimerTick() throws IOException {
		if (timerTicks > 0) {
			os.write(BufferType.TICK.getCode());
			os.write(timerTicks);
			timerTicks = 0;
		}
	}
	
	@Override
	protected void writeTimerTick() throws IOException {
		boolean wrote = flushAll();
		++timerTicks;
		if (wrote || timerTicks == 255) {
			emitTimerTick();
		}
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
		soundRegsBuffer.pushVar(ev.getReg() & 0x7fffffff);
		soundRegsBuffer.pushVar(ev.getVal() & 0x7fffffff);
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
		
		byte[] data = we.getData();
		
		RleSegmenter segmenter = new RleSegmenter(8, data, we.getOffset(), we.getLength());
		for (RleSegmenter.Segment segment : segmenter) {
			int addr = segment.getOffset() - we.getOffset() + we.getAddress();
			if (!videoBuffer.isAvailable(12)) {
				videoBuffer.flush();
			}
			if (segment.isRepeat()) {
				videoBuffer.pushVar(addr);
				videoBuffer.pushVar(- segment.getLength());
				videoBuffer.push(data[segment.getOffset()]);
			}
			else {
				videoBuffer.pushVar(addr);
				videoBuffer.pushVar(segment.getLength());
				videoBuffer.pushData(data, segment.getOffset(), segment.getLength());
				
			}
		}
		
//		final int totalLength = we.getLength();
//		int rest = totalLength;
//		int offs = 0;
//		while (rest > 0) {
//			videoBuffer.pushVar(we.getAddress() + offs);
//			
//			// split into zeroes and non-zeroes
//			int toUse;
//			byte[] data = we.getData();
//			boolean isZero = data[offs] == 0;
//			if (isZero) {
//				toUse = 1;
//				while (offs + toUse < totalLength
//						&& data[offs + toUse] == 0) {
//					toUse++;
//				}
//				// overhead for a single zero byte wouldn't be worthwhile
//				if (toUse < 4 && offs + toUse < totalLength) {
//					isZero = false;
//				}
//			} else {
//				toUse = 0;
//			}
//			
//			if (isZero) {
//				videoBuffer.pushVar(-toUse);
//			} else {
//				toUse = Math.min(255, rest);
//				
//				videoBuffer.pushVar(toUse);
//				if (!videoBuffer.isAvailable(toUse)) {
//					videoBuffer.flush();
//				}
//				videoBuffer.pushData(data, offs + we.getOffset(), toUse);
//			}
//			rest -= toUse;
//			offs += toUse;
//		}
	}

	@Override
	protected void writeVideoRegisterEvent(IDemoEvent event)
			throws IOException {
		if (!videoBuffer.isAvailable(4)) {
			videoBuffer.flush();
		}
		VideoWriteRegisterEvent ev = (VideoWriteRegisterEvent) event;
		videoBuffer.pushVar(ev.getReg() & 0x7fffffff);
		videoBuffer.pushVar(0);	// this is the cue that it's a register write
		videoBuffer.pushVar(ev.getVal() & 0x7fffffff);
	}
}
