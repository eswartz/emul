/**
 * 
 */
package v9t9.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import v9t9.common.files.Catalog;
import v9t9.common.files.CatalogEntry;
import v9t9.common.files.FDR;
import v9t9.common.files.IDiskImage;
import v9t9.common.files.IEmulatedDisk;
import v9t9.common.files.IEmulatedFile;
import v9t9.common.files.IEmulatedFileHandler;
import v9t9.common.files.IFDROwner;
import v9t9.common.machine.IMachine;
import v9t9.server.EmulatorLocalServer;
import v9t9.server.client.EmulatorServerBase;
import ejs.base.logging.LoggingUtils;
import ejs.base.utils.Pair;

/**
 * @author ejs
 *
 */
public class DiskUtils {
	private static final String PROGNAME = DiskUtils.class.getSimpleName();

	private static void help() {
        System.out.println("\n"
                        + "V9t9 Disk Utils\n"
                        + "\n" 
                        + PROGNAME + " [command] [args...]\n" +
           			 "\n"+
           			 "where command is one of:\n"+
           			 "\n"+
           			 "\tcatalog {disk|dir}[:name]\n"+
           			 "\tcopy {srcdisk:name|file}+ {dstdisk:name|file}\n"+
           			 "\n"+
           			 "where 'disk' is a path to a *.dsk or *.trk image and\n"+
           			 "'dir' is a disk directory or 'file' is a file, and\n"+
           			 "'name' is a emulated file in that disk or directory"
           			 
           			 );
    }

	public static void main(String[] args) {
		LoggingUtils.setupNullLogging();
		
        if (args.length == 0) {
        	help();
        	System.exit(0);
        }

        EmulatorServerBase server = new EmulatorLocalServer();
        String modelId = server.getMachineModelFactory().getDefaultModel();
        IMachine machine = createMachine(server, modelId);
        
        DiskUtils utils = new DiskUtils(machine, System.out);
        
        try {
	        if ("catalog".equals(args[0])) {
	        	utils.catalog(args);
	        }
	        else if ("copy".equals(args[0])) {
	        	utils.copy(args);
	        }
	        else {
	        	help();
	        	System.exit(0);
	        }
        } catch (IOException e) {
        	e.printStackTrace();
        }
	}


	/**
	 * @param modelId
	 * @return
	 */
	private static IMachine createMachine(EmulatorServerBase server, String modelId) {
		if (modelId == null)
			modelId = server.getMachineModelFactory().getDefaultModel();
		try {
			server.init(modelId);
		} catch (IOException e) {
			System.err.println("Failed to contact or create server:" + modelId);
			e.printStackTrace();
			System.exit(23);
			return null;
		}
		return server.getMachine();
	}

	private IMachine machine;
	private PrintStream out;
	
	/**
	 * @param machine
	 */
	public DiskUtils(IMachine machine, PrintStream out) {
		this.machine = machine;
		this.out = out;
	}


	/**
	 * @param substring
	 * @return
	 */
	private IEmulatedDisk resolveDisk(String path) throws IOException {
		File target = new File(path);
		IEmulatedFileHandler handler = machine.getEmulatedFileHandler();
		if (target.isDirectory())
			return handler.getFilesInDirectoryMapper().createDiskDirectory(target);
		else
			return handler.getDiskImageMapper().createDiskImage(target);
	}
	
	private Pair<IEmulatedDisk, String> decode(String arg) throws IOException {
		IEmulatedDisk disk;
		String pattern = null;
		
		int colIdx = arg.lastIndexOf(':');
		String path;
		if (colIdx > 1) {
			path = arg.substring(0, colIdx);
			arg = arg.substring(colIdx+1);
			pattern = "(?i)" + arg.replace("*", ".*").replace("?", ".");
		} else {
			path = arg;
		}
		disk = resolveDisk(path);
		
		return new Pair<IEmulatedDisk, String>(disk, pattern);
	}

	private void getSrcFiles(List<IEmulatedFile> srcFiles, IEmulatedDisk disk,
			String pattern) throws IOException {
		Catalog catalog = disk.readCatalog();
		for (CatalogEntry entry : catalog.getEntries()) {
			if (pattern == null 
					|| entry.getFile().getFileName().matches(pattern)) {
				srcFiles.add(entry.getFile());
			}
		}
	}

	private void catalog(String[] args) throws IOException {
		for (int idx = 1; idx < args.length; idx++) {
			String arg = args[idx];
			Catalog catalog;
			Pair<IEmulatedDisk, String> info = decode(arg);
			catalog = info.first.readCatalog();
			dumpCatalog(catalog, info.second);
		}			
	}

	private void dumpCatalog(Catalog catalog, String pattern) {
		if (pattern == null) {
			out.println("Catalog of " + catalog.getDisk().getPath() + "\n");
			out.printf("Volume name:  %-10s  Used sectors: %5d  Total sectors: %5d\n\n",
					catalog.volumeName, catalog.usedSectors, catalog.totalSectors);
		}
		for (CatalogEntry entry : catalog.getEntries()) {
			if (pattern == null 
					|| entry.getFile().getFileName().matches(pattern)) {
				String reclen = entry.typeCode != CatalogEntry.TYPE_PROGRAM ? 
						String.valueOf(entry.recordLength) : "";
				out.println(String.format("%-10s  %5d  %7s %3s  %s",
						entry.fileName, entry.secs, entry.type, reclen, entry.isProtected ? "Y" : " "));
			}
		}
		out.println();
	}


	private void copy(String[] args) throws IOException {
		List<IEmulatedFile> srcFiles = new ArrayList<IEmulatedFile>();
		for (int idx = 1; idx < args.length - 1; idx++) {
			String arg = args[idx];
			Pair<IEmulatedDisk, String> info = decode(arg);
			getSrcFiles(srcFiles, info.first, info.second);
		}
		
		Pair<IEmulatedDisk, String> info = decode(args[args.length - 1]);
		
		String dstFile = info.second;
		IEmulatedDisk dstDisk = info.first;
		
		if (!dstDisk.isFormatted()) {
			throw new IOException("target is not formatted (create or format it): " +dstDisk);
		}
		
		if (dstFile != null) {
			if (srcFiles.size() != 1) {
				throw new IOException("last argument should be a disk or directory");
			} else {
				try {
					copy(srcFiles.get(0), dstDisk, dstFile);
				} catch (IOException e) {
					System.err.println("failed to copy " + srcFiles.get(0) + " to " +dstDisk);
					e.printStackTrace();
				}
			}
		} else {
			// copying to a disk
			for (IEmulatedFile srcFile : srcFiles) {
				try {
					copy(srcFile, dstDisk, srcFile.getFileName());
				} catch (IOException e) {
					System.err.println("failed to copy " + srcFile + " to " +dstDisk);
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param srcFile
	 * @param first
	 * @param second
	 * @throws IOException 
	 */
	private void copy(IEmulatedFile srcFile, IEmulatedDisk dstDisk,
			String dstFilename) throws IOException {
		
		if (dstDisk instanceof IDiskImage)
			((IDiskImage)dstDisk).openDiskImage();
		
		byte[] contents = new byte[256];
		
		FDR srcFdr = null;
		if (srcFile instanceof IFDROwner) {
			srcFdr = ((IFDROwner) srcFile).getFDR();
		}
		IEmulatedFile dstFile = dstDisk.createFile(dstFilename, srcFdr);

		int left = srcFile.getSectorsUsed();
		int offset = 0;
		while (left > 0) {
			int len = srcFile.readContents(contents, 0, offset, 256);
			dstFile.writeContents(contents, 0, offset, 256);
			offset += len;
			left--;
		}
		dstFile.flush();
		
		if (dstDisk instanceof IDiskImage)
			((IDiskImage)dstDisk).closeDiskImage();

	}


}
