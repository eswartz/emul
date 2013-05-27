/*
  ManualTestTI99RomsDetection.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.common.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.DataFiles;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.machine.ti99.dsr.emudisk.EmuDiskDsr;
import v9t9.machine.ti99.dsr.realdisk.RealDiskImageDsr;
import v9t9.machine.ti99.machine.StandardTI994AMachineModel;

/**
 * @author ejs
 *
 */
public class ManualTestTI99RomsDetection {

	private ISettingsHandler settings;
	private IMachine machine;
	private IPathFileLocator locator;

	@Before
	public void setup() throws Exception {
		settings = new BasicSettingsHandler();  
		machine = new StandardTI994AMachineModel().createMachine(settings);
		locator = machine.getRomPathFileLocator();
		
		 //"/usr/local/src/v9t9-data/modules"
	}

	@Test
	public void testDiskDsrResolve() throws Exception {
		// either should work (last 16 bytes differ)
		verifyLookup(new String[] { "/usr/local/src/v9t9-data/roms" },
				RealDiskImageDsr.dsrRomInfo,
				"disk.bin", "diskgarbage.bin");
	
		// different MD5 -- use it
		settings.get(RealDiskImageDsr.settingDsrRomFileName).setString("mydisk.bin");
		verifyLookup(new String[] { "/usr/local/src/v9t9-data/roms" },
				RealDiskImageDsr.dsrRomInfo,
				"mydisk.bin");
	}

	@Test
	public void testEmuDiskDsrResolve() throws Exception {
		// MD5 doesn't matter since we ship it
		verifyLookup(new String[] { 
				"jar:file:/tmp/.v9t9j/exec/v9t9-data.jar!/ti99/dsrs/",
				},
				EmuDiskDsr.dsrRomInfo,
				"emudisk.bin");
		
		verifyLookup(new String[] { 
				"/media/M/fun/ti994a/v60ugly/roms/",
				},
				EmuDiskDsr.dsrRomInfo,
				"emudisk.bin");
	}
	/**
	 * @param string
	 * @param dsrRomInfo
	 * @param string2
	 */
	private void verifyLookup(String[] paths, MemoryEntryInfo info,
			String... allowed) {

		
		// add the path...
		locator.resetPathProperties();
		locator.addReadOnlyPathProperty(settings.get(DataFiles.settingBootRomsPath));
		
		for (String path : paths)
			DataFiles.addSearchPath(settings, path);
		
		try {

			URI uri = locator.findFile(settings, info);
			assertNotNull(info.toString(), uri);
			
			for (String all : allowed) {
				if (uri.toString().endsWith("/"+all)) {
					System.out.println("found: " + uri);
					return;
				}
			}
			fail(info + ": did not find expected entry, but " + uri);
		} finally {
			for (String path : paths)
				DataFiles.removeSearchPath(settings, path);
		}
	}


}
