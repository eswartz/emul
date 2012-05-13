/**
 * 
 */
package v9t9.server.demo;

import java.io.IOException;
import java.io.InputStream;

import v9t9.common.demo.IDemoInputStream;
import v9t9.common.events.NotifyException;
import v9t9.server.demo.events.SoundWriteRegisterEvent;
import v9t9.server.demo.events.TimerTick;
import v9t9.server.demo.events.VideoWriteDataEvent;
import v9t9.server.demo.events.VideoWriteRegisterEvent;


/**
 * Reader for new format, using variable-length registers and values.
 * @author ejs
 *
 */
public class NewDemoFormatReader extends BaseDemoFormatReader implements IDemoInputStream {

	public NewDemoFormatReader(InputStream is) throws IOException {
		super(is);

		// skip header
		is.read(new byte[DemoFormat.DEMO_MAGIC_HEADER_LENGTH]);
		isPos += DemoFormat.DEMO_MAGIC_HEADER_LENGTH;
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoInputStream#getTimerRate()
	 */
	@Override
	public int getTimerRate() {
		return 100;
	}

	@Override
	protected void queueTimerTickEvent() throws IOException, NotifyException {
		queuedEvents.add(new TimerTick());		
	}
	
	@Override
	protected void queueSoundEvents() throws IOException, NotifyException {
		CommonDemoFormat.queueSoundEvents(queuedEvents, soundBuffer);
	}

	@Override
	protected void queueVideoEvents() throws IOException, NotifyException {
		// collection of video events
		videoBuffer.refill();
		
		// parse events
		while (videoBuffer.isAvailable()) {
			int regOrAddr = videoBuffer.readVar(); 
			int chunkLength = videoBuffer.read() & 0xff;
			if (chunkLength == 0) {
				// register
				int regVal = videoBuffer.readVar(); 
				queuedEvents.add(new VideoWriteRegisterEvent(regOrAddr, regVal));
			} else {
				byte[] chunk = videoBuffer.readData(chunkLength);
				queuedEvents.add(new VideoWriteDataEvent(regOrAddr, chunk));
			}
		}
	}


	@Override
	protected void queueSpeechEvents() throws IOException, NotifyException {
		CommonDemoFormat.queueSpeechEvents(queuedEvents, speechBuffer);
	}

	@Override
	protected void queueSoundRegEvents() throws IOException, NotifyException {
		soundRegsBuffer.refill();
		
		// parse events
		while (soundRegsBuffer.isAvailable()) {
			int reg = soundRegsBuffer.readVar();
			int val = soundRegsBuffer.readVar();  
			queuedEvents.add(new SoundWriteRegisterEvent(reg, val));
		}
	}

}
