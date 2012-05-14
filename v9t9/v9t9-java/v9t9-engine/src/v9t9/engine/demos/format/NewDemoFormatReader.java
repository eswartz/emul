/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import v9t9.common.demo.IDemoInputStream;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachineModel;
import v9t9.engine.demos.events.SoundWriteRegisterEvent;
import v9t9.engine.demos.events.TimerTick;
import v9t9.engine.demos.events.VideoWriteDataEvent;
import v9t9.engine.demos.events.VideoWriteRegisterEvent;


/**
 * Reader for new format, using variable-length registers and values.
 * @author ejs
 *
 */
public class NewDemoFormatReader extends BaseDemoFormatReader implements IDemoInputStream {

	private int ticks100;

	public NewDemoFormatReader(IMachineModel machineModel, InputStream is_) throws IOException, NotifyException {
		super(is_);

		// expect machine identifier
		is.mark(1);
		if (is.read() == 0x7f) {
			StringBuilder sb = new StringBuilder();
			int ch;
			while ((ch = is.read()) > 0) {
				sb.append((char) ch);
			}
			if (!machineModel.getIdentifier().equals(sb.toString())) {
				throw new NotifyException(null,
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
		int count = is.read();  
		while (count-- > 0) {
			ticks100++;
			queuedEvents.add(new TimerTick(getElapsedTime()));
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
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoInputStream#getElapsedTime()
	 */
	@Override
	public long getElapsedTime() {
		return ticks100 * 1000L / 100;
	}

}
