/*
  ForthComp.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp;

import ejs.base.utils.HexUtils;
import ejs.base.utils.TextUtils;
import gnu.getopt.Getopt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import v9t9.common.files.DataFiles;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.engine.memory.ByteMemoryArea;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.tools.forthcomp.TargetContext.IMemoryReader;
import v9t9.tools.forthcomp.f99b.F99bTargetContext;
import v9t9.tools.forthcomp.ti99.TI99TargetContext;
import v9t9.tools.forthcomp.words.BarTest;
import v9t9.tools.forthcomp.words.HostVariable;
import v9t9.tools.forthcomp.words.IPrimitiveWord;
import v9t9.tools.forthcomp.words.TargetConstant;
import v9t9.tools.forthcomp.words.TestQuote;
import v9t9.tools.forthcomp.words.TestsStore;

/**
 * This class compiles FORTH programs into ROM images for V9t9
 * @author ejs
 *
 */
public class ForthComp {

	
	private static final String PROGNAME = ForthComp.class.getSimpleName();

	public static void main(String[] args) throws Exception {
		
		TargetContext targetContext = null;
		
		String consoleOutFile = null;
		String gromDictFile = null;
		String gromOutFile = null;
		PrintStream logfile = System.out;
		boolean doHistogram = false;
		boolean doTest = false;
		
        Getopt getopt = new Getopt(PROGNAME, args, "?c:l:b9hg:d:t");
        int opt;
        while ((opt = getopt.getopt()) != -1) {
            switch (opt) {
            case '?':
                help();
                break;
            case 'c':
            	consoleOutFile = getopt.getOptarg();
            	break;
            case 'd':
            	gromDictFile = getopt.getOptarg();
            	break;
            case 'g':
            	gromOutFile = getopt.getOptarg();
            	break;
            case 'l':
				logfile = new PrintStream(new File(getopt.getOptarg()));
            	break;
            case 'b':
            	targetContext = new F99bTargetContext(65536);
            	break;
            case '9':
            	targetContext = new TI99TargetContext(65536);
            	break;
            case 'h':
            	doHistogram = true;
            	break;
            case 't':
            	doTest = true;
            	break;
            	
            }
        }
        
        if (targetContext == null)
        	targetContext = new F99bTargetContext(65536);
        
        if (gromDictFile != null) {
        	if (targetContext instanceof IGromTargetContext) {
        		((IGromTargetContext) targetContext).setUseGromDictionary(true);
        		
        		MemoryDomain grom = new MemoryDomain(IMemoryDomain.NAME_GRAPHICS, false);
        		ByteMemoryArea memArea = new ByteMemoryArea(0, new byte[0x10000]); 
        		MemoryEntry bigRamEntry = new MemoryEntry("GRAM", grom, 0, MemoryDomain.PHYSMEMORYSIZE, 
        				memArea);
        		grom.mapEntry(bigRamEntry);

        		((IGromTargetContext) targetContext).setGrom(grom);
        	}
        	else {
        		System.err.println("Must use F99b for GROM dictionary");
        		System.exit(2);
        	}
        }
        
        HostContext hostContext = new HostContext(targetContext);
        final ForthComp comp = new ForthComp(hostContext, targetContext);
        
        comp.setTestMode(doTest);
        
        comp.setLog(logfile);
        
        if (getopt.getOptind() >= args.length) {
        	System.err.println(PROGNAME + ": no files specified");
        	System.exit(1);
        } 
        
        if (gromDictFile != null) {
        	targetContext.define("grom-dictionary", new TargetConstant(
        			new DictEntry(0, 0, "grom-dictionary"), 
        			1, targetContext.getCellSize()));
        }
        
    	int idx = getopt.getOptind();
    	while (idx < args.length) {
        	String name = args[idx];
        	try {
        		comp.parseFile(name);
        	} catch (AbortException e) {
        		System.err.println(e.getFile() +":" + e.getLine()+": " + e.getMessage());
        	}
        	idx++;
    	}
//    	logfile.println("DP = " + HexUtils.toHex4(comp.getTargetContext().getDP()));
//    	logfile.println("UP = " + HexUtils.toHex4(comp.getTargetContext().getUP()));
    	if (gromDictFile != null)
    		logfile.println("GDP = " + HexUtils.toHex4(((IGromTargetContext)comp.getTargetContext()).getGP()));

    	try {
    		comp.finish();
    	} catch (AbortException e) {
    		System.err.println(e.getFile() +":" + e.getLine()+": " + e.getMessage());
    	}
    		
    	
    	if (comp.getErrors() > 0) {
    		System.err.println("Errors: " + comp.getErrors());
    		System.exit(1);
    	}
        
    	comp.getTargetContext().alignDP();
    	comp.saveMemory(consoleOutFile, gromDictFile);
    	
    	if (doHistogram) {
	    	List<DictEntry> sortedDict = new ArrayList<DictEntry>(comp.getTargetContext().getTargetDictionary().values());
	    	logfile.println("Top 100 word uses of " + sortedDict.size() +":");
			
	    	Collections.sort(sortedDict, new Comparator<DictEntry>() {
					public int compare(DictEntry o1, DictEntry o2) {
						return o2.getUses() - o1.getUses();
					}
				}
	    	);
			for (int i = 0; i < sortedDict.size() && i < 100; i++) {
				DictEntry entry = sortedDict.get(i);
				logfile.print("\t" + entry.getUses() +"\t" + entry.getName() );
				if (entry.getTargetWord() instanceof IPrimitiveWord)
					logfile.print("\t" + "(primitive, size = " + 
							((IPrimitiveWord) entry.getTargetWord()).getPrimitiveSize() + ")");
				logfile.println();
			}
	    	
	    	
	    	logfile.println("Word sizes:");
			
	    	Collections.sort(sortedDict, new Comparator<DictEntry>() {
					public int compare(DictEntry o1, DictEntry o2) {
						return o1.getSize(comp.getTargetContext()) - o2.getSize(comp.getTargetContext());
					}
				}
	    	);
	    	
	    	int realSize = 0;
	    	int ifAlignedSize = 0;
	    	for (DictEntry entry : sortedDict) {
	    		int size = entry.getSize(comp.getTargetContext());
				logfile.println("\t" + entry.getName() + "\t" + size );
	    		realSize += size;
	    		ifAlignedSize += (size + 3) / 4 * 4;
	    	}
	    	System.out.println("real size = " + realSize + "; if aligned = " + ifAlignedSize);
	    	
	    	int headerSizes = 0;
	    	for (DictEntry entry : sortedDict) {
	    		if (entry.getHeaderSize() > 0) {
	    			System.out.println(": " + entry.getName());
	    			headerSizes += entry.getHeaderSize();
	    		}
	    	}
	    	System.out.println("headers size = " + headerSizes);
	    	
	    	targetContext.dumpStubs(logfile);
    	}
    	
    	if (gromOutFile != null && gromDictFile != null)
    		comp.mergeGromDictionary(gromDictFile, gromOutFile);

	}

	private PrintStream logfile;

	private HostContext hostContext;
	private TargetContext targetContext;
	private TokenStream tokenStream;
	private int errors;

	private UnitTests unitTests;

	public ForthComp(HostContext hostContext, TargetContext targetContext) {
		this.hostContext = hostContext;
		this.targetContext = targetContext;
		this.logfile = System.out;
		
		this.tokenStream = hostContext.getStream();

		defineCompilerWords();
		
		hostContext.defineHostCompilerWords();
	 	
	 	targetContext.defineCompilerWords(hostContext);
	 	
	 	targetContext.setHostContext(hostContext);
	}

	private void defineCompilerWords() {
		hostContext.define("base", new HostVariable(10));
		hostContext.define("state", new HostVariable(0));
		hostContext.define("(state)", new HostVariable(0));
	}

	public void setTestMode(boolean doTest) throws AbortException {
		if (unitTests == null) {
			unitTests = new UnitTests();
			unitTests.setCompiler(this);
		}
		targetContext.setTestMode(doTest);
		
		TestQuote testQuote = ((TestQuote) hostContext.require("TEST\"")); 
		testQuote.setUnitTests(unitTests);
		
		BarTest barTest = ((BarTest) hostContext.require("|TEST")); 
		barTest.setUnitTests(unitTests);
		
		TestsStore testsStore = ((TestsStore) hostContext.require("TESTS!")); 
		testsStore.setUnitTests(unitTests);
		
	}
		
	/**
	 * @return the hostContext
	 */
	public HostContext getHostContext() {
		return hostContext;
	}
	public void parseFile(String file) throws IOException, AbortException {
		tokenStream.push(new File(file));
		try {
			parse();
		} catch (AbortException e) {
			errors++;
			throw e;
		}
	}

	public void parseString(String name, String text) throws AbortException {
		LineNumberReader reader = tokenStream.push(name, text);
		try {
			try {
				String token;
				while ((token = tokenStream.read()) != null) {
					targetContext.parse(token);
					if (tokenStream.getCurrentReader() != reader)
						break;
				}
			} catch (IOException e) {
				throw abort(e.getMessage());
			}
		} catch (AbortException e) {
			errors++;
			throw e;
		}
	}
	public void parseString(String text) throws AbortException {
		parseString("<string>", text);
	}
	public void parse() throws AbortException {
		String token;
		try {
			while ((token = tokenStream.read()) != null) {
//				System.out.println("> " + token);
				targetContext.parse(token);
			}
		} catch (IOException e) {
			throw abort(e.getMessage());
		}
	}

	private AbortException abort(String string) {
		return tokenStream.abort(string);
	}

	
	/**
	 * @throws AbortException 
	 * 
	 */
	public void finish() throws AbortException {
		for (ForwardRef ref : targetContext.getForwardRefs()) {
			logfile.println("*** Unresolved symbol: " + ref.getEntry().getName() + " (" + ref.getLocation() + ")");
			System.err.println("*** Unresolved symbol: " + ref.getEntry().getName() + " (" + ref.getLocation() + ")");
			errors++;
		}
		if (!hostContext.getDataStack().isEmpty()) {
			System.err.println("*** Items left on stack: " + 
					TextUtils.catenateStrings(hostContext.getDataStack(), ", "));
			errors++;
		}
	}

	/**
	 * @param logfile
	 */
	public void setLog(PrintStream logfile) {
		this.logfile = logfile;
		targetContext.setLog(logfile);
	}

	/**
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws AbortException 
	 * 
	 */
	private void saveMemory(String consoleOutFile, String gromOutFile) throws FileNotFoundException, IOException, AbortException {
	
		final IMemoryDomain console = targetContext.createMemory();
		targetContext.exportMemory(console);
		
		TargetContext.dumpMemory(logfile, 0, targetContext.getDP(),
			new IMemoryReader() {
	
				public int readWord(int addr) {
					return console.readWord(addr);
				}
		});
		
		System.out.println("# words: " + targetContext.getDictionary().size());
		
		if (consoleOutFile != null) {
			System.out.println("Writing " + consoleOutFile);
			
			DataFiles.writeMemoryImage(new File(consoleOutFile), 
					0, targetContext.getDP(), 
					console);
			
			File symfile;
			int didx = consoleOutFile.lastIndexOf('.');
	        if (didx >= 0) {
	        	symfile = new File(consoleOutFile.substring(0, didx) + ".sym");
	        } else {
	        	symfile = new File(consoleOutFile + ".sym");
	        }
			FileOutputStream fos = new FileOutputStream(symfile);
			for (IMemoryEntry entry : console.getFlattenedMemoryEntries())
				entry.writeSymbols(new PrintStream(fos));
			fos.close();
		}
			
		if (gromOutFile != null) {
			IGromTargetContext f99bCtx = (IGromTargetContext) targetContext;
			
			final MemoryDomain gromMemory = f99bCtx.getGrom();
			
			System.out.println("Writing " + gromOutFile);
			
			DataFiles.writeMemoryImage(new File(gromOutFile), 
					0, f99bCtx.getGP(), 
					gromMemory);

		}
	}

	/**
	 * 
	 */
	public void dumpDict() {
		targetContext.dumpDict(logfile, targetContext.getBaseDP(), targetContext.getDP());
		
	}

	public ITargetContext getTargetContext() {
		return targetContext;
	}

	private static void help() {
		System.out.println("Help me!");
	}

	/**
	 * @return the errors
	 */
	public int getErrors() {
		return errors;
	}
	
	protected void mergeGromDictionary(String gromDictPath, String gromFilePath) throws IOException {

    	// see if we need to merge the GROM dictionary
    	File dictFile = null;

    	File gromFile = new File(gromFilePath);
    	dictFile = new File(gromDictPath);
    	if (gromFile.exists() && dictFile.exists() && dictFile.lastModified() > gromFile.lastModified()) {

    		int gromFileSize = (int) gromFile.length();
    		byte[] grom;
    		
			int gromDictSize = (int) dictFile.length();

			grom = DataFiles.readMemoryImage(new File(gromFilePath), 0, gromFileSize);
			int gromDictBase = (grom[2] << 8) | (grom[3] & 0xff);
			
			if (gromDictSize + gromDictBase > grom.length) {
				System.err.println("GROM dictionary too big!  GROM plus dictionary maxes out at "+grom.length + " bytes.");
			}
			
			byte[] gromDict = DataFiles.readMemoryImage(new File(gromDictPath), 0, gromDictSize);
			
			for (int i = 0; i < gromDictSize; i++) {
				grom[i + gromDictBase] = gromDict[i];
			}
			
			int end = gromDictSize + gromDictBase;
			grom[4] = (byte) (end >> 8);
			grom[5] = (byte) (end & 0xff);
			
			DataFiles.writeMemoryImage(gromFile.getAbsolutePath(), grom.length, grom);
			
			System.out.println("Merged dictionary into GROM, changed " + gromFile);
    	}
    	
	}

	/**
	 * @return
	 */
	public UnitTests getUnitTests() {
		return unitTests;
	}
}
