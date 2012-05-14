/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.IOException;
import java.util.Queue;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.events.NotifyException;
import v9t9.engine.demos.events.SoundWriteDataEvent;

/**
 * @author ejs
 *
 */
public class CommonDemoFormat {
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
