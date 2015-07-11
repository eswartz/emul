/*
  PrinterImageActorProvider.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.printer;

import v9t9.common.demos.IDemoActorProvider;
import v9t9.common.demos.IDemoPlaybackActor;
import v9t9.common.demos.IDemoRecordingActor;
import v9t9.common.demos.IDemoReversePlaybackActor;
import v9t9.engine.demos.events.PrinterImageEvent;

/**
 * @author ejs
 *
 */
public class PrinterImageActorProvider implements IDemoActorProvider {

	private String printerId;

	/**
	 * @param printerId
	 */
	public PrinterImageActorProvider(String printerId) {
		this.printerId = printerId;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoActorProvider#createForPlayback()
	 */
	@Override
	public IDemoPlaybackActor createForPlayback() {
		return new PrinterImageActor(printerId);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoActorProvider#createForRecording()
	 */
	@Override
	public IDemoRecordingActor createForRecording() {
		return new PrinterImageActor(printerId);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoActorProvider#createForReversePlayback()
	 */
	@Override
	public IDemoReversePlaybackActor createForReversePlayback() {
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoActorProvider#getEventIdentifier()
	 */
	@Override
	public String getEventIdentifier() {
		return PrinterImageEvent.ID;
	}

}
