/*
  Type.java

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

import v9t9.common.files.DsrException;
import v9t9.common.files.IEmulatedDisk;
import v9t9.common.files.IEmulatedFile;
import v9t9.common.files.NativeTextFile;
import v9t9.common.files.PabConstants;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.engine.files.directory.OpenFile;
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
public class Type extends BaseDiskUtil {
	private static final String PROGNAME = Type.class.getSimpleName();
	private boolean emitRaw;

	private static void help() {
        System.out.println("\n"
                        + "V9t9 File Typer\n"
                        + "\n"
                        + "Shows the content files in a disk or directory\n"
                        + "\n" 
                        + PROGNAME + " [options] {disk|dir}:namePattern\n"+
                        "\n"+
                        "Options:\n"+
                        "-r: raw output -- suppress newline between each record\n"+
           			 "\n"+
           			 "where 'disk' is a path to a *.dsk or *.trk image and\n"+
           			 "'dir' is a disk directory and\n"+
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
        
        Type typer = new Type(machine, System.out);
        		
		Getopt getopt;
		getopt = new Getopt(PROGNAME, args, "?r");        

		int opt;
		while ((opt = getopt.getopt()) != -1) {
			switch (opt) {
            case '?':
                help();
                break;
            case 'r': 
            	typer.setEmitRaw(true);
            	break;
			}
        }
		
        try {
            // leftover files are FIAD
            int idx = getopt.getOptind();
            while (idx < args.length) {
            	String name = args[idx++];
            	typer.type(name);
            }
        } catch (IOException e) {
        	e.printStackTrace();
        }
	}

	private void setEmitRaw(boolean b) {
		this.emitRaw = b;
		
	}

	public Type(IMachine machine, PrintStream out) {
		super(machine, out);
	}

	private void type(String arg) throws IOException {
		List<IEmulatedFile> srcFiles = new ArrayList<IEmulatedFile>();
		Pair<IEmulatedDisk, String> info = decode(arg);
		getSrcFiles(srcFiles, info.first, info.second);
		if (srcFiles.isEmpty()) {
			System.err.println("no files resolved for " + arg);
			return;
		}

		for (IEmulatedFile srcFile : srcFiles) {
			dumpContent(srcFile);
		}
	}

	private void dumpContent(IEmulatedFile file) throws IOException {
		OpenFile of = new OpenFile(file, "DSK1", file.getFileName());
		try {
			if (file instanceof NativeTextFile || of.isProgram()) {
				// dump whole thing
				byte[] contents = new byte[file.getFileSize()];
				file.readContents(contents, 0, 0, contents.length);
				out.write(contents);
			} else {
				byte[] record = new byte[256];
				ByteMemoryAccess access = new ByteMemoryAccess(record, 0);
				while (true) {
					access.offset = 0;
					int len = 0;
					try {
						len = of.readRecord(access, file.getRecordLength());
					} catch (DsrException e) {
						if (e.getErrorCode() == PabConstants.e_endoffile) {
							break;
						} else {
							throw e;
						}
					}
					if (len <= 0)
						break;
					out.write(record, 0, len);
					if (!emitRaw)
						out.println();
				}
			}
		} finally {
			of.close();
		}
		
	}

}
