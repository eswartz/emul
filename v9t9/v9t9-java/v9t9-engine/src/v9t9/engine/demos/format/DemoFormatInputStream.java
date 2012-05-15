/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoInputStream;
import v9t9.common.demo.ISpeechEvent;
import v9t9.common.machine.IMachineModel;
import v9t9.engine.demos.events.SoundWriteDataEvent;
import v9t9.engine.demos.events.SoundWriteRegisterEvent;
import v9t9.engine.demos.events.SpeechEvent;
import v9t9.engine.demos.events.TimerTick;
import v9t9.engine.demos.events.VideoWriteDataEvent;
import v9t9.engine.demos.events.VideoWriteRegisterEvent;
import v9t9.engine.demos.format.DemoFormat.DemoHeader;
import v9t9.engine.demos.stream.BaseDemoInputStream;


/**
 * Reader for new format, using variable-length registers and values.
 * @author ejs
 *
 */
public class DemoFormatInputStream extends BaseDemoInputStream implements IDemoInputStream {

	private int ticks;
	private DemoHeader header;
	private Map<Integer, DemoInputBuffer> buffers = new HashMap<Integer, DemoInputBuffer>();
	private DemoInputBuffer videoRegsBuffer;
	private DemoInputBuffer videoDataBuffer;
	private DemoInputBuffer soundRegsBuffer;
	private DemoInputBuffer soundDataBuffer;
	private DemoInputBuffer speechBuffer;

	public DemoFormatInputStream(IMachineModel machineModel, InputStream is_) throws IOException {
		super(is_);

		header = new DemoHeader();
		header.read(is);
		
		if (!machineModel.getIdentifier().equals(header.getMachineModel())) {
			throw new IOException(
					"Note: this demo is incompatible with the "+
					"current machine: " + header.getMachineModel() + " expected");
		}
		
		for (Map.Entry<Integer, String> ent : header.getBufferIdentifierMap().entrySet()) {
			if (ent.getValue().equals(DemoFormat.VIDEO_REGS)) {
				videoRegsBuffer = new DemoInputBuffer(is, ent.getKey(), ent.getValue()) {
					@Override
					public void decodeEvents(Queue<IDemoEvent> queuedEvents)
							throws IOException {
						queueVideoRegEvents();
					}
				};
				registerBuffer(videoRegsBuffer);
			}
			else if (ent.getValue().equals(DemoFormat.VIDEO_DATA)) {
				videoDataBuffer = new DemoInputBuffer(is, ent.getKey(), ent.getValue()) {
					@Override
					public void decodeEvents(Queue<IDemoEvent> queuedEvents)
							throws IOException {
						queueVideoDataEvents();
					}
				};
				registerBuffer(videoDataBuffer);
			}
			else if (ent.getValue().equals(DemoFormat.SOUND_REGS)) {
				soundRegsBuffer = new DemoInputBuffer(is, ent.getKey(), ent.getValue()) {
					@Override
					public void decodeEvents(Queue<IDemoEvent> queuedEvents)
							throws IOException {
						queueSoundRegEvents();
					}
				};
				registerBuffer(soundRegsBuffer);
			}
			else if (ent.getValue().equals(DemoFormat.SOUND_DATA)) {
				soundDataBuffer = new DemoInputBuffer(is, ent.getKey(), ent.getValue()) {
					@Override
					public void decodeEvents(Queue<IDemoEvent> queuedEvents)
							throws IOException {
						queueSoundEvents();
					}
				};
				registerBuffer(soundDataBuffer);
			}
			else if (ent.getValue().equals(DemoFormat.SPEECH_PHRASES)) {
				speechBuffer = new DemoInputBuffer(is, ent.getKey(), ent.getValue()) {
					@Override
					public void decodeEvents(Queue<IDemoEvent> queuedEvents)
							throws IOException {
						queueSpeechEvents();
					}
				};
				registerBuffer(speechBuffer);
			}
			else {
				// callers should invoke #registerBuffer
			}
		}
	}
	
	public void registerBuffer(DemoInputBuffer buffer) {
		buffers.put(buffer.getCode(), buffer);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoInputStream#getTimerRate()
	 */
	@Override
	public int getTimerRate() {
		return header.getTimerRate();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoInputStream#getElapsedTime()
	 */
	@Override
	public long getElapsedTime() {
		return ticks * 1000L / getTimerRate();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.demos.stream.BaseDemoInputStream#ensureEvents()
	 */
	@Override
	protected void ensureEvents() throws IOException {
		while (queuedEvents.isEmpty()) {
			int code = is.read();
			if (code < 0) {
				return;
			}
			
			if (code == 0) {
				System.out.println(Long.toHexString(is.getPosition()) + ": tick" );
				queueTimerTickEvent();
				continue;
			}
			
			DemoInputBuffer buffer = buffers.get(code);
			if (buffer == null)
				throw new IOException("invalid code " + code + " encountered");
			
			System.out.println(Long.toHexString(is.getPosition()) + ": " + buffer.getIdentifier());
			
			// get contents
			buffer.refill();
			
			// decode em
			buffer.decodeEvents(queuedEvents);
		}
		
	}

	protected void queueTimerTickEvent() throws IOException {
		int count = getInputStream().read();  
		while (count-- > 0) {
			ticks++;
			queuedEvents.add(new TimerTick(getElapsedTime()));
		}
	}
	

	protected void queueVideoRegEvents() throws IOException {
		// parse events
		while (videoRegsBuffer.isAvailable()) {
			int regOrAddr = videoRegsBuffer.readVar(); 
			int regVal = videoRegsBuffer.readVar(); 
			//System.err.println("reg: " + Integer.toHexString(regOrAddr) +" = " + regVal);
			queuedEvents.add(new VideoWriteRegisterEvent(regOrAddr, regVal));
		}
	}

	

	protected void queueVideoDataEvents() throws IOException {
		// parse events
		while (videoDataBuffer.isAvailable()) {
			int regOrAddr = videoDataBuffer.readVar(); 
			int chunkLength = videoDataBuffer.readVar();
			if (chunkLength < 0) {
				// RLE repeat
				byte[] chunk = new byte[-chunkLength];
				int val = videoDataBuffer.read();
				//System.err.println("RLE: " + Integer.toHexString(regOrAddr) +" @ " + Integer.toHexString(-chunkLength) +  " = " + Integer.toHexString(val));
				Arrays.fill(chunk, (byte) val);
				queuedEvents.add(new VideoWriteDataEvent(regOrAddr, chunk));
			} else {
				// real data
				//System.err.println("Data: " + Integer.toHexString(regOrAddr) +" @ " + Integer.toHexString(chunkLength));
				byte[] chunk = videoDataBuffer.readData(chunkLength);
				queuedEvents.add(new VideoWriteDataEvent(regOrAddr, chunk));
			}
		}
	}

	protected void queueSoundEvents() throws IOException {
		// blast all the data to the same address
		queuedEvents.add(new SoundWriteDataEvent(0x8400, soundDataBuffer.readRest()));
	}


	protected void queueSpeechEvents() throws IOException {
		// parse events
		while (speechBuffer.isAvailable()) {
			int code = speechBuffer.read();
			if (code == ISpeechEvent.SPEECH_ADDING_BYTE) {
				int byt = speechBuffer.read() & 0xff;
				queuedEvents.add(new SpeechEvent(code, byt));
			} else {
				ISpeechEvent ev = new SpeechEvent(code);
				if (ev == null) {
					throw speechBuffer.newBufferException("corrupt speech byte " + Integer.toHexString(code));
				}
				queuedEvents.add(ev);
			}
		}
	}

	protected void queueSoundRegEvents() throws IOException {
		
		// parse events
		while (soundRegsBuffer.isAvailable()) {
			int reg = soundRegsBuffer.readVar();
			int val = soundRegsBuffer.readVar();  
			queuedEvents.add(new SoundWriteRegisterEvent(reg, val));
		}
	}

}
