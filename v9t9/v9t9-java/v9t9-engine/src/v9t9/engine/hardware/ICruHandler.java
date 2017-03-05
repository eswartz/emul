/*
  ICruHandler.java

  (c) 2005-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.hardware;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.SettingSchema;
/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 24, 2004
 *
 */


/**
 * Handle the behavior of the CRU.
 * 
 * @author ejs
 */
public interface ICruHandler {
	SettingSchema settingDumpCruAccess = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"DumpCruAccess", Boolean.FALSE);

    public void writeBits(int addr, int val, int num);
    public int readBits(int addr, int num);
}
