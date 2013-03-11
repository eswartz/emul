/**
 * 
 */
package v9t9.machine.ti99.machine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import v9t9.common.files.Catalog;
import v9t9.common.files.CatalogEntry;
import v9t9.common.files.IFileExecutionHandler;
import v9t9.common.files.IFileExecutor;
import v9t9.common.machine.IMachine;
import v9t9.common.modules.IModule;
import v9t9.machine.ti99.machine.fileExecutors.AdventureLoadFileExecutor;
import v9t9.machine.ti99.machine.fileExecutors.EditAssmLoadAndRunProgramFileExecutor;
import v9t9.machine.ti99.machine.fileExecutors.ExtBasicAutoLoadFileExecutor;
import v9t9.machine.ti99.machine.fileExecutors.ExtBasicLoadAndRunFileExecutor;

/**
 * This analyzes standard TI-99/4A file types
 * @author ejs
 *
 */
public class TI99FileExecutionHandler implements IFileExecutionHandler {

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
				scanExtBasic(machine, drive, catalog, execs, module);
				sawExtBasic = true;
			}
			else if (module.getName().toLowerCase().contains("editor/assembler")) {
				if (sawEditAssm)
					continue;
				scanEditAssm(machine, drive, catalog, execs, module);
				sawEditAssm = true;
			}
			else if (module.getName().toLowerCase().contains("scott adam's adventure")) {
				if (sawAdventure)
					continue;
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
				execs.add(new ExtBasicAutoLoadFileExecutor(module));
				
				// can't do anything else (yet)
				return;
			}
		}
		
		// else look for programs
		for (CatalogEntry ent : catalog.entries) {
			if (ent.type.equals("PROGRAM") && isExtBasicMemoryImageProgram(machine, ent)) {
				execs.add(new ExtBasicLoadAndRunFileExecutor(module,
						catalog.deviceName + "." + ent.fileName));
			}
			else if (ent.type.equals("DIS/VAR") && ent.recordLength == 163) {
				execs.add(new ExtBasicLoadAndRunFileExecutor(module,
						catalog.deviceName + "." + ent.fileName));
			}
		}
	
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
		if (pgm2 < hi && hi < machine.getVdp().getMemorySize() && (hi - Math.min(pgm1, pgm2) < size)) {
			return true;
		}
		return false;
	}

	private void scanEditAssm(IMachine machine, int drive, Catalog catalog,
			List<IFileExecutor> execs, IModule module) {
		
		gatherMemoryImagePrograms(machine, catalog, execs, module);

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
					continue;
				}
				CatalogEntry old = memImgMap.put(baseName, ent);
				// keep the lowest file
				if (old != null && old.fileName.compareTo(ent.fileName) < 0) {
					memImgMap.put(baseName, old);
				}
			}
		}

		for (CatalogEntry ent : memImgMap.values()) {
			execs.add(new EditAssmLoadAndRunProgramFileExecutor(module,
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
		if (((low & 0xff) == 0 || (low & 0xff) == 0xff
				|| (low & 0xff00) == 0xff00 || (low & 0xff00) == 0)
				&& binsize <= size
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
