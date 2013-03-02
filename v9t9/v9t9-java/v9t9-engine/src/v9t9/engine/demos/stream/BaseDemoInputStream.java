/*
  BaseDemoInputStream.java

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
package v9t9.engine.demos.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoInputBuffer;
import v9t9.common.demos.IDemoInputStream;

/**
 * @author ejs
 *
 */
public abstract class BaseDemoInputStream extends BaseReader implements IDemoInputStream {
	
	protected List<IDemoInputBuffer> buffers = new ArrayList<IDemoInputBuffer>(4);
	
	protected Queue<IDemoEvent> queuedEvents;

	public BaseDemoInputStream(InputStream is_) {
		super(is_);

		queuedEvents = new LinkedList<IDemoEvent>();
		

	}

	public void close() throws IOException {
		if (is != null) {
			is.close();
			is = null;
		}
	}

	@Override
	public IDemoEvent readNext() throws IOException {
		try {
			if (is != null && queuedEvents.isEmpty()) {
				ensureEvents();
			}
		} catch (IOException e) {
			throw e;
		} catch (Throwable e) {
			throw new IOException("Error reading demo at 0x" + 
					Long.toHexString(getPosition()), e);
		}
		
		return queuedEvents.poll();
	}

	/**
	 * Read from the input stream and queue events into queuedEvents.
	 * If stream is empty, just return.
	 * @throws IOException
	 */
	protected abstract void ensureEvents() throws IOException;
}