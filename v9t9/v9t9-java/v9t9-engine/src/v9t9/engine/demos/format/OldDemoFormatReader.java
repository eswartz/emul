/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.IOException;
import java.io.InputStream;

import v9t9.common.demo.IDemoInputStream;
import v9t9.common.events.NotifyException;
import v9t9.engine.demos.events.SoundWriteRegisterEvent;
import v9t9.engine.demos.events.SpeechWriteEvent;
import v9t9.engine.demos.events.TimerTick;
import v9t9.engine.demos.events.VideoWriteDataEvent;
import v9t9.engine.demos.events.VideoWriteRegisterEvent;


/**
 * Reader for TI Emulator v6.0 & V9t9 demo formats
 * @author ejs
 *
 */
public class OldDemoFormatReader extends BaseDemoFormatReader implements IDemoInputStream {

	private int ticks60;

	public OldDemoFormatReader(InputStream is) throws IOException {
		super(is);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoInputStream#getTimerRate()
	 */
	@Override
	public int getTimerRate() {
		return 60;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.server.demo.BaseDemoFormatReader#queueTimerTickEvent()
	 */
	@Override
	protected void queueTimerTickEvent() throws IOException, NotifyException {
		ticks60++;
		queuedEvents.add(new TimerTick(getElapsedTime()));
	}
	
	/**
	 * @throws IOException
	 * @throws NotifyException 
	 */
	@Override
	protected void queueSoundEvents() throws IOException, NotifyException {
		CommonDemoFormat.queueSoundEvents(queuedEvents, soundBuffer);
	}

	/**
	 * @throws IOException
	 * @throws NotifyException 
	 */
	@Override
	protected void queueVideoEvents() throws IOException, NotifyException {
		// collection of video events
		videoBuffer.refill();
		
		// parse events
		while (videoBuffer.isAvailable()) {
			int addr = videoBuffer.readWord(); 
			if ((addr & 0x8000) != 0) {
				queuedEvents.add(new VideoWriteRegisterEvent(addr));
			} else {
				int chunkLength = videoBuffer.read() & 0xff; 
				byte[] chunk = videoBuffer.readData(chunkLength);
				queuedEvents.add(new VideoWriteDataEvent(addr & 0x3fff, chunk));
			}
		}
	}


	/**
	 * @throws NotifyException 
	 */
	@Override
	protected void queueSpeechEvents() throws IOException, NotifyException {
		// collection of speech events
		speechBuffer.refill();
		
		// parse events
		while (speechBuffer.isAvailable()) {
			int byt = speechBuffer.read();  
			if (byt != DemoFormat.SpeechEvent.ADDING_BYTE.getCode()) {
				try {
					queuedEvents.add(new SpeechWriteEvent(DemoFormat.SpeechEvent.fromCode(byt), 0));
				} catch (IllegalArgumentException e) {
					throw speechBuffer.newBufferException("corrupt speech byte " + Integer.toHexString(byt));
				}
				
				// ignore next byte (always emitted in old format)
				speechBuffer.read();
			} else {
				byt = speechBuffer.read() & 0xff;  
				queuedEvents.add(new SpeechWriteEvent(DemoFormat.SpeechEvent.ADDING_BYTE, byt));
			}
		}
	}
	
	/**
	 * @throws NotifyException 
	 */
	@Override
	protected void queueSoundRegEvents() throws IOException, NotifyException {
		soundRegsBuffer.refill();
		
		// parse events
		while (soundRegsBuffer.isAvailable()) {
			int reg = (short) soundRegsBuffer.readWord();
			int val = soundRegsBuffer.readWord() & 0xffff;  
			queuedEvents.add(new SoundWriteRegisterEvent(reg, val));
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoInputStream#getElapsedTime()
	 */
	@Override
	public long getElapsedTime() {
		return ticks60 * 1000L / 60;
	}
}
