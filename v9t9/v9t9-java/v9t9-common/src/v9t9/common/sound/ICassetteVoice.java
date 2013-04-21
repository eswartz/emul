/*
  ICassetteVoice.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.sound;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.SettingSchema;

/**
 * @author ejs
 *
 */
public interface ICassetteVoice {
	public static SettingSchema settingCassette1Writing = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"Cassette1Writing", false);
	
	public static SettingSchema settingCassette2Writing = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"Cassette2Writing", false);
	

	public static SettingSchema settingCassetteReading = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"CassetteReading", false);
	
	public static SettingSchema settingCassetteInput = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CassetteInput", "");
	
	public static SettingSchema settingCassette1OutputFile = new SettingSchema(
			ISettingsHandler.MACHINE,
			"Cassette1Output", String.class, null);
	
	public static SettingSchema settingCassette2OutputFile = new SettingSchema(
			ISettingsHandler.MACHINE,
			"Cassette2Output", String.class, null);
	
	public static SettingSchema settingDumpCassetteAccess = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CassetteAccess", true);
	
	public static SettingSchema settingCassetteDebug = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CassetteDebug", false);
	void setState(boolean state);
	boolean getState();
	
	void setMotor1(boolean motor);
	void setMotor2(boolean motor);
	/**
	 * @param secs
	 */
	void setClock(float secs);
}
