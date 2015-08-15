/*
  ComputeMD5.java

  (c) 2015 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools;

import java.io.File;
import java.io.IOException;

import v9t9.common.files.IMD5SumFilter;
import v9t9.common.files.MD5FilterAlgorithms;
import v9t9.common.machine.IMachine;
import v9t9.tools.utils.Category;
import v9t9.tools.utils.ToolUtils;
import ejs.base.logging.LoggingUtils;
import gnu.getopt.Getopt;

/**
 * @author ejs
 *
 */
@Category(Category.SETUP)
public class ComputeMD5 {
    private static final String PROGNAME = ComputeMD5.class.getName();
    
    private static void help() {
	   System.out
	            .println("\n"
	                    + "V9t9 MD5 Computer\n"
	                    + "\n"
	                    + "Usage:   " + PROGNAME + " filterAlgorithmId file[:offset+length]...\n"
	                    + "\n"
	                    + PROGNAME + " will apply the given filter algorithm to the given file(s) and report the results.\n"
	                    + "A file may have a suffix of ':offset+length' (hex numbers) to sum up a segment of a file.\n"
	                    + "\n");
	}

    
    public static void main(String[] args) {
		LoggingUtils.setupNullLogging();

        Getopt getopt;
        
        getopt = new Getopt(PROGNAME, args, "?");
		int opt;
		
		while ((opt = getopt.getopt()) != -1) {
			switch (opt) {
            case '?':
                help();
                break;
            default:
                throw new AssertionError();
    
            }
        }
		
		ComputeMD5 compute = new ComputeMD5();
		
		IMD5SumFilter filter = null;
		
		int i = getopt.getOptind();
        if (i < args.length) {
        	String alg = args[i++];
        	
        	filter = MD5FilterAlgorithms.create(alg);
        	if (filter == null) {
        		System.err.println("cannot create filter for '" + alg + "'");
        		System.exit(1);
        	}
        }
		
		boolean any = false;
        while (i < args.length) {
        	any = true;
			String arg = args[i++];
			compute.compute(filter, arg);
		}
        
        if (!any) {
        	help();
        	return;
        }
	}

	
	private IMachine machine;
	
	public ComputeMD5() {
		machine = ToolUtils.createMachine();
		
	}
	
	public void compute(IMD5SumFilter filter, String path) {
		int offset = 0;
		int size = -1;
		
		int cidx = path.lastIndexOf(':');
		int pidx = path.indexOf('+', cidx+1);
		if (cidx > 0 && pidx > cidx) {
			offset = Integer.parseInt(path.substring(cidx + 1, pidx), 16);
			size = Integer.parseInt(path.substring(pidx+1), 16);
			path = path.substring(0, cidx);
		}
		
		File file = new File(path);
		if (!file.isFile()) {
			System.err.println("Not a file: " + path);
			return;
		}
		
		String md5;
		try {
			md5 = machine.getRomPathFileLocator().getContentMD5(file.toURI(), offset, size, filter);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("failed to compute MD5 from " + file);
			return;
		}
		
		System.out.println(md5 + " " + path);
	}
}
