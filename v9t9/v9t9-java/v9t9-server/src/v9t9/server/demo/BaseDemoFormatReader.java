/**
 * 
 */
package v9t9.server.demo;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoInputStream;
import v9t9.common.events.NotifyException;
import v9t9.server.demo.DemoFormat.BufferType;

/**
 * @author ejs
 *
 */
public abstract class BaseDemoFormatReader extends BaseReader implements IDemoInputStream {

	protected DemoReadBuffer videoBuffer;

	protected abstract void queueTimerTickEvent() throws IOException,
			NotifyException;

	protected abstract void queueSoundRegEvents() throws IOException,
			NotifyException;
	
	protected abstract void queueSpeechEvents() throws IOException,
			NotifyException;

	protected abstract void queueVideoEvents() throws IOException,
			NotifyException;

	protected abstract void queueSoundEvents() throws IOException,
			NotifyException;

	protected DemoReadBuffer soundBuffer;
	protected DemoReadBuffer soundRegsBuffer;
	protected DemoReadBuffer speechBuffer;
	protected Queue<IDemoEvent> queuedEvents;

	/**
	 * @param is
	 */
	public BaseDemoFormatReader(InputStream is) {
		super(is);
		
		// skipped header
		isPos += DemoFormat.DEMO_MAGIC_HEADER_LENGTH;

		queuedEvents = new LinkedList<IDemoEvent>();
		
		videoBuffer = new DemoReadBuffer(this, "video", BufferType.VIDEO,
				DemoFormat.VIDEO_BUFFER_SIZE);
		soundBuffer = new DemoReadBuffer(this, "sound", BufferType.SOUND,
						DemoFormat.SOUND_BUFFER_SIZE);
		soundRegsBuffer = new DemoReadBuffer(this, "sound", BufferType.SOUND,
					DemoFormat.SOUND_REGS_BUFFER_SIZE);
		speechBuffer = new DemoReadBuffer(this, "speech", BufferType.SPEECH,
				DemoFormat.SPEECH_BUFFER_SIZE);

	}

	public void close() throws IOException {
		if (is != null)
			is.close();
	}

	@Override
	public IDemoEvent readNext() throws NotifyException {
		try {
			ensureEvents();
		} catch (NotifyException e) {
			throw e;
		} catch (Throwable e) {
			throw new NotifyException(null, "Error reading demo at " + Integer.toHexString(isPos), e);
		}
		
		return queuedEvents.poll();
	}

	/**
	 * @throws IOException 
	 * 
	 */
	private void ensureEvents() throws IOException, NotifyException {
		if (!queuedEvents.isEmpty())
			return;
		
		int kind = is.read();  isPos++;
		if (kind < 0)
			return;
		
		if (kind == DemoFormat.BufferType.TICK.getCode()) {
			queueTimerTickEvent();
		}
		else if (kind == DemoFormat.BufferType.VIDEO.getCode()) {
			queueVideoEvents();
		}
		else if (kind == DemoFormat.BufferType.SOUND.getCode()) {
			queueSoundEvents();
		}
		else if (kind == DemoFormat.BufferType.SOUND_REGS.getCode()) {
			queueSoundRegEvents();
		}
		else if (kind == DemoFormat.BufferType.SPEECH.getCode()) {
			queueSpeechEvents();
		}
		else {
			// urf
			throw new NotifyException(null, "demo corrupted at " + Integer.toHexString(isPos) + "; byte " + Integer.toHexString(kind));
		}
		
	}

}