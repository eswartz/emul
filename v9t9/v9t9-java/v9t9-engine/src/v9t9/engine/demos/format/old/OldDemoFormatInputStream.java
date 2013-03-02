/*
  OldDemoFormatInputStream.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.format.old;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoInputStream;
import v9t9.engine.demos.events.OldSpeechEvent;
import v9t9.engine.demos.events.SoundWriteDataEvent;
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

	private Map<Integer, OldDemoInputBuffer> codeToBufferMap = 
			new HashMap<Integer, OldDemoInputBuffer>();
	private int ticks60;

	public OldDemoFormatInputStream(InputStream is_) throws IOException {
		super(is_);

		videoBuffer = registerStandardBuffer(new OldDemoInputBuffer(
				is, "video", OldDemoFormat.VIDEO,
				OldDemoFormat.VIDEO_BUFFER_SIZE) {
			@Override
			public void decodeEvents(Queue<IDemoEvent> queuedEvents)
					throws IOException {
				queueVideoEvents(queuedEvents);
			}
		});
		soundBuffer = registerStandardBuffer(new OldDemoInputBuffer(
				is, "sound", OldDemoFormat.SOUND,
						OldDemoFormat.SOUND_BUFFER_SIZE) {
			/* (non-Javadoc)
			 * @see v9t9.common.demo.IDemoInputBuffer#decodeEvents(java.util.Queue)
			 */
			@Override
			public void decodeEvents(
					Queue<IDemoEvent> queuedEvents)
					throws IOException {
				queueSoundEvents(queuedEvents);
			}
		});
		speechBuffer = registerStandardBuffer(new OldDemoInputBuffer(
				is, "speech", OldDemoFormat.SPEECH,
				OldDemoFormat.SPEECH_BUFFER_SIZE) {
			/* (non-Javadoc)
			 * @see v9t9.common.demo.IDemoInputBuffer#decodeEvents(java.util.Queue)
			 */
			@Override
			public void decodeEvents(Queue<IDemoEvent> queuedEvents)
					throws IOException {
				queueSpeechEvents(queuedEvents);
			}
		});
	}

	/**
	 * @param buffer
	 * @return
	 */
	private OldDemoInputBuffer registerStandardBuffer(
			OldDemoInputBuffer buffer) {
		codeToBufferMap.put(buffer.getCode(), buffer);
		return buffer;
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
		else {
			OldDemoInputBuffer buffer = codeToBufferMap.get(kind);
			if (buffer == null)
				throw newFormatException("unrecognized buffer type " + Integer.toHexString(kind));
			
			buffer.decodeEvents(queuedEvents);
		}
	}

	protected void queueTimerTickEvent() throws IOException {
		ticks60++;
		queuedEvents.add(new TimerTick(getElapsedTime()));
	}
	
	protected void queueSoundEvents(Queue<IDemoEvent> queuedEvents) throws IOException {
		// collection of sound events
		soundBuffer.refill();
		
		// blast all the data to the same address
		queuedEvents.add(new SoundWriteDataEvent(0x8400, soundBuffer.readRest()));
	}

	protected void queueVideoEvents(Queue<IDemoEvent> queuedEvents) throws IOException {
		// collection of video events (register writes and memory sets)
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

	protected void queueSpeechEvents(Queue<IDemoEvent> queuedEvents) throws IOException {
		// collection of speech events
		speechBuffer.refill();
		
		// parse events
		while (speechBuffer.isAvailable()) {
			int code = speechBuffer.read();
			byte byt = (byte) speechBuffer.read();  // byte follows every command, even for commands not using it
			if (code == OldSpeechEvent.SPEECH_ADDING_BYTE) {
				queuedEvents.add(new OldSpeechEvent(byt));
			} else if (code <= OldSpeechEvent.SPEECH_STOPPING) {
				OldSpeechEvent ev = new OldSpeechEvent(code);
				if (ev == null) {
					throw speechBuffer.newBufferException("corrupt speech byte " + Integer.toHexString(code));
				}
				queuedEvents.add(ev);
			} else {
				// uh... what?  
				throw speechBuffer.newBufferException("corrupt speech byte " + Integer.toHexString(code));
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
