/**
 * 
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