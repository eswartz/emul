/*
  ICassetteChip.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cassette;

import ejs.base.properties.IPersistable;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.dsr.DeviceEditorIdConstants;
import v9t9.common.dsr.IDeviceSettings;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.settings.SettingSchema;

/**
 * @author ejs
 *
 */
public interface ICassetteChip extends IPersistable, IRegisterAccess {
	public static SettingSchema settingCassetteEnabled = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CassetteEnabled",
			"Enable cassette emulation",
			"Tell whether accesses to the CS1 or CS2 devices tries to read/write WAV files",
			Boolean.FALSE);

	public static SettingSchema settingCassetteDebug = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CassetteDebug", 
			"Debug cassette support",
			"Enable to log info to stdout about cassette reading",
			false);

	public static SettingSchema settingCassette1File = new SettingSchema(
			ISettingsHandler.MACHINE,
			"Cassette1File", 
			"Cassette #1",
			"Path to a *.wav file that provides or records CS1 data.",
			DeviceEditorIdConstants.ID_CASSETTE_FILE,
			null, String.class);
	
	public static SettingSchema settingCassette2File = new SettingSchema(
			ISettingsHandler.MACHINE,
			"Cassette2File", 
			"Cassette #2",
			"Path to a *.wav file that provides or records CS2 data.",
			DeviceEditorIdConstants.ID_CASSETTE_FILE,
			null, String.class);

	public static SettingSchema settingCassetteCompressSilence = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CassetteCompressSilence", 
			"Compress leading silence in cassette output",
			"If checked, limit initial recorded silence (~14 seconds from 99/4A delay) to save space",
			false);

//	public static SettingSchema settingCassette1Writing = new SettingSchema(
//			ISettingsHandler.TRANSIENT,
//			"Cassette1Writing", false);
//	
//	public static SettingSchema settingCassette2Writing = new SettingSchema(
//			ISettingsHandler.TRANSIENT,
//			"Cassette2Writing", false);
//	
//
//	public static SettingSchema settingCassetteReading = new SettingSchema(
//			ISettingsHandler.TRANSIENT,
//			"CassetteReading", false);
	
	public static String GROUP_CASSETTE_FILES = "Files";
	public static String GROUP_CASSETTE_OPTIONS = "Options";

	ICassetteDeck getCassette1();
	ICassetteDeck getCassette2();
	void reset();
	
	/** Get configurable settings */
	IDeviceSettings getDeviceSettings();
}
