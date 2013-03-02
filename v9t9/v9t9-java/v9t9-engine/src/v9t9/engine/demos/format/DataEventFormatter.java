/*
  DataEventFormatter.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.engine.demos.format;

import java.io.IOException;
import java.util.Arrays;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoInputEventBuffer;
import v9t9.common.demos.IDemoOutputEventBuffer;
import v9t9.engine.demos.events.WriteDataBlock;

/**
 * @author ejs
 *
 */
public abstract class DataEventFormatter extends BaseEventFormatter {

	/**
	 * @param videoData
	 */
	public DataEventFormatter(String bufferId, String eventId) {
		super(bufferId, eventId);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEventFormatter#readEvent(v9t9.common.demo.IDemoInputEventBuffer)
	 */
	@Override
	public IDemoEvent readEvent(IDemoInputEventBuffer buffer)
			throws IOException {
		int addr = ((DemoInputEventBuffer) buffer).readVar(); 
		int chunkLength = ((DemoInputEventBuffer) buffer).readVar();
		if (chunkLength < 0) {
			// RLE repeat
			byte[] chunk = new byte[-chunkLength];
			int val = buffer.read();
			//System.err.println("RLE: " + Integer.toHexString(regOrAddr) +" @ " + Integer.toHexString(-chunkLength) +  " = " + Integer.toHexString(val));
			Arrays.fill(chunk, (byte) val);
			return createWriteDataEvent(addr, chunk);
		} else {
			// real data
			//System.err.println("Data: " + Integer.toHexString(regOrAddr) +" @ " + Integer.toHexString(chunkLength));
			byte[] data = buffer.readData(chunkLength);
			return createWriteDataEvent(addr, data);
		}
	}

	protected abstract IDemoEvent createWriteDataEvent(int addr, byte[] data);

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEventFormatter#writeEvent(v9t9.common.demo.IDemoOutputEventBuffer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void writeEvent(IDemoOutputEventBuffer buffer, IDemoEvent event)
			throws IOException {
		WriteDataBlock we = (WriteDataBlock) event;	
		
		((DemoOutputEventBuffer) buffer).pushRleMemoryWriteData(8, we.getAddress(),
				we.getData(), we.getOffset(), we.getLength());
	}

}
