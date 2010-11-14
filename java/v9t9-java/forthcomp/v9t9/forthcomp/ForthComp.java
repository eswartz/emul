/**
 * 
 */
package v9t9.forthcomp;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ejs.coffee.core.utils.HexUtils;
import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.DictEntry;
import v9t9.forthcomp.F99TargetContext;
import v9t9.forthcomp.F99bTargetContext;
import v9t9.forthcomp.ForthComp;
import v9t9.forthcomp.ForwardRef;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ITargetWord;
import v9t9.forthcomp.IWord;
import v9t9.forthcomp.TokenStream;

import v9t9.engine.files.DataFiles;
import v9t9.engine.memory.MemoryDomain;
import v9t9.forthcomp.words.HostDoubleLiteral;
import v9t9.forthcomp.words.HostLiteral;
import v9t9.forthcomp.words.HostVariable;
import v9t9.forthcomp.words.TargetContext;
import v9t9.forthcomp.words.TargetContext.IMemoryReader;

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
		String gromOutFile = null;
		PrintStream logfile = System.out;
		boolean doHistogram = false;
		
        Getopt getopt = new Getopt(PROGNAME, args, "?c:g:l:bh");
        int opt;
        while ((opt = getopt.getopt()) != -1) {
            switch (opt) {
            case '?':
                help();
                break;
            case 'c':
            	consoleOutFile = getopt.getOptarg();
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
            case 'h':
            	doHistogram = true;
            	break;
            	
            }
        }
        
        if (targetContext == null)
        	targetContext = new F99TargetContext(65536);
        HostContext hostContext = new HostContext(targetContext);
        ForthComp comp = new ForthComp(hostContext, targetContext);
        
        comp.setLog(logfile);
        
        if (getopt.getOptind() >= args.length) {
        	System.err.println(PROGNAME + ": no files specified");
        	System.exit(1);
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
    	logfile.println("DP = " + HexUtils.toHex4(comp.getTargetContext().getDP()));
	

    	comp.finish();
    	
    	if (comp.getErrors() > 0) {
    		System.err.println("Errors: " + comp.getErrors());
    		System.exit(1);
    	}
        
    	comp.getTargetContext().alignDP();
    	comp.saveMemory(consoleOutFile, gromOutFile);
    	
    	if (doHistogram) {
	    	List<DictEntry> sortedDict = new ArrayList<DictEntry>(comp.getTargetContext().getTargetDictionary().values());
	    	logfile.println("Top word uses of " + sortedDict.size() +":");
			
	    	Collections.sort(sortedDict, new Comparator<DictEntry>() {
					public int compare(DictEntry o1, DictEntry o2) {
						return o1.getUses() - o2.getUses();
					}
				}
	    	);
	    	for (DictEntry entry : sortedDict.subList(Math.max(0, sortedDict.size() - 32), sortedDict.size())) {
	    		logfile.println("\t" + entry.getUses() +"\t" + entry.getName() );
	    		
	    	}
    	}
	}

	private PrintStream logfile;

	private HostContext hostContext;
	private TargetContext targetContext;
	private TokenStream tokenStream;
	private HostVariable baseVar;
	private HostVariable stateVar;
	private int errors;

	public ForthComp(HostContext hostContext, TargetContext targetContext) {
		this.hostContext = hostContext;
		this.targetContext = targetContext;
		this.logfile = System.out;
		
		this.tokenStream = hostContext.getStream();

		defineCompilerWords();
		
		hostContext.defineHostCompilerWords();
	 	
	 	targetContext.defineCompilerWords(hostContext);
	}

	private void defineCompilerWords() {
		baseVar = (HostVariable) hostContext.define("base", new HostVariable(10));
		stateVar = (HostVariable) hostContext.define("state", new HostVariable(0));
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

	public void parseString(String text) throws AbortException {
		tokenStream.push(text);
		try {
			parse();
		} catch (AbortException e) {
			errors++;
			throw e;
		}
	}
	public void parse() throws AbortException {
		String token;
		try {
			while ((token = tokenStream.read()) != null)
				parse(token);
		} catch (IOException e) {
			throw abort(e.getMessage());
		}
	}

	private void parse(String token) throws AbortException {
		IWord word = null;
		
		if (stateVar.getValue() == 0) {
			word = hostContext.find(token);
			if (word == null) {
				word = targetContext.find(token);
			}
			if (word == null) {
				word = parseLiteral(token);
			}
			if (word == null) {
				throw abort("unknown word or literal: " + token);
			}
			
			if (word.getInterpretationSemantics() == null)
				throw abort(word.getName() + " has no interpretation semantics");
			
			word.getInterpretationSemantics().execute(hostContext, targetContext);
		} else {
			word = targetContext.find(token);
			if (word == null) {
				word = hostContext.find(token);
			}
			if (word == null) {
				word = parseLiteral(token);
			}
			if (word == null) {
				word = targetContext.defineForward(token, hostContext.getStream().getLocation());
			}
		
			ITargetWord targetWord = null;
			IWord hostWord = null;
			
			if (word instanceof ITargetWord) {
				targetWord = (ITargetWord) word;
				hostWord = hostContext.find(token);
				if (hostWord == null) 
					hostWord = targetWord;
			} else {
				hostWord = word;
				targetWord = null;
			}		
			
			hostContext.compileWord(targetContext, hostWord, targetWord);
		}
	}

	private AbortException abort(String string) {
		return tokenStream.abort(string);
	}

	private IWord parseLiteral(String token) {
		int radix = baseVar.getValue();
		boolean isNeg = token.startsWith("-");
		if (isNeg) {
			token = token.substring(1);
		}
		if (token.startsWith("$")) {
			radix = 16;
			token = token.substring(1);
		}
		boolean isDouble = false;
		if (token.contains(".")) {
			isDouble = true;
		}
		token = token.replaceAll("\\.", "");
		boolean isUnsigned = false;
		if (token.toUpperCase().endsWith("U")) {
			isUnsigned = true;
			token = token.substring(0, token.length() - 1);
		}
		try {
			long val = Long.parseLong(token, radix);
			if (isNeg)
				val = -val;
			if (isDouble) {
				if (targetContext.getCellSize() == 2)
					return new HostDoubleLiteral((int)(val & 0xffff), (int)(val >> 16), isUnsigned);
				else
					throw new UnsupportedOperationException();
			}
			else
				return new HostLiteral((int) val, isUnsigned);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	/**
	 * 
	 */
	public void finish() {
		for (ForwardRef ref : targetContext.getForwardRefs()) {
			logfile.println("*** Unresolved symbol: " + ref.getEntry().getName() + " (" + ref.getLocation() + ")");
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
	
		final MemoryDomain console = targetContext.createMemory();
		targetContext.exportMemory(console);
		
		TargetContext.dumpMemory(logfile, 0, targetContext.getDP(),
			new IMemoryReader() {
	
				public int readWord(int addr) {
					return console.readWord(addr);
				}
		});
		
		if (consoleOutFile != null) {
			System.out.println("Writing " + consoleOutFile);
			
			DataFiles.writeMemoryImage(new File(consoleOutFile).getAbsolutePath(), 
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
			console.getEntryAt(targetContext.getBaseDP()).writeSymbols(new PrintStream(fos));
			fos.close();
		}
				
	}

	/**
	 * 
	 */
	public void dumpDict() {
		targetContext.dumpDict(logfile, targetContext.getBaseDP(), targetContext.getDP());
		
	}

	public TargetContext getTargetContext() {
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
}
