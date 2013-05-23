/*
  TI99FileExecutionHandler.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.machine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ejs.base.utils.HexUtils;

import v9t9.common.files.Catalog;
import v9t9.common.files.CatalogEntry;
import v9t9.common.files.DsrException;
import v9t9.common.files.IFileExecutionHandler;
import v9t9.common.files.IFileExecutor;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.modules.IModule;
import v9t9.engine.files.directory.OpenFile;
import v9t9.machine.ti99.machine.fileExecutors.AdventureLoadFileExecutor;
import v9t9.machine.ti99.machine.fileExecutors.ArchiverExtractFileExecutor;
import v9t9.machine.ti99.machine.fileExecutors.EditAssmLoadAndRunFileExecutor;
import v9t9.machine.ti99.machine.fileExecutors.EditAssmRunProgramFileExecutor;
import v9t9.machine.ti99.machine.fileExecutors.ExtBasicAutoLoadFileExecutor;
import v9t9.machine.ti99.machine.fileExecutors.ExtBasicLoadAndRunFileExecutor;

/**
 * This analyzes standard TI-99/4A file types
 * @author ejs
 *
 */
public class TI99FileExecutionHandler implements IFileExecutionHandler {

	private static final Logger log = Logger.getLogger(TI99FileExecutionHandler.class);
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutionHandler#analyze(v9t9.common.files.Catalog)
	 */
	@Override
	public IFileExecutor[] analyze(IMachine machine, int drive, Catalog catalog) {
		List<IFileExecutor> execs = new ArrayList<IFileExecutor>();

		boolean sawExtBasic = false;
		boolean sawEditAssm = false;
		boolean sawAdventure = false;
		
		for (IModule module : machine.getModuleManager().getModules()) {
			if (module.getName().toLowerCase().contains("extended basic")) {
				if (sawExtBasic)
					continue;
				log.debug("Found Extended BASIC match: " + module);
				scanExtBasic(machine, drive, catalog, execs, module);
				sawExtBasic = true;
			}
			else if (module.getName().toLowerCase().contains("editor/assembler")) {
				if (sawEditAssm)
					continue;
				log.debug("Found Editor/Assembler match: " + module);
				scanEditAssm(machine, drive, catalog, execs, module);
				sawEditAssm = true;
			}
			else if (module.getName().toLowerCase().contains("scott adam's adventure")) {
				if (sawAdventure)
					continue;
				log.debug("Found Scott Adam's Adventure match: " + module);
				scanAdventure(machine, catalog, execs, module);
				sawAdventure = true;
			}
		}
		return (IFileExecutor[]) execs.toArray(new IFileExecutor[execs.size()]);
	}

	private void scanExtBasic(IMachine machine, int drive, Catalog catalog,
			List<IFileExecutor> execs, IModule module) {
		if (drive == 1) {
			CatalogEntry load = catalog.findEntry("LOAD", "PROGRAM", 0);
			if (load != null) {
				log.debug("Found DSK1.LOAD");
				execs.add(new ExtBasicAutoLoadFileExecutor(module));
				
				// if there is a LOAD, can't do anything else (yet -- need a way to force FCTN-4 to work)
				log.debug("Not scanning further -- can't yet abort auto-load to do anything else");
				return;
			}
		}
		
		// else look for programs
		for (CatalogEntry ent : catalog.entries) {
			String filePath = catalog.deviceName + "." + ent.fileName;
			if (ent.type.equals("PROGRAM") && isExtBasicMemoryImageProgram(machine, ent)) {
				execs.add(new ExtBasicLoadAndRunFileExecutor(module,
						filePath));
			}
			else if (ent.type.equals("DIS/VAR") && ent.recordLength == 163) {
				execs.add(new ExtBasicLoadAndRunFileExecutor(module,
						filePath));
			}
		}

		// and uncompressed objects
		gatherObjectFiles(machine, catalog, execs, module, false);

	}

	int readShort(byte[] content, int offs) {
		return (((content[offs] << 8) & 0xff00) | (content[offs + 1] & 0xff));
	}
	
	/**
	 * Identify Extended BASIC memory image programs.
	 * <p/>
	 * These are PROGRAM files that are loaded into VDP RAM.
	 * They have this header:
	 * <pre>
	 *  word            word       word         word
	 * [vdp addr??]  [VDP addr]   [VDP addr]   [high VDP addr]
	 * </pre>
	 * @param machine
	 * @param ent
	 * @return
	 */
	private boolean isExtBasicMemoryImageProgram(IMachine machine, CatalogEntry ent) {
		int size = ent.getFile().getFileSize();
		byte[] header = new byte[256];
		try {
			ent.getFile().readContents(header, 0, 0, header.length);
		} catch (IOException e) {
			return false;
		}
		//int low = readShort(header, 0);
		int pgm1 = readShort(header, 2);
		int pgm2 = readShort(header, 4);
		int hi = readShort(header, 6);
		
		log.debug("Ext Basic program attempt for " + ent.getFile() + ": hdr is " 
				+ HexUtils.toHex4(pgm1) + ", "
				+ HexUtils.toHex4(pgm2) + ", "
				+ HexUtils.toHex4(hi));
		if (pgm2 < hi && hi < machine.getVdp().getMemorySize() && (hi - Math.min(pgm1, pgm2) < size)) {
			log.debug("accepting");
			return true;
		}
		return false;
	}

	private void scanEditAssm(IMachine machine, int drive, Catalog catalog,
			List<IFileExecutor> execs, IModule module) {
		
		gatherMemoryImagePrograms(machine, catalog, execs, module);
		gatherArchives(machine, drive, catalog, execs, module);
		gatherObjectFiles(machine, catalog, execs, module, true);
		gatherObjectFiles(machine, catalog, execs, module, false);

	}

	/**
	 * Look for memory image programs, PROGRAM files in groups of one
	 * or more with incrementing filenames.
	 * @param machine
	 * @param catalog
	 * @param execs
	 * @param module
	 */
	protected void gatherMemoryImagePrograms(IMachine machine, Catalog catalog,
			List<IFileExecutor> execs, IModule module) {
		// gather memory image segments
		Map<String, CatalogEntry> memImgMap = new TreeMap<String, CatalogEntry>();
		
		for (CatalogEntry ent : catalog.entries) {
			if (ent.type.equals("PROGRAM") && isMemoryImageProgram(machine, ent)) {
				String baseName = ent.fileName.substring(0, ent.fileName.length() - 1);
				if (baseName.equals("ASSM") || baseName.equals("EDIT")) {
					log.debug("Skipping ASSM/EDIT");
					continue;
				}
				CatalogEntry old = memImgMap.put(baseName, ent);
				// keep the lowest file
				if (old != null) {
					if (old.fileName.compareTo(ent.fileName) < 0) {
						memImgMap.put(baseName, old);
					}
				}
			}
		}

		for (CatalogEntry ent : memImgMap.values()) {
			execs.add(new EditAssmRunProgramFileExecutor(module,
					catalog.deviceName + "." + ent.fileName));
		}
	}
	
	/**
	 * Identify memory image programs.
	 * <p/>
	 * These are PROGRAM files that are loaded directly into RAM.
	 * They have this header:
	 * <pre>
	 *  word          word       word
	 * [more flag]   [size]   [load addr]
	 * </pre>
	 * @param machine
	 * @param ent
	 * @return
	 */
	private boolean isMemoryImageProgram(IMachine machine, CatalogEntry ent) {
		int size = ent.getFile().getFileSize();
		byte[] header = new byte[256];
		try {
			ent.getFile().readContents(header, 0, 0, header.length);
		} catch (IOException e) {
			return false;
		}
		int low = readShort(header, 0);
		int binsize = readShort(header, 2);
		int addr = readShort(header, 4);
		
		log.debug("Memory Image program attempt for " + ent.getFile() + ": hdr is " 
				+ HexUtils.toHex4(low) + ", "
				+ HexUtils.toHex4(binsize) + ", "
				+ HexUtils.toHex4(addr));

		if (((low & 0xff) == 0 || (low & 0xff) == 0xff
				|| (low & 0xff00) == 0xff00 || (low & 0xff00) == 0)
				&& binsize <= size + 6
				&& addr + binsize <= 0x10000
				&& machine.getConsole().hasRamAccess(addr)
				&& machine.getConsole().hasRamAccess(addr + binsize - 1)) {
			return true;
		}
		return false;
	}

	/**
	 * Look for memory image programs, PROGRAM files in groups of one
	 * or more with incrementing filenames.
	 * @param machine
	 * @param catalog
	 * @param execs
	 * @param module
	 */
	protected void gatherArchives(IMachine machine, int drive, Catalog catalog,
			List<IFileExecutor> execs, IModule module) {
		for (CatalogEntry ent : catalog.entries) {
			if (ent.type.equals("INT/FIX") && ent.recordLength == 128) {
				
				OpenFile file;
				try {
					file = new OpenFile(ent.getFile(), catalog.deviceName, ent.fileName);
				} catch (DsrException e1) {
					continue;
				}
				
				byte[] record = new byte[128];
				ByteMemoryAccess access = new ByteMemoryAccess(record, 0);
				
				int len;
				try {
					len = file.readRecord(access, 128);
				} catch (DsrException e) {
					continue;
				}

				log.debug("Archiver3 attempt for " + ent.getFile() + ": hdr is " 
						+ HexUtils.toHex2(record[0]) + ", "
						+ HexUtils.toHex2(record[1]));

				if (len == 128 && access.memory[0] == (byte) 0x80 && 
						(access.memory[1] >= 0x10 && access.memory[1] <= 0x13)) {
					execs.add(new ArchiverExtractFileExecutor(module,
							ent.fileName, drive,
							file.getNativeFile().getFile().getParent()
							 ));
				}
			}
		}
	}
	
	/**
	 * Look for memory image programs, PROGRAM files in groups of one
	 * or more with incrementing filenames.
	 * @param machine
	 * @param catalog
	 * @param execs
	 * @param module
	 */
	protected void gatherObjectFiles(IMachine machine, Catalog catalog,
			List<IFileExecutor> execs, IModule module, boolean allowCompressed) {
		for (CatalogEntry ent : catalog.entries) {
			if (ent.type.equals("DIS/FIX") && ent.recordLength == 80) { 
				List<String> entries = new ArrayList<String>();
				if (isUncompressedObjectFile(machine, catalog, ent, entries)
						|| (allowCompressed && isCompressedObjectFile(machine, catalog, ent, entries))) {
					if (entries.isEmpty() || entries.contains("*")) {
						execs.add(new EditAssmLoadAndRunFileExecutor(module,
								catalog.deviceName + "." + ent.fileName, null));
					} else {
						for (String entry : entries) {
							execs.add(new EditAssmLoadAndRunFileExecutor(module,
									catalog.deviceName + "." + ent.fileName, entry));
						}
					}
				}
			}
		}
	}
	
	/**
	 * Identify uncompressed object files.
	 * <p/>
	 * These are DIS/FIX 80 files with tagged records in ASCII.
	 * @param machine
	 * @param ent
	 * @param entries 
	 * @return
	 */
	private boolean isUncompressedObjectFile(IMachine machine, Catalog catalog, CatalogEntry ent, List<String> entries) {
		entries.clear();
		
		OpenFile file;
		try {
			file = new OpenFile(ent.getFile(), catalog.deviceName, ent.fileName);
		} catch (DsrException e1) {
			return false;
		}
		
		byte[] record = new byte[80];
		ByteMemoryAccess access = new ByteMemoryAccess(record, 0);
		
		boolean valid = false;
		while (true) {
			try {
				int len = file.readRecord(access, 80);
				//System.out.println(new String(record).replaceAll("[\\x00-\\x1f]", " "));
				
				if (!isUncompressedObjectCodeRecord(Arrays.copyOf(record, len), entries)) {
					valid = false;
					break;
				} else {
					// at least one record
					valid = true;
				}
			} catch (DsrException e) {
				break;
			}
			
		}
		return valid;
	}

	
	/**
	 * @param copyOf
	 * @return
	 */
	private boolean isUncompressedObjectCodeRecord(byte[] rec, List<String> entries) {
		int idx = 0;
		while (idx < rec.length) {
			char tag = (char) rec[idx++];
			int left = rec.length - idx;
			switch (tag) {
			case '0':
				if (left < 4 + 8)
					return false;
				idx += 12;
				break;
			case '1':
			case '2':
				// entry
				entries.add("*");
				idx += 4;
				break;
			case '7':
			case '8':
			case '9':
			case 'A':
			case 'B':
			case 'C':
				if (left < 4)
					return false;
				idx += 4;
				break;
			case '3':
			case '4':
			case '5':
			case '6':
				if (left < 4 + 6)
					return false;
				idx += 4;
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < 6; i++) {
					char ch = (char) rec[idx++];
					if (ch <= 0x20)
						break;
					sb.append(ch);
				}
				if (tag == '5' || tag == '6') {
					entries.add(sb.toString());
				}
				break;
			case 'F':
				// end of record
				return true;
			case ':':
				// end of file
				if (idx == 1)
					return true;
				return false;
			}
		}
				
		return false;
	}

	/**
	 * Identify compressed object files.
	 * <p/>
	 * These are DIS/FIX 80 files with tagged records in binary.
	 * @param machine
	 * @param ent
	 * @param entries 
	 * @return
	 */
	private boolean isCompressedObjectFile(IMachine machine, Catalog catalog, CatalogEntry ent, List<String> entries) {
		entries.clear();
		
		OpenFile file;
		try {
			file = new OpenFile(ent.getFile(), catalog.deviceName, ent.fileName);
		} catch (DsrException e1) {
			return false;
		}
		
		byte[] record = new byte[80];
		ByteMemoryAccess access = new ByteMemoryAccess(record, 0);
		
		boolean valid = false;
		while (true) {
			try {
				int len = file.readRecord(access, 80);
				//System.out.println(new String(record).replaceAll("[\\x00-\\x1f]", " "));
				
				if (!isCompressedObjectCodeRecord(Arrays.copyOf(record, len), entries)) {
					if (record[0] != 0)
						valid = false;
					break;
				} else {
					// at least one record
					valid = true;
				}
			} catch (DsrException e) {
				break;
			}
			
		}
		return valid;
	}

	
	/**
	 */
	private boolean isCompressedObjectCodeRecord(byte[] rec, List<String> entries) {
		int idx = 0;
		while (idx < rec.length) {
			char tag = (char) rec[idx++];
			int left = rec.length - idx;
			switch (tag) {
			case '\001':
				if (left < 2 + 8)
					return false;
				idx += 10;
				break;
			case '1':
			case '2':
				// entry
				entries.add("*");
				idx += 2;
				break;
			case '7':
			case '8':
			case '9':
			case 'A':
			case 'B':
			case 'C':
				if (left < 2)
					return false;
				idx += 2;
				break;
			case '3':
			case '4':
			case '5':
			case '6':
				if (left < 2 + 6)
					return false;
				idx += 2;
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < 6; i++) {
					char ch = (char) rec[idx+i];
					if (ch <= 0x20)
						break;
					sb.append(ch);
				}
				idx += 6;
				if (tag == '5' || tag == '6') {
					entries.add(sb.toString());
				}
				break;
			case 'F':
				// end of record
				return true;
			case ':':
				// end of file
				if (idx == 1)
					return true;
				return false;
			default:
				return false;
			}
		}
				
		return false;
	}

	/**
	 * Look for Adventure files, PROGRAM files in groups of one
	 * or more with incrementing filenames.
	 * @param machine
	 * @param catalog
	 * @param execs
	 * @param module
	 */
	protected void scanAdventure(IMachine machine, Catalog catalog,
			List<IFileExecutor> execs, IModule module) {
		for (CatalogEntry ent : catalog.entries) {
			String[] gameName = { null };
			if (ent.type.equals("PROGRAM") && isAdventureProgram(machine, ent, gameName)) {
				execs.add(new AdventureLoadFileExecutor(module,
						catalog.deviceName + "." + ent.fileName,
						gameName[0]));
			}
		}
	}
	
	/**
	 * Identify Scott Adam's Adventure programs
	 * <p/>
	 * These are PROGRAM files which are loaded directly into VDP RAM
	 * at address 0.  Thus they seem to have a 960-char text header.
	 * @param machine
	 * @param ent
	 * @param gameName 
	 * @return
	 */
	private boolean isAdventureProgram(IMachine machine, CatalogEntry ent, String[] gameName) {
		int size = ent.getFile().getFileSize();
		byte[] header = new byte[960];
		try {
			ent.getFile().readContents(header, 0, 0, header.length);
		} catch (IOException e) {
			return false;
		}
		if (size > 960) {
			for (byte b : header) {
				if (!(b >= 0x20 && b < 127)) {
					return false;
				}
			}
			// look for the name
			try {
				header = new byte[size];
				ent.getFile().readContents(header, 0, 0, size);
				String text = new String(header);
				text = text.replaceAll("[\\x00-\\x1f]", " "); 
				Pattern pattern = Pattern.compile("(?msi)Welcome to[^']+'([^']+)'");
				Matcher matcher = pattern.matcher(text);
				if (matcher.find()) {
					gameName[0] = matcher.group(1);
				} else {
					pattern = Pattern.compile("(?msi)\\*\\*\\*\\s+([^*]+?)\\s*\\*\\*\\*");
					matcher = pattern.matcher(text);
					if (matcher.find()) {
						gameName[0] = matcher.group(1);
					} else {
						gameName[0] = null;
					}
				}
			} catch (IOException e) {
			}
			return true;
		}
		
		return false;
	}

}
