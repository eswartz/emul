/**
 * 
 */
package v9t9.server.demo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import v9t9.common.demo.IDemoInputStream;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
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

	public NewDemoFormatReader(IMachine machine, InputStream is) throws IOException, NotifyException {
		super(is);

		// skip header
		is.read(new byte[DemoFormat.DEMO_MAGIC_HEADER_LENGTH]);
		isPos += DemoFormat.DEMO_MAGIC_HEADER_LENGTH;

		// expect machine identifier
		is.mark(1);
		if (is.read() == 0x7f) {
			StringBuilder sb = new StringBuilder();
			int ch;
			while ((ch = is.read()) > 0) {
				sb.append((char) ch);
			}
			if (!machine.getModel().getIdentifier().equals(sb.toString())) {
				machine.getEventNotifier().notifyEvent(null, Level.WARNING,
						"Note: this demo is incompatible with the "+
						"current machine: " + sb + " expected");
			}
		} else {
			// temporary format
			is.reset();
		} 
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
		int count = is.read();  isPos++;
		while (count-- > 0) {
			queuedEvents.add(new TimerTick());
		}
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
			int chunkLength = videoBuffer.readVar();
			if (chunkLength == 0) {
				// register
				int regVal = videoBuffer.readVar(); 
				//System.err.println("reg: " + Integer.toHexString(regOrAddr) +" = " + regVal);
				queuedEvents.add(new VideoWriteRegisterEvent(regOrAddr, regVal));
			} else if (chunkLength < 0) {
				// RLE repeat
				byte[] chunk = new byte[-chunkLength];
				int val = videoBuffer.read();
				//System.err.println("RLE: " + Integer.toHexString(regOrAddr) +" @ " + Integer.toHexString(-chunkLength) +  " = " + Integer.toHexString(val));
				Arrays.fill(chunk, (byte) val);
				queuedEvents.add(new VideoWriteDataEvent(regOrAddr, chunk));
			} else {
				// real data
				//System.err.println("Data: " + Integer.toHexString(regOrAddr) +" @ " + Integer.toHexString(chunkLength));
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
