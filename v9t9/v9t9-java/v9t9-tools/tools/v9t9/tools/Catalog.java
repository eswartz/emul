package v9t9.tools;

import java.io.IOException;
import java.io.PrintStream;

import v9t9.common.files.CatalogEntry;
import v9t9.common.files.IEmulatedDisk;
import v9t9.common.files.NativeTextFile;
import v9t9.common.machine.IMachine;
import v9t9.tools.utils.BaseDiskUtil;
import v9t9.tools.utils.Category;
import v9t9.tools.utils.ToolUtils;
import ejs.base.logging.LoggingUtils;
import ejs.base.utils.Pair;
import gnu.getopt.Getopt;

/**
 * @author ejs
 *
 */
@Category(Category.DISKUTILS)
public class Catalog extends BaseDiskUtil {
	private static final String PROGNAME = Catalog.class.getSimpleName();
	private boolean showTextFiles;

	private static void help() {
        System.out.println("\n"
                        + "V9t9 Disk Catalog\n"
                        + "\n"
                        + "Shows the listing of files, types, and sizes in a disk or directory\n"
                        + "\n" 
                        + PROGNAME + " [options] {disk|dir}[:namePattern]\n"+
                        "\n"+
                        "Options:\n"+
                        "-t: show native files as DIS/VAR 80 files (e.g. to see FIAD name)\n"+
           			 "\n"+
           			 "where 'disk' is a path to a *.dsk or *.trk image and\n"+
           			 "'dir' is a disk directory or 'file' is a file, and\n"+
           			 "'namePattern' is a emulated file (or regex) in that disk or directory"
           			 );
    }

	public static void main(String[] args) {
		LoggingUtils.setupNullLogging();
		
        if (args.length == 0) {
        	help();
        	System.exit(0);
        }

        IMachine machine = ToolUtils.createMachine();
        
        Catalog catalog = new Catalog(machine, System.out);
        		
		Getopt getopt;
		getopt = new Getopt(PROGNAME, args, "?t");        

		int opt;
		while ((opt = getopt.getopt()) != -1) {
			switch (opt) {
            case '?':
                help();
                break;
            case 't': 
            	catalog.setShowTextFiles(true);
            	break;
			}
        }
		
        try {
            // leftover files are FIAD
            int idx = getopt.getOptind();
            while (idx < args.length) {
            	String name = args[idx++];
            	catalog.catalog(name);
            }
        } catch (IOException e) {
        	e.printStackTrace();
        }
	}

	/**
	 * @param showTextFiles
	 */
	private void setShowTextFiles(boolean showTextFiles) {
		this.showTextFiles = showTextFiles;
		
	}

	public Catalog(IMachine machine, PrintStream out) {
		super(machine, out);
	}

	private void catalog(String arg) throws IOException {
		v9t9.common.files.Catalog catalog;
		Pair<IEmulatedDisk, String> info = decode(arg);
		catalog = info.first.readCatalog();
		dumpCatalog(catalog, info.second);
	}

	private void dumpCatalog(v9t9.common.files.Catalog catalog, String pattern) {
		if (pattern == null) {
			out.println("Catalog of " + catalog.getDisk().getPath() + "\n");
			out.printf("Volume name:  %-10s  Used sectors: %5d  Total sectors: %5d\n\n",
					catalog.volumeName, catalog.usedSectors, catalog.totalSectors);
		}
		for (CatalogEntry entry : catalog.getEntries()) {
			if (!showTextFiles && entry.getFile() instanceof NativeTextFile)
				continue;
			if (pattern != null 
					&& entry.getFile().getFileName().matches(pattern))
				continue;
			
			String reclen = entry.typeCode != CatalogEntry.TYPE_PROGRAM ? 
					String.valueOf(entry.recordLength) : "";
			out.println(String.format("%-10s  %5d  %7s %3s  %s",
					entry.fileName, entry.secs, entry.type, reclen, entry.isProtected ? "Y" : " "));
		}
		out.println();
	}

}
