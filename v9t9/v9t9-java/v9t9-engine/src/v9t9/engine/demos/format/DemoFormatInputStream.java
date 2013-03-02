/*
  DemoFormatInputStream.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import v9t9.common.demos.DemoHeader;
import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoEventFormatter;
import v9t9.common.demos.IDemoInputEventBuffer;
import v9t9.common.demos.IDemoInputStream;
import v9t9.common.machine.IMachineModel;
import v9t9.engine.demos.events.TimerTick;
import v9t9.engine.demos.stream.BaseDemoInputStream;


/**
 * Reader for new format, using variable-length registers and values.
 * @author ejs
 *
 */
public class DemoFormatInputStream extends BaseDemoInputStream implements IDemoInputStream {

	private int ticks;
	private DemoHeader header;
	private Map<Integer, IDemoInputEventBuffer> buffers = new HashMap<Integer, IDemoInputEventBuffer>();

	private Set<Integer> unknownBuffers = new HashSet<Integer>();
	
	public DemoFormatInputStream(IMachineModel machineModel, InputStream is_) throws IOException {
		super(is_);

		header = new DemoHeader();
		header.read(is);
		
		if (!machineModel.isModelCompatible(header.getMachineModel())) {
			throw new IOException(
					"Note: this demo is incompatible with the "+
					"current machine: " + header.getMachineModel() + " expected");
		}
		
		for (Map.Entry<Integer, String> ent : header.getBufferIdentifierMap().entrySet()) {
			final IDemoEventFormatter formatter = DemoFormat.FORMATTER_REGISTRY.findFormatterByBuffer(
					ent.getValue());
			DemoInputEventBuffer buffer;
			if (formatter != null) {
				buffer = new DemoFormatterInputEventBuffer(is, ent.getKey(),
						ent.getValue(), formatter);
			}
			else {
				// callers should invoke #registerBuffer later -- or not at all
				buffer = new DemoInputEventBuffer(is, ent.getKey(),
						ent.getValue()) {
					
					@Override
					public void decodeEvents(Queue<IDemoEvent> queuedEvents) throws IOException {
						if (!unknownBuffers.contains(getCode())) {
							System.err.println("0x" + Long.toHexString(getPosition()) + ": unrecognized code " + getCode() + " encountered");
							unknownBuffers.add(getCode());
						}
						readRest();
					}
				};
			}
			registerBuffer(buffer);
		}
	}
	
	public void registerBuffer(IDemoInputEventBuffer buffer) {
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
				//System.out.println(Long.toHexString(is.getPosition()) + ": tick" );
				queueTimerTickEvent();
				continue;
			}
			
			IDemoInputEventBuffer buffer = buffers.get(code);
			if (buffer == null) {
				throw new IOException("unknown buffer for " + code);
			}
			
			//System.out.println(Long.toHexString(is.getPosition()) + ": " + 
			//		buffer.getIdentifier());	
			
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
	

}
