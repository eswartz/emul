/**
 * 
 */
package v9t9.machine.ti99.machine;

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
				scanExtBasic(drive, catalog, execs, module);
				break;
			}
		}
		return (IFileExecutor[]) execs.toArray(new IFileExecutor[execs.size()]);
	}

	private void scanExtBasic(int drive, Catalog catalog,
			List<IFileExecutor> execs, IModule module) {
		if (drive == 1) {
			CatalogEntry load = catalog.findEntry("LOAD", "PROGRAM", 0);
			if (load != null) {
				execs.add(new ExtBasicAutoLoadFileExecutor(module));
				// can't really avoid otherwise
				return;
			}
		}
		
		// else look for programs
		for (CatalogEntry ent : catalog.entries) {
			if (ent.type.equals("PROGRAM")) {
				execs.add(new ExtBasicLoadAndRunFileExecutor(module,
						catalog.deviceName + "." + ent.fileName));
			}
		}
	
	}

}
