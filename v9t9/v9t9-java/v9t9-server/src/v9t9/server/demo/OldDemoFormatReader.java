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
 * Reader for TI Emulator v6.0 & V9t9 demo formats
 * @author ejs
 *
 */
public class OldDemoFormatReader extends BaseDemoFormatReader implements IDemoInputStream {

	public OldDemoFormatReader(InputStream is) throws IOException {
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
		return 60;
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
		CommonDemoFormat.queueSpeechEvents(queuedEvents, speechBuffer);
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
