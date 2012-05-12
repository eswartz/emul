/**
 * 
 */
package v9t9.server.demo;

import java.io.IOException;
import java.io.InputStream;

import v9t9.common.demo.IDemoInputStream;
import v9t9.common.events.NotifyException;
import v9t9.server.demo.events.SoundWriteDataEvent;
import v9t9.server.demo.events.SoundWriteRegisterEvent;
import v9t9.server.demo.events.SpeechWriteEvent;
import v9t9.server.demo.events.TimerTick;
import v9t9.server.demo.events.VideoWriteDataEvent;
import v9t9.server.demo.events.VideoWriteRegisterEvent;


public class OldDemoFormatReader extends BaseDemoFormatReader implements IDemoInputStream {

	public OldDemoFormatReader(InputStream is) throws IOException {
		super(is);

		// skip header
		is.read(new byte[DemoFormat.DEMO_MAGIC_HEADER_LENGTH]);
		isPos += DemoFormat.DEMO_MAGIC_HEADER_LENGTH;
		
	}

	/* (non-Javadoc)
	 * @see v9t9.server.demo.BaseDemoFormatReader#queueTimerTickEvent()
	 */
	@Override
	protected void queueTimerTickEvent() throws IOException, NotifyException {
		queuedEvents.add(new TimerTick());		
	}
	
	/**
	 * @throws IOException
	 * @throws NotifyException 
	 */
	@Override
	protected void queueSoundEvents() throws IOException, NotifyException {
		// collection of sound events
		soundBuffer.refill();
		
		// blast all the data to the same address
		queuedEvents.add(new SoundWriteDataEvent(0x8400, soundBuffer.readRest()));
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
				if (byt == 255) {
					// bug in TI Emulator 6.0
					continue;
				}
				try {
					queuedEvents.add(new SpeechWriteEvent(DemoFormat.SpeechEvent.fromCode(byt), 0));
				} catch (IllegalArgumentException e) {
					throw new NotifyException(null, "corrupt speech byte " + Integer.toHexString(byt) + " at " 
							+ Integer.toHexString(speechBuffer.getEffectivePos()));
				}
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

}
