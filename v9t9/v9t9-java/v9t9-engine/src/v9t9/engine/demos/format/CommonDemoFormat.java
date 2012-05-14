/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.IOException;
import java.util.Queue;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.events.NotifyException;
import v9t9.engine.demos.events.SoundWriteDataEvent;
import v9t9.engine.demos.events.SpeechWriteEvent;
import v9t9.engine.demos.format.DemoFormat.SpeechEvent;

/**
 * @author ejs
 *
 */
public class CommonDemoFormat {

	/**
	 * @param queuedEvents
	 * @param speechBuffer
	 */
	public static void queueSpeechEvents(Queue<IDemoEvent> queuedEvents,
			DemoReadBuffer speechBuffer) throws IOException, NotifyException {
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
				
				// ignore next byte
				speechBuffer.read();
			} else {
				byt = speechBuffer.read() & 0xff;  
				queuedEvents.add(new SpeechWriteEvent(DemoFormat.SpeechEvent.ADDING_BYTE, byt));
			}
		}
	}

	/**
	 * @param event
	 * @param speechBuffer
	 * @throws IOException 
	 */
	public static void writeSpeechEvent(IDemoEvent event,
			DemoOutBuffer speechBuffer) throws IOException {
		SpeechWriteEvent ev = (SpeechWriteEvent) event;
		
		if (ev.getEvent() != SpeechEvent.ADDING_BYTE || !speechBuffer.isAvailable(2)) {
			speechBuffer.flush();
		}

		speechBuffer.push((byte) ev.getEvent().getCode());
		speechBuffer.push((byte) ev.getAddedByte());
	}

	/**
	 * @param event
	 * @param soundDataBuffer
	 * @throws IOException 
	 */
	public static void writeSoundDataEvent(IDemoEvent event,
			DemoOutBuffer soundDataBuffer) throws IOException {
		SoundWriteDataEvent ev = (SoundWriteDataEvent) event;
		byte[] data = ev.getData();
		soundDataBuffer.pushData(data, 0, ev.getLength());		
	}

	/**
	 * @param queuedEvents
	 * @param soundBuffer
	 * @throws NotifyException 
	 * @throws IOException 
	 */
	public static void queueSoundEvents(Queue<IDemoEvent> queuedEvents,
			DemoReadBuffer soundBuffer) throws IOException, NotifyException {
		// collection of sound events
		soundBuffer.refill();
		
		// blast all the data to the same address
		queuedEvents.add(new SoundWriteDataEvent(0x8400, soundBuffer.readRest()));
		
	}

}
