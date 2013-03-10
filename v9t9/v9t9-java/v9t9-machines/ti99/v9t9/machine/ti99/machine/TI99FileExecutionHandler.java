/**
 * 
 */
package v9t9.machine.ti99.machine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import v9t9.common.files.Catalog;
import v9t9.common.files.CatalogEntry;
import v9t9.common.files.IFileExecutionHandler;
import v9t9.common.files.IFileExecutor;
import v9t9.common.machine.IMachine;
import v9t9.common.modules.IModule;
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

		for (IModule module : machine.getModuleManager().getModules()) {
			if (module.getName().toLowerCase().contains("extended basic")) {
				scanExtBasic(machine, drive, catalog, execs, module);
				break;
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
			if (ent.type.equals("PROGRAM") && isExtBasicProgram(machine, ent)) {
				execs.add(new ExtBasicLoadAndRunFileExecutor(module,
						catalog.deviceName + "." + ent.fileName));
			}
		}
	
	}

	int readShort(byte[] content, int offs) {
		return (((content[offs] << 8) & 0xff00) | (content[offs + 1] & 0xff));
	}
	/**
	 * @param machine 
	 * @param ent
	 * @return
	 */
	private boolean isExtBasicProgram(IMachine machine, CatalogEntry ent) {
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

}
