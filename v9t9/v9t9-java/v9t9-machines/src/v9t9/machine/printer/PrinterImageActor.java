/**
 * 
 */
package v9t9.machine.printer;

import java.io.IOException;

import org.eclipse.swt.widgets.Display;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoPlaybackActor;
import v9t9.common.demos.IDemoPlayer;
import v9t9.common.demos.IDemoRecorder;
import v9t9.common.demos.IDemoRecordingActor;
import v9t9.common.dsr.IPrinterImageHandler;
import v9t9.common.dsr.IPrinterImageListener;
import v9t9.common.machine.IMachine;
import v9t9.engine.demos.events.PrinterImageWriteDataEvent;
import v9t9.engine.demos.format.DemoFormat;

/**
 * @author ejs
 *
 */
public class PrinterImageActor implements IDemoRecordingActor, IDemoPlaybackActor {

	private int printerId;
	private IPrinterImageListener printerImageListener;
	private IDemoRecorder recorder;
	private IPrinterImageHandler printerImageHandler;

	/**
	 * @param printerId
	 */
	public PrinterImageActor(int printerId) {
		this.printerId = printerId;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoActor#getEventIdentifier()
	 */
	@Override
	public String getEventIdentifier() {
		return PrinterImageWriteDataEvent.ID;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoActor#setup(v9t9.common.machine.IMachine)
	 */
	@Override
	public void setup(IMachine machine) {
		for (IPrinterImageHandler handler : machine.getPrinterImageHandlers()) {
			if (handler.getPrinterId() == printerId) {
				this.printerImageHandler = handler;
				printerImageListener = new IPrinterImageListener() {
					
					@Override
					public void updated(Object image) {
						// ignore
					}
					
					@Override
					public void newPage(Object image) {
						// ignore
					}
					
					@Override
					public void bytesProcessed(byte[] bytes) {
						try {
							writePrinterData(bytes);
						} catch (IOException e) {
							recorder.fail(e);
						}
					}
				};
				handler.getEngine().addListener(printerImageListener);
				break;
			}
		}

	}

	/**
	 * @param bytes
	 * @throws IOException 
	 */
	protected void writePrinterData(byte[] bytes) throws IOException {
		if (recorder != null) {
			recorder.getOutputStream().writeEvent(
					new PrinterImageWriteDataEvent(
							printerId, bytes));
		}		
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoRecordingActor#shouldRecordFor(byte[])
	 */
	@Override
	public boolean shouldRecordFor(byte[] header) {
		return DemoFormat.DEMO_MAGIC_HEADER_V9t9.equals(header);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoRecordingActor#connectForRecording(v9t9.common.demos.IDemoRecorder)
	 */
	@Override
	public void connectForRecording(IDemoRecorder recorder) throws IOException {
		if (this.printerImageHandler != null) { 
			this.recorder = recorder;
			this.printerImageHandler.getEngine().addListener(printerImageListener);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoRecordingActor#flushRecording(v9t9.common.demos.IDemoRecorder)
	 */
	@Override
	public void flushRecording(IDemoRecorder recorder) throws IOException {
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoRecordingActor#disconnectFromRecording(v9t9.common.demos.IDemoRecorder)
	 */
	@Override
	public void disconnectFromRecording(IDemoRecorder recorder) {
		this.recorder = null;
		if (printerImageHandler != null) { 
			printerImageHandler.getEngine().removeListener(printerImageListener);
		}

	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoPlaybackActor#setupPlayback(v9t9.common.demos.IDemoPlayer)
	 */
	@Override
	public void setupPlayback(IDemoPlayer player) {
		if (printerImageHandler != null)  {
			printerImageHandler.getEngine().flushPage();
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoPlaybackActor#executeEvent(v9t9.common.demos.IDemoPlayer, v9t9.common.demos.IDemoEvent)
	 */
	@Override
	public void executeEvent(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		final PrinterImageWriteDataEvent ev = (PrinterImageWriteDataEvent) event;
		if (ev.getPrinterId() != printerId)
			return;
		if (printerImageHandler == null) 
			return;
			
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				for (byte b : ev.getData()) {
					printerImageHandler.getEngine().print((char) b);
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoPlaybackActor#cleanupPlayback(v9t9.common.demos.IDemoPlayer)
	 */
	@Override
	public void cleanupPlayback(IDemoPlayer player) {
		
	}

}
