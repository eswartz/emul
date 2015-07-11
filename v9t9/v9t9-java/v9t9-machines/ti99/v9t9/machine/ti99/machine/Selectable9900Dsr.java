/*
  Selectable9900Dsr.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.machine;

import java.io.IOException;

import v9t9.common.dsr.IMemoryTransfer;
import v9t9.common.dsr.ISelectableDsrHandler;
import v9t9.common.dsr.SelectableDsrHandler;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntryFactory;
import v9t9.machine.ti99.dsr.IDsrHandler9900;
import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public class Selectable9900Dsr extends SelectableDsrHandler implements ISelectableDsrHandler, IDsrHandler9900 {

	public Selectable9900Dsr(TI99Machine machine, IProperty selectorProperty,
			Object... valueAndDsrPairs) {
		super(machine, selectorProperty, valueAndDsrPairs);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.SelectableDsrHandler#getCurrentDsr()
	 */
	@Override
	public IDsrHandler9900 getCurrentDsr() {
		return (IDsrHandler9900) super.getCurrentDsr();
	}
	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.dsr.IDsrHandler9900#getCruBase()
	 */
	@Override
	public short getCruBase() {
		return getCurrentDsr().getCruBase();
	}

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.dsr.IDsrHandler9900#handleDSR(v9t9.common.dsr.IMemoryTransfer, short)
	 */
	@Override
	public boolean handleDSR(IMemoryTransfer xfer, short code) {
		return getCurrentDsr().handleDSR(xfer, code);
	}

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.dsr.IDsrHandler9900#activate(v9t9.common.memory.IMemoryDomain, v9t9.common.memory.IMemoryEntryFactory)
	 */
	@Override
	public void activate(IMemoryDomain console,
			IMemoryEntryFactory memoryEntryFactory) throws IOException {
		getCurrentDsr().activate(console, memoryEntryFactory);
	}

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.dsr.IDsrHandler9900#deactivate(v9t9.common.memory.IMemoryDomain)
	 */
	@Override
	public void deactivate(IMemoryDomain console) {
		getCurrentDsr().deactivate(console);
	}

}
