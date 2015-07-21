/*
  PrinterImageActor.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.printer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.widgets.Display;

import v9t9.common.demos.DemoHeader;
import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoPlaybackActor;
import v9t9.common.demos.IDemoPlayer;
import v9t9.common.demos.IDemoRecorder;
import v9t9.common.demos.IDemoRecordingActor;
import v9t9.common.dsr.IPrinterImageEngine;
import v9t9.common.dsr.IPrinterImageHandler;
import v9t9.common.dsr.IPrinterImageListener;
import v9t9.common.dsr.PrinterPage;
import v9t9.common.machine.IMachine;
import v9t9.engine.demos.events.PrinterImageEvent;

/**
 * @author ejs
 *
 */
public class PrinterImageActor implements IDemoRecordingActor, IDemoPlaybackActor {
	
	private String printerId;
	private IPrinterImageListener printerImageListener;
	private IDemoRecorder recorder;
	private Set<IPrinterImageEngine> imageEngines = new HashSet<IPrinterImageEngine>();

	/**
	 * @param printerId
	 */
	public PrinterImageActor(String printerId) {
		this.printerId = printerId;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoActor#getEventIdentifier()
	 */
	@Override
	public String getEventIdentifier() {
		return PrinterImageEvent.ID;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoActor#setup(v9t9.common.machine.IMachine)
	 */
	@Override
	public void setup(IMachine machine) {
		printerImageListener = new IPrinterImageListener() {
			
			@Override
			public void updated(PrinterPage image) {
				// ignore
			}
			
			@Override
			public void newPage(PrinterPage image) {
				try {
					if (recorder != null) {
						recorder.getOutputStream().writeEvent(PrinterImageEvent.newPage());
					}
				} catch (IOException e) {
					recorder.fail(e);
				}
			}
			
			@Override
			public void bytesProcessed(byte[] bytes) {
				try {
					if (recorder != null) {
						recorder.getOutputStream().writeEvent(PrinterImageEvent.writeData(bytes));
					}
				} catch (IOException e) {
					recorder.fail(e);
				}
			}
		};
		
		imageEngines.clear();
		for (IPrinterImageHandler handler : machine.getPrinterImageHandlers()) {
			if (handler.getPrinterId().equals(printerId)) {
				imageEngines.add(handler.getEngine());
			}
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoRecordingActor#shouldRecordFor(byte[])
	 */
	@Override
	public boolean shouldRecordFor(byte[] magic) {
		return DemoHeader.isV9t9jFormat(magic);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoRecordingActor#connectForRecording(v9t9.common.demos.IDemoRecorder)
	 */
	@Override
	public void connectForRecording(IDemoRecorder recorder) throws IOException {
		this.recorder = recorder;
		for (IPrinterImageEngine engine : imageEngines) {
			engine.addListener(printerImageListener);
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

		for (IPrinterImageEngine engine : imageEngines) {
			engine.removeListener(printerImageListener);
		}

	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoPlaybackActor#setupPlayback(v9t9.common.demos.IDemoPlayer)
	 */
	@Override
	public void setupPlayback(IDemoPlayer player) {
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoPlaybackActor#executeEvent(v9t9.common.demos.IDemoPlayer, v9t9.common.demos.IDemoEvent)
	 */
	@Override
	public void executeEvent(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		final PrinterImageEvent ev = (PrinterImageEvent) event;

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				if (ev.getType() == PrinterImageEvent.NEW_PAGE) {
					for (IPrinterImageEngine engine : imageEngines) {
						engine.newPage();
					}
				} else if (ev.getType() == PrinterImageEvent.DATA) {
					for (byte b : ev.getData()) {
						for (IPrinterImageEngine engine : imageEngines) {
							engine.print(b);
						}
					}
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
