/*
  ICpuState.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cpu;

import v9t9.common.machine.IRegisterAccess;
import v9t9.common.memory.IMemoryDomain;

/**
 * @author Ed
 *
 */
public interface ICpuState extends IRegisterAccess {

	short getPC();

	void setPC(short pc);

	short getST();

	void setST(short st);

	IMemoryDomain getConsole();

	/** Create a new status object */
	IStatus createStatus();

	/** Get the live status object */
	IStatus getStatus();
	/** Set the live status object */
	void setStatus(IStatus status);

	CycleCounts getCycleCounts();

}