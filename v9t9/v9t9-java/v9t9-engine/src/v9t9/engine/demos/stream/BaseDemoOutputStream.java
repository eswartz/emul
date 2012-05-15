/**
 * 
 */
package v9t9.engine.demos.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoOutputStream;
import v9t9.engine.demos.events.SoundWriteDataEvent;
import v9t9.engine.demos.events.SoundWriteRegisterEvent;
import v9t9.engine.demos.events.SpeechEvent;
import v9t9.engine.demos.events.TimerTick;
import v9t9.engine.demos.events.VideoWriteDataEvent;
import v9t9.engine.demos.events.VideoWriteRegisterEvent;
import ejs.base.utils.CountingOutputStream;

/**
 * @author ejs
 *
 */
public abstract class BaseDemoOutputStream implements IDemoOutputStream {

	protected CountingOutputStream os;
	
	protected abstract void writeTimerTick() throws IOException;

	protected abstract void writeVideoRegisterEvent(IDemoEvent event)
			throws IOException;

	protected abstract void writeVideoDataEvent(IDemoEvent event)
			throws IOException;

	protected abstract void writeSoundDataEvent(IDemoEvent event)
			throws IOException;

	protected abstract void writeSoundRegisterEvent(IDemoEvent event)
			throws IOException;

	protected abstract void writeSpeechEvent(IDemoEvent event) throws IOException;

	protected List<IDemoOutputBuffer> buffers = new ArrayList<IDemoOutputBuffer>(4);

	/**
	 * @param os 
	 * 
	 */
	public BaseDemoOutputStream(OutputStream os_) {
		this.os = os_ instanceof CountingOutputStream ? (CountingOutputStream) os_ : new CountingOutputStream(os_);
		
	}

	public synchronized void close() throws IOException {
		flushAll();

		preClose();
		
		if (os != null) {
			os.close();
			os = null;
		}
		
		buffers.clear();
	}

	/**
	 * Called immediately before closing.
	 * @throws IOException
	 */
	protected void preClose() throws IOException {
		
	}


	protected boolean anythingToFlush() {
		boolean any = false;
		for (IDemoOutputBuffer buffer : buffers) {
			any |= !buffer.isEmpty();
		}
		return any;
	}
	
	protected void flushAll() throws IOException {
		for (IDemoOutputBuffer buffer : buffers) {
			buffer.flush();
		}
	}

	@Override
	public synchronized void writeEvent(IDemoEvent event) throws IOException {
		if (event instanceof TimerTick) {
			writeTimerTick();
		}
		else if (event instanceof VideoWriteRegisterEvent) {
			writeVideoRegisterEvent(event);
		}
		else if (event instanceof VideoWriteDataEvent) {
			writeVideoDataEvent(event);
		}
		else if (event instanceof SoundWriteDataEvent) {
			writeSoundDataEvent(event);
		}
		else if (event instanceof SoundWriteRegisterEvent) {
			writeSoundRegisterEvent(event);
		}
		else if (event instanceof SpeechEvent) {
			writeSpeechEvent(event);
		}
		else {
			throw new IOException("unknown event type: " + event);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoOutputStream#getPosition()
	 */
	@Override
	public long getPosition() {
		return os.getPosition();
	}
}