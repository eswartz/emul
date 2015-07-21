/*
  PIOPrinterImageHandler.java

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
import v9t9.common.dsr.IPIOListener;
import v9t9.common.dsr.IPrinterImageEngine;
import v9t9.common.dsr.IPrinterImageHandler;
import v9t9.common.machine.IMachine;
import v9t9.machine.dsr.rs232.RS232Settings;

/**
 * This handles printer data coming from PIO
 * @author ejs
 *
 */
public class PIOPrinterImageHandler implements IPIOListener, IPrinterImageHandler {

	private IPrinterImageEngine engine;
	private IProperty activeProperty;
	
	public PIOPrinterImageHandler(IMachine machine, IPrinterImageEngine engine_) {
		this.activeProperty = machine.getSettings().get(RS232Settings.settingPIOPrint);
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
	 * @see v9t9.common.dsr.IPIOListener#charsTransmitted(byte[])
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
