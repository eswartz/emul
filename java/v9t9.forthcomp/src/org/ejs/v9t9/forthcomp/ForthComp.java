/**
 * 
 */
package org.ejs.v9t9.forthcomp;

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
import org.ejs.v9t9.forthcomp.TargetContext.IMemoryReader;
import org.ejs.v9t9.forthcomp.words.Again;
import org.ejs.v9t9.forthcomp.words.Allot;
import org.ejs.v9t9.forthcomp.words.BackSlash;
import org.ejs.v9t9.forthcomp.words.Begin;
import org.ejs.v9t9.forthcomp.words.CharComma;
import org.ejs.v9t9.forthcomp.words.Colon;
import org.ejs.v9t9.forthcomp.words.Comma;
import org.ejs.v9t9.forthcomp.words.Constant;
import org.ejs.v9t9.forthcomp.words.Create;
import org.ejs.v9t9.forthcomp.words.DConstant;
import org.ejs.v9t9.forthcomp.words.DVariable;
import org.ejs.v9t9.forthcomp.words.Do;
import org.ejs.v9t9.forthcomp.words.Else;
import org.ejs.v9t9.forthcomp.words.ParsedTick;
import org.ejs.v9t9.forthcomp.words.PopExportState;
import org.ejs.v9t9.forthcomp.words.PushExportState;
import org.ejs.v9t9.forthcomp.words.Here;
import org.ejs.v9t9.forthcomp.words.If;
import org.ejs.v9t9.forthcomp.words.Include;
import org.ejs.v9t9.forthcomp.words.Lbracket;
import org.ejs.v9t9.forthcomp.words.Leave;
import org.ejs.v9t9.forthcomp.words.Loop;
import org.ejs.v9t9.forthcomp.words.Paren;
import org.ejs.v9t9.forthcomp.words.PlusLoop;
import org.ejs.v9t9.forthcomp.words.QuestionDo;
import org.ejs.v9t9.forthcomp.words.Rbracket;
import org.ejs.v9t9.forthcomp.words.Repeat;
import org.ejs.v9t9.forthcomp.words.SemiColon;
import org.ejs.v9t9.forthcomp.words.SetDP;
import org.ejs.v9t9.forthcomp.words.Then;
import org.ejs.v9t9.forthcomp.words.Tick;
import org.ejs.v9t9.forthcomp.words.UPlusLoop;
import org.ejs.v9t9.forthcomp.words.Until;
import org.ejs.v9t9.forthcomp.words.Variable;
import org.ejs.v9t9.forthcomp.words.While;

import v9t9.emulator.hardware.memory.EnhancedRamArea;
import v9t9.engine.files.DataFiles;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;

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
		
        Getopt getopt = new Getopt(PROGNAME, args, "?c:g:l:");
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
            }
        }
        
        if (targetContext == null)
        	targetContext = new F99TargetContext(65536);
        ForthComp comp = new ForthComp(targetContext);
        
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
    	
    	logfile.println("Top word uses:");
    	List<DictEntry> sortedDict = new ArrayList<DictEntry>(comp.getTargetContext().getDictionary().values());
		
    	Collections.sort(sortedDict, new Comparator<DictEntry>() {
				public int compare(DictEntry o1, DictEntry o2) {
					return o1.getUses() - o2.getUses();
				}
			}
    	);
    	for (DictEntry entry : sortedDict.subList(Math.max(0, sortedDict.size() - 16), sortedDict.size())) {
    		logfile.println("\t" + entry.getUses() +"\t" + entry.getName() );
    		
    	}
	}

	private PrintStream logfile;

	private HostContext hostContext;
	private TargetContext targetContext;
	private TokenStream tokenStream;
	private HostVariable baseVar;
	private HostVariable stateVar;
	private int errors;

	public ForthComp(TargetContext targetContext) {
		hostContext = new HostContext();
		tokenStream = hostContext.getStream();
		this.targetContext = targetContext;
		this.logfile = System.out;
		
		defineHostCompilerWords();
	 	
	 	targetContext.defineCompilerWords(hostContext);
	}

	private void defineHostCompilerWords() {
		baseVar = (HostVariable) hostContext.define("base", new HostVariable(10));
		stateVar = (HostVariable) hostContext.define("state", new HostVariable(0));
		hostContext.define("csp", new HostVariable(0));
		
		hostContext.define("(define-prims)", new IWord() {

			public void execute(HostContext hostContext,
					TargetContext targetContext) throws AbortException {
				targetContext.defineBuiltins();
			}

			public boolean isImmediate() {
				return false;
			}
			
		});
		hostContext.define("include", new Include());
		
		hostContext.define("<EXPORT", new PushExportState());
		hostContext.define("EXPORT>", new PopExportState());
		
		hostContext.define("create", new Create());
		hostContext.define("variable", new Variable());
		hostContext.define("dvariable", new DVariable());
		hostContext.define("constant", new Constant());
		hostContext.define("dconstant", new DConstant());
		hostContext.define("allot", new Allot());
		hostContext.define("'", new Tick());
		hostContext.define("[']", new ParsedTick());
		
		hostContext.define("!", new HostStore());
		hostContext.define("@", new HostFetch());
		hostContext.define("true", new HostConstant(-1));
		hostContext.define("false", new HostConstant(0));
		hostContext.define(":", new Colon());
		hostContext.define(";", new SemiColon());
		
		hostContext.define("if", new If());
		hostContext.define("else", new Else());
	 	hostContext.define("then", new Then());
	 	hostContext.define("begin", new Begin());
	 	hostContext.define("again", new Again());
	 	hostContext.define("until", new Until());
	 	hostContext.define("while", new While());
	 	hostContext.define("repeat", new Repeat());
	 	hostContext.define("do", new Do());
	 	hostContext.define("?do", new QuestionDo());
	 	hostContext.define("leave", new Leave());
	 	hostContext.define("loop", new Loop());
	 	hostContext.define("+loop", new PlusLoop());
	 	hostContext.define("u+loop", new UPlusLoop());

	 	hostContext.define("(", new Paren());
	 	hostContext.define("\\", new BackSlash());
	 	
	 	hostContext.define("[", new Lbracket());
	 	hostContext.define("]", new Rbracket());
	 	
	 	hostContext.define(",", new Comma());
	 	hostContext.define("c,", new CharComma());
	 	
		hostContext.define("DP!", new SetDP());
		hostContext.define("HERE", new Here());
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
		
		if (stateVar.getValue() == 0)
			word = hostContext.find(token);
		
		if (word == null) {
			word = targetContext.find(token);
			if (word instanceof ITargetWord && ((ITargetWord) word).getEntry().isHidden())
				word = null;
		}
		
		if (word == null) {
			word = hostContext.find(token);
			
		}
		
		if (word == null) {
			word = parseLiteral(token);
		}
		if (word == null) {
			word = targetContext.defineForward(token, hostContext.getStream().getLocation());
		}
		
		if (stateVar.getValue() == 0 || word.isImmediate()) {
			try {
				word.execute(hostContext, targetContext);
			} catch (AbortException e) {
				throw e;
			} catch (Throwable t) {
				throw abort("unexpected error at " + tokenStream.getLocation()+"\n"+t);
			}
		} else {
			// compiling
			if (word instanceof ITargetWord) {
				targetContext.compile((ITargetWord) word);
			} else if (word instanceof Literal) {
				targetContext.compileLiteral(((Literal) word).getValue(), ((Literal) word).isUnsigned());
			} else if (word instanceof DoubleLiteral) {
				targetContext.compileDoubleLiteral(((DoubleLiteral) word).getValue(), ((DoubleLiteral) word).isUnsigned());
				
			} else {
				throw abort("unknown compile-time semantics for " + token);
			}
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
			if (isDouble)
				return new DoubleLiteral(val, isUnsigned);
			else
				return new Literal((int) val, isUnsigned);
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
	 * 
	 */
	private void saveMemory(String consoleOutFile, String gromOutFile) throws FileNotFoundException, IOException {
	
		final MemoryDomain console = new MemoryDomain("CONSOLE");
		EnhancedRamArea bigRamArea = new EnhancedRamArea(0, 0x10000); 
		MemoryEntry bigRamEntry = new MemoryEntry("RAM", console, 0, MemoryDomain.PHYSMEMORYSIZE, 
				bigRamArea);
		console.mapEntry(bigRamEntry);
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
			bigRamEntry.writeSymbols(new PrintStream(fos));
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
