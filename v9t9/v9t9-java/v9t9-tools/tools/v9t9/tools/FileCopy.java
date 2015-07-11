/*
  FileCopy.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import v9t9.common.files.FDR;
import v9t9.common.files.IDiskImage;
import v9t9.common.files.IEmulatedDisk;
import v9t9.common.files.IEmulatedFile;
import v9t9.common.files.IFDROwner;
import v9t9.common.machine.IMachine;
import v9t9.tools.utils.BaseDiskUtil;
import v9t9.tools.utils.Category;
import v9t9.tools.utils.ToolUtils;
import ejs.base.logging.LoggingUtils;
import ejs.base.utils.Pair;

/**
 * @author ejs
 *
 */
@Category(Category.DISKUTILS)
public class FileCopy extends BaseDiskUtil {
	private static final String PROGNAME = FileCopy.class.getSimpleName();

	private static void help() {
        System.out.println("\n"
                        + "V9t9 Disk Copy\n"
                        + "\n" 
                        + "Copies files from one disk/directory to another\n"
                        + "\n"
                        + PROGNAME + " {srcdisk:namePattern|file}+ {dstdisk:name|file}\n"+
           			 "\n"+
           			 "where 'disk' is a path to a *.dsk or *.trk image and\n"+
           			 "'dir' is a disk directory or 'file' is a file, and\n"+
           			"'namePattern' is a emulated file (or regex) in that disk or directory"+
           			"'name' is the name of a single target file"
           			 );
    }

	public static void main(String[] args) {
		LoggingUtils.setupNullLogging();
		
        if (args.length == 0) {
        	help();
        	System.exit(0);
        }

        IMachine machine = ToolUtils.createMachine();
        		
        FileCopy copy = new FileCopy(machine, System.out);
        
        try {
        	copy.copy(args);
        } catch (IOException e) {
        	e.printStackTrace();
        }
	}

	public FileCopy(IMachine machine, PrintStream out) {
		super(machine, out);
	}

	private void copy(String[] args) throws IOException {
		List<IEmulatedFile> srcFiles = new ArrayList<IEmulatedFile>();
		for (int idx = 0; idx < args.length - 1; idx++) {
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
