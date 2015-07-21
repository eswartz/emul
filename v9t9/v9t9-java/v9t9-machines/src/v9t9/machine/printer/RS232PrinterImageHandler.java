/*
  RS232PrinterImageHandler.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.printer;

import org.eclipse.swt.widgets.Display;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import v9t9.common.dsr.IPrinterImageEngine;
import v9t9.common.dsr.IPrinterImageHandler;
import v9t9.common.dsr.IRS232Handler.DataSize;
import v9t9.common.dsr.IRS232Handler.Parity;
import v9t9.common.dsr.IRS232Handler.Stop;
import v9t9.common.dsr.IRS232Listener;
import v9t9.common.machine.IMachine;
import v9t9.machine.dsr.rs232.RS232Settings;

/**
 * This handles 
 * @author ejs
 *
 */
public class RS232PrinterImageHandler implements IRS232Listener, IPrinterImageHandler {

	private IPrinterImageEngine engine;
	private IProperty activeProperty;
	
	/**
	 * @param machine
	 * @param i
	 */
	public RS232PrinterImageHandler(IMachine machine, IPrinterImageEngine engine_) {
		this.activeProperty = machine.getSettings().get(RS232Settings.settingRS232Print);
		this.engine = engine_;
		
		activeProperty.addListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				engine.flushPage();
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IPrinterImageHandler#getPrinterId()
	 */
	@Override
	public String getPrinterId() {
		return engine.getPrinterId();
	}
	
	@Override
	public IPrinterImageEngine getEngine() {
		return engine;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler.IRS232Listener#receiveRateSet(int)
	 */
	@Override
	public void receiveRateSet(int recvrate) {
		// ignore
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler.IRS232Listener#transmitRateSet(int)
	 */
	@Override
	public void transmitRateSet(int xmitrate) {
		// ignore
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler.IRS232Listener#updatedControl(v9t9.common.dsr.IRS232Handler.DataSize, v9t9.common.dsr.IRS232Handler.Parity, v9t9.common.dsr.IRS232Handler.Stop)
	 */
	@Override
	public void updatedControl(DataSize size, Parity parity, Stop stop) {
		if (activeProperty.getBoolean())
			engine.flushPage();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler.IRS232Listener#charsTransmitted(byte[])
	 */
	@Override
	public void charsTransmitted(final byte[] buffer) {
		if (!activeProperty.getBoolean())
			return;
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				for (byte b : buffer) {
					engine.print(b);
				}
			}
		});
	}

}
