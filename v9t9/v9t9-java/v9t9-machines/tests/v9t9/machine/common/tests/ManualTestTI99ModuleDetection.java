/*
  ManualTestTI99ModuleDetection.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.common.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.modules.IModule;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.machine.ti99.machine.StandardTI994AMachineModel;

/**
 * @author ejs
 *
 */
public class ManualTestTI99ModuleDetection {

	private ISettingsHandler settings;
	private IMachine machine;
	private IPathFileLocator locator;

	@Before
	public void setup() throws Exception {
		settings = new BasicSettingsHandler();  
		machine = new StandardTI994AMachineModel().createMachine(settings);
		locator = machine.getRomPathFileLocator();
	}
	
	/** Make sure stock modules don't overlap */
	@Test
	public void testStockModules() throws Exception {
		IModule[] stocks = machine.getModuleManager().getStockModules();
		Map<String, IModule> md5Map = new HashMap<String, IModule>();
		for (IModule stock : stocks) {
			String md5 = stock.getMD5();
			IModule old = md5Map.put(md5, stock);
			if (old != null) {
				fail(stock.getMD5() + " -> " + stock.getName() + " / " + old.getName());
			}
		}
	}
	@Test
	public void testMyModules() throws Exception {
		testDirectory("/usr/local/src/v9t9-data/modules", 
				"(?i).*\\.bin",
				"c.bin", "g.bin", 
				"forthc.bin", "nforthc.bin", "0forth.bin",
				"TI-EXTBC.BIN","TI-EXTBD.BIN", "cp01.bin"
				);
	}

	@Test
	public void testMessModules() throws Exception {
		testDirectory("/usr/local/src/v9t9-data/modules/mess",
				"(?i).*\\.bin", "forthc.bin" 
				);
	}
	@Test
	public void testToSecModules() throws Exception {
		testDirectory("/usr/local/src/v9t9-data/modules/tosec",
				"(?i).*\\.bin", "Forthc (19xx)(-)(Unknown).bin",
				"Supercart (19xx)(Texas Instruments).bin",		// nothing
				"Sneggit (1982)(Texas Instruments)(File 1 of 2)(Sneggitc).bin"	// no #2
				);
	}
	@Test
	public void testToWhtechZipModules() throws Exception {
		testDirectory("/usr/local/src/v9t9-data/modules/ftp.whtech.com/emulators/cartridges/zip",
				"(?i).*\\.zip");
	}
	@Test
	public void testToWhtechRpkModules() throws Exception {
		testDirectory("/usr/local/src/v9t9-data/modules/ftp.whtech.com/emulators/cartridges/rpk",
				"(?i).*\\.rpk");
	}
	@Test
	public void testToWhtechRpkConvertedModules() throws Exception {
		testDirectory("/usr/local/src/v9t9-data/modules/ftp.whtech.com/emulators/cartridges/rpk/converted",
				"(?i).*\\.rpk");
	}
	
	protected List<IModule> testDirectory(String path, final String pattern, final String... ignore) {
		File dir = new File(path);
		assertTrue(dir.exists());
		
		Set<File> allFiles = getAllFiles(pattern, dir, ignore); 
		
		URI databaseURI = URI.create("test.xml");
		Collection<IModule> modules = machine.createModuleDetector(databaseURI).scan(dir);
		
		List<IModule> matches = new ArrayList<IModule>();
		System.out.println("Found " + modules.size() + " modules in " + dir);
		for (IModule module : modules) {
			
			Collection<File> files = module.getUsedFiles(locator);
			assertFalse(files.isEmpty());
			if (allFiles.removeAll(files)) {
				System.out.println(module);
				for (File file : files) {
					System.out.println("\t" + file);
				}
				matches.add(module);
			}
			
			verifySpecificModule(module);
		}
		
		StringBuilder sb = new StringBuilder();
		for (File file : allFiles) {
			sb.append("*** did not detect ").append(file).append('\n');
		}

		if (sb.length() > 0) {
			System.err.print(sb);
			fail(sb.toString());
		}
		
		return matches;
	}

	/**
	 * @param pattern
	 * @param dir
	 * @param ignore
	 * @return
	 */
	protected Set<File> getAllFiles(final String pattern, File dir,
			final String... ignore) {
		Set<File> allFiles = new LinkedHashSet<File>(Arrays.asList(dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory() || !pathname.getName().matches(pattern))
					return false;
				for (String ign : ignore) {
					if (pathname.getName().equals(ign))
						return false;
					if (pathname.getName().indexOf('\uFFFD') >= 0)
						return false;
				}
				return true;
			}
		})));
		return allFiles;
	}

	/**
	 * @param module
	 */
	private void verifySpecificModule(IModule module) {
		if (verifyMiniMemory(module))
			return;
		if (verifyEASuperCart(module))
			return;
		if (verifyParsec(module))
			return;
	}

	/**
	 * @param module
	 * @return
	 */
	private boolean verifyEASuperCart(IModule module) {
		if (module.getName().equalsIgnoreCase("EA/8K Super Cart")) {
			boolean foundGraphics = false; 
			// make sure it has the nonvolatile RAM
			MemoryEntryInfo[] infos = module.getMemoryEntryInfos();
			for (MemoryEntryInfo info : infos) {
				if (info.getDomainName().equals(IMemoryDomain.NAME_GRAPHICS)
						&& info.getAddress() == 0x6000) {
					foundGraphics = true;
				}
			}
			assertTrue(foundGraphics);
			assertEquals(2, infos.length);
			return true;
		}
		return false;
	}
	/**
	 * @param module
	 * @return
	 */
	private boolean verifyMiniMemory(IModule module) {
		if (module.getName().equalsIgnoreCase("Easy Bug")) {
			fail("should be named Mini Memory");
		}
		else if (module.getName().equalsIgnoreCase("Mini Memory")) {
			// make sure it has the nonvolatile RAM
			MemoryEntryInfo[] infos = module.getMemoryEntryInfos();
			for (MemoryEntryInfo info : infos) {
				if (info.getDomainName().equals(IMemoryDomain.NAME_CPU)
						&& info.getAddress() == 0x6000) {
					assertEquals(0x1000, info.getFileMd5Limit());
					assertEquals("9BCF230E42BB280199A04F0E0C4797C1", info.getFileMD5());
				}
			}
			assertEquals(3, infos.length);
			return true;
		}
		return false;
	}

	/**
	 * @param module
	 * @return
	 */
	private boolean verifyParsec(IModule module) {
		if (module.getName().equalsIgnoreCase("Parsec")) {
			// make sure it has banked ROM and GROM
			MemoryEntryInfo[] infos = module.getMemoryEntryInfos();
			int cpus = 0;
			int gpls = 0;
			for (MemoryEntryInfo info : infos) {
				if (info.getDomainName().equals(IMemoryDomain.NAME_CPU)
						&& info.getAddress() == 0x6000) {
					cpus++;
				}
				else if (info.getDomainName().equals(IMemoryDomain.NAME_GRAPHICS)
						&& info.getAddress() == 0x6000) {
					gpls++;
				}
			}
			assertEquals(1, gpls);
			assertEquals(1, cpus);
			assertEquals(2, infos.length);
			return true;
		}
		return false;
	}

	@Test
	public void testNonModules() throws Exception {
		testNoneDirectory("/usr/local/src/v9t9-data/roms", "(?i).*\\.bin", "ed_basicL.bin");		
		testNoneDirectory("/usr/local/src/v9t9-data/roms/pcode", "(?i).*\\.bin");		
		testNoneDirectory("/home/ejs/devel/emul/v9t9/v9t9-java/v9t9-data/data/ti99/dsrs", "(?i).*\\.bin");		
	}
	
	protected void testNoneDirectory(String path, final String pattern, final String... ignore) {
		File dir = new File(path);
		assertTrue(dir.exists());
		
		URI databaseURI = URI.create("test.xml");
		Collection<IModule> modules = machine.createModuleDetector(databaseURI).scan(dir);
		
		System.out.println("Found " + modules.size() + " modules in " + dir);
		
		StringBuilder sb = new StringBuilder();
		for (IModule module : modules) {

			boolean bad = false;
			Collection<File> files = module.getUsedFiles(locator);
			for (File file : files) {
				if (!bad && !Arrays.asList(ignore).contains(file.getName())) {
					bad = true;
					System.out.println(module);
					sb.append("*** found ").append(module).append('\n');
				}
				if (bad)
					System.out.println("\t" + file);
			}
		}

		if (sb.length() > 0) {
			System.err.print(sb);
			fail(sb.toString());
		}
	}


	@Test
	public void testMilliken() throws Exception {
		List<IModule> mods = testDirectory("/usr/local/src/v9t9-data/modules/mess",
				"(?i)(phm309.|phm310.)g\\.bin" 
				);
		assertEquals(11, mods.size());
		
		for (IModule mod : mods) {
			assertFalse(mod.toString(), mod.getName().isEmpty());
		}
		
		boolean anyEqual = false;
		for (int i = 0; i < mods.size(); i++) {
			IModule mod = mods.get(i);
			for (int j = i+1; j < mods.size(); j++) {
				IModule amod = mods.get(j);
				if (mod.equals(amod)) {
					// if equal, must have identical files & contents
					System.out.println(mod.getName() + " == " + amod.getName() + "\n");
					anyEqual = true;
				}
			}
		}
		if (anyEqual)
			System.out.println("Some modules are equal");
	}
	/**
	 * @param mods
	 * @param md5Map
	 */
	private void addModuleHashes(List<IModule> mods, 
			Map<String, IModule> md5Map, 
			Map<String, String> nameToMd5Map, 
			StringBuilder fails) {
		// these have no unique name header, so the filename is used
		final String millikens = "phm(3027|3028|3029|3043|3046|3049|3051|3093)";
		// these are the exact same product sold under two PHMs
		final String divisors = "phm3093|phm3049";
		for (IModule mod : mods) {
			String md5 = mod.getMD5();
			System.out.println(mod.getName() + " -> " + md5);
			IModule old = md5Map.put(md5, mod);
			if (old != null && !old.equals(mod) && !old.getName().equals(mod.getName())) {
				System.out.println("\tconflicts with " + old.getName());
				if (old.getName().matches(millikens) && mod.getName().matches(millikens)) {
					if (old.getName().matches(divisors) && mod.getName().matches(divisors)) {
						// okay
						continue;
					}
				}
				else if (old.getName().matches(millikens) || mod.getName().matches(millikens)) {
					// fine
					continue;
				}
				fails.append(mod.getName());
				fails.append(" conflicts with ");
				fails.append(old.getName());
				fails.append("\n");
			}
			
			String oldMd5 = nameToMd5Map.put(mod.getName(), md5);
			if (oldMd5 != null && !oldMd5.equals(md5)) {
				fails.append(mod.getName()).append(" has two MD5s: ").
					append(oldMd5).append(" and ").append(md5).append('\n');
			}
		}
	}
	

	/**
	 * Most modules can be detected fine, but some cause problems since their
	 * headers are all the same or are auto-start.  We have a hard-coded database
	 * to cover these.
	 * @throws Exception
	 */
	@Test
	public void testModuleDatabase() throws Exception {
		List<IModule> mods = testDirectory("/usr/local/src/v9t9-data/modules/mess",
				"(?i)(phm.*)\\.bin" 
				);
		assertEquals(121, mods.size());
		
		for (IModule mod : mods) {
			assertFalse(mod.toString(), mod.getName().isEmpty());
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mods.size(); i++) {
			IModule mod = mods.get(i);
			System.out.println(mod);
			if (mod.getName().equals("") || mod.getName().matches("phm.*")) {
				sb.append(mod.toString()).append('\n');
			}
		}
		if (sb.length() > 0) {
			System.err.println(sb);
			fail(sb.toString());
		}
	}
	
	/**
	 * The module hashes -- which include titles -- are meant to
	 * uniquely identify a module by content, excising anything about
	 * its filename, but retaining the number of files and their
	 * memory mappings.
	 * @throws Exception
	 */
	@Test
	public void testHashes() throws Exception {
		Map<String, IModule> md5Map = new HashMap<String, IModule>();
		Map<String, String> nameToMd5Map = new HashMap<String, String>();

		StringBuilder sb = new StringBuilder();
		List<IModule> mods;
		
		mods = testDirectory("/usr/local/src/v9t9-data/modules/mess",
				"(?i).*\\.bin",
				"forthc.bin", "nforthc.bin", "0forth.bin"
				);
		
		addModuleHashes(mods, md5Map, nameToMd5Map, sb);
		
		
		mods = testDirectory("/usr/local/src/v9t9-data/modules/tosec",
				"(?i).*\\.bin", "Forthc (19xx)(-)(Unknown).bin",
				"Supercart (19xx)(Texas Instruments).bin",		// nothing
				"Sneggit (1982)(Texas Instruments)(File 1 of 2)(Sneggitc).bin"	// no #2
				);
		
		addModuleHashes(mods, md5Map, nameToMd5Map, sb);
		
		mods = testDirectory("/usr/local/src/v9t9-data/modules",
				"(?i).*\\.bin",
				"c.bin", "g.bin", 
				"forthc.bin", "nforthc.bin", "0forth.bin",
				"TI-EXTBC.BIN","TI-EXTBD.BIN", "cp01.bin",
				"xxxxxxxg.bin"
				);

		addModuleHashes(mods, md5Map, nameToMd5Map, sb);

		// These are named according to whim and are mostly wrong
//		mods = testDirectory("/usr/local/src/v9t9-data/modules/ftp.whtech.com/emulators/cartridges/rpk",
//					"(?i).*\\.rpk");
//		
//		addModuleHashes(mods, md5Map);

		if (sb.length() > 0)
			fail(sb.toString());
	}
	
	{
		// 2FDCBBCBA6585BA68EA1FFD0C19FF30B maps to Mancala or The Great Word Race
		
		// why "TI invaders" instead of "TI Invaders" for A4BDDA62ADFC49141573A424B54F6344
	}

}
