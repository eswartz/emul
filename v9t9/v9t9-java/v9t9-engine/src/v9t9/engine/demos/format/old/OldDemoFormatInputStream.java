/**
 * 
 */
package v9t9.engine.demos.format.old;

import java.io.IOException;
import java.io.InputStream;

import v9t9.common.demo.IDemoInputStream;
import v9t9.common.demo.ISpeechEvent;
import v9t9.engine.demos.events.SoundWriteDataEvent;
import v9t9.engine.demos.events.SpeechEvent;
import v9t9.engine.demos.events.TimerTick;
import v9t9.engine.demos.events.VideoWriteDataEvent;
import v9t9.engine.demos.events.VideoWriteRegisterEvent;
import v9t9.engine.demos.stream.BaseDemoInputStream;


/**
 * Reader for TI Emulator v6.0 & V9t9 demo formats
 * @author ejs
 *
 */
public class OldDemoFormatInputStream extends BaseDemoInputStream implements IDemoInputStream {
	protected OldDemoInputBuffer soundBuffer;
	protected OldDemoInputBuffer speechBuffer;
	protected OldDemoInputBuffer videoBuffer;

	private int ticks60;

	public OldDemoFormatInputStream(InputStream is_) throws IOException {
		super(is_);

		videoBuffer = new OldDemoInputBuffer(is, "video", OldDemoFormat.VIDEO,
				OldDemoFormat.VIDEO_BUFFER_SIZE);
		soundBuffer = new OldDemoInputBuffer(is, "sound", OldDemoFormat.SOUND,
						OldDemoFormat.SOUND_BUFFER_SIZE);
		speechBuffer = new OldDemoInputBuffer(is, "speech", OldDemoFormat.SPEECH,
				OldDemoFormat.SPEECH_BUFFER_SIZE);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoInputStream#getTimerRate()
	 */
	@Override
	public int getTimerRate() {
		return 60;
	}
	
	protected void ensureEvents() throws IOException {
		
		int kind = getInputStream().read();  
		if (kind < 0)
			return;
		
		if (kind == OldDemoFormat.TICK) {
			queueTimerTickEvent();
		}
		else if (kind == OldDemoFormat.VIDEO) {
			queueVideoEvents();
		}
		else if (kind == OldDemoFormat.SOUND) {
			queueSoundEvents();
		}
		else if (kind == OldDemoFormat.SPEECH) {
			queueSpeechEvents();
		}
		else {
			// urf
			throw newFormatException("unrecognized buffer type " + Integer.toHexString(kind));
		}
		
	}

	protected void queueTimerTickEvent() throws IOException {
		ticks60++;
		queuedEvents.add(new TimerTick(getElapsedTime()));
	}
	
	protected void queueSoundEvents() throws IOException {
		// collection of sound events
		soundBuffer.refill();
		
		// blast all the data to the same address
		queuedEvents.add(new SoundWriteDataEvent(0x8400, soundBuffer.readRest()));
	}

	protected void queueVideoEvents() throws IOException {
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

	protected void queueSpeechEvents() throws IOException {
		// collection of speech events
		speechBuffer.refill();
		
		// parse events
		while (speechBuffer.isAvailable()) {
			int code = speechBuffer.read();
			int byt = speechBuffer.read() & 0xff;  // dummy byte follows every command
			if (code == ISpeechEvent.SPEECH_ADDING_BYTE) {
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
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoInputStream#getElapsedTime()
	 */
	@Override
	public long getElapsedTime() {
		return ticks60 * 1000L / 60;
	}
}
