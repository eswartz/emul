/**
 * 
 */
package v9t9.tools.asm;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.RamArea;
import v9t9.tools.asm.directive.DescrDirective;
import v9t9.tools.asm.directive.LabelDirective;
import v9t9.tools.asm.transform.ConstPool;
import v9t9.tools.asm.transform.JumpFixer;
import v9t9.tools.asm.transform.Simplifier;
import v9t9.tools.llinst.MemoryRanges;
import v9t9.tools.llinst.ParseException;
import v9t9.utils.Utils;

/**
 * @author ejs
 *
 */
public class Assembler {
	private int DEBUG = 0;
	
	/** memory domain for area-sensitive view of the world */
    private MemoryDomain StdCPU = new MemoryDomain();
    private MemoryEntry StdCPURAM = new MemoryEntry("Std CPU RAM",
    		StdCPU, 0x8000, 0x400, new RamArea(0x400));
    private MemoryEntry StdCPUExpLoRAM = new MemoryEntry("Std CPU Low Exp RAM",
    		StdCPU, 0x2000, 0x2000, new RamArea(0x2000));
    private MemoryEntry StdCPUExpHiRAM = new MemoryEntry("Std CPU Hi Exp RAM",
    		StdCPU, 0xA000, 0x6000, new RamArea(0x6000));
    
    /** memory domain for the assembler's view of the world */
    private MemoryDomain CPUFullRAM = new MemoryDomain();
    private MemoryEntry CPUFullRAMEntry = new MemoryEntry("Assembler RAM",
    		CPUFullRAM, 0, 0x10000, new RamArea(0x10000));
    
	private List<DiskMemoryEntry> memoryEntries = new ArrayList<DiskMemoryEntry>();

	private PrintStream log = System.out;
	private PrintStream errlog = System.err;
	
    private MemoryRanges memoryRanges = new MemoryRanges();
    
    private Stack<ContentEntry> contentStack = new Stack<ContentEntry>();
    private int pc;
	private InstructionParser instructionParser = new InstructionParser();
	private SymbolTable symbolTable;
	private IdentityHashMap<Symbol, LabelDirective> labelTable;
	
	private ArrayList<RawInstruction> insts;
	//private HashMap<IInstruction, String> instDescrMap;
	/** Map of raw inst to the asm inst */
	private LinkedHashMap<IInstruction, IInstruction> resolvedToAsmInstMap;

	private ArrayList<AssemblerError> errorList;

	private ConstPool constPool = new ConstPool(this);
    
	private static final Pattern INCL_LINE = Pattern.compile(
			"\\s*incl\\s+(\\S+).*", Pattern.CASE_INSENSITIVE);
	
    public Assembler() {
    	CPUFullRAMEntry.map();
    	StdCPURAM.map();
    	StdCPUExpHiRAM.map();
    	StdCPUExpLoRAM.map();
    	
    	OperandParser operandParser = new OperandParser();
    	operandParser.appendStage(new AssemblerOperandParserStage(this));
    	StandardInstructionParserStage instStage = new StandardInstructionParserStage(operandParser);

    	// handle directives first to trap DATA and BYTE
    	instructionParser.appendStage(new DirectiveInstructionParserStage(operandParser));
    	instructionParser.appendStage(instStage);
    	instructionParser.appendStage(new MacroInstructionParserStage(
    			this, operandParser));

    	instructionParser.appendStage(new IInstructionParserStage() {

			public IInstruction[] parse(String descr, String string)
					throws ParseException {
				Matcher matcher = INCL_LINE.matcher(string);
				if (matcher.matches()) {
					String filename = matcher.group(1);
					try {
						pushContentEntry(new FileContentEntry(findFile(filename)));
					} catch (IOException e) {
						throw new ParseException("Could not include file: " + e.getMessage());
					}
					return new IInstruction[0];
				}
				return null;
			}
    		
    	});
    	
    	//instDescrMap = new HashMap<IInstruction, String>();
    	
    	symbolTable = new SymbolTable();
    	registerPredefinedSymbols();
    	
    	labelTable = new IdentityHashMap<Symbol, LabelDirective>();
    	errorList = new ArrayList<AssemblerError>();
	}

	private void registerPredefinedSymbols() {
		for (int i = 0; i < 16; i++) {
			symbolTable.addSymbol(new Equate(symbolTable, "R" + i, i));
		}
	}

	public void setList(PrintStream stream) {
		log = stream;
	}
	public void setError(PrintStream stream) {
		errlog = stream;
	}
	
	public File findFile(String filename) {
		for (ContentEntry entry : contentStack) {
			if (entry instanceof FileContentEntry) {
				File file = new File(((FileContentEntry) entry).getFile().getParentFile(), filename);
				if (file.exists())
					return file;
			}
		}
		return new File(filename);
	}
	public void pushContentEntry(ContentEntry entry) {
		contentStack.push(entry);
	}

	public Stack<ContentEntry> getContentEntryStack() {
		return contentStack;
	}
	
	public boolean assemble() {
		labelTable.clear();
		errorList.clear();
		constPool.clear();
		
		List<IInstruction> asmInsts = parse();
		if (errorList.size() > 0)
			return false;
		
		List<IInstruction> insts = resolve(asmInsts);
		if (errorList.size() > 0)
			return false;
		
		insts = resolve(asmInsts);
		insts = optimize(insts);
		if (errorList.size() >0)
			return false;
		
		// fix up any jumps
		//fixupJumps(insts);
		//if (errorList.size() >0)
		//	return false;
		
		generateObject(insts);
		if (errorList.size() >0)
			return false;
		
		
		saveMemory();
		
		return true;
	}
	
	public List<AssemblerError> getErrorList() {
		return errorList;
	}

	public List<IInstruction> parse() {
		List<IInstruction> asmInsts = new ArrayList<IInstruction>();
		while (true) {
			String line = getNextLine();
			if (line == null) {
				break;
			}
			
			ContentEntry entry = getLineFileContentEntry();
			String filename = entry.getName();
			int lineno = entry.getLine();
			
			try {
				assembleInst(asmInsts, line, filename, lineno);
			} catch (ParseException e) {
				reportError(e, filename, lineno, line, e.getMessage());
			}
		}
		return asmInsts;
	}

	/** Read the next line from the file stack */
	public String getNextLine() {
		while (!contentStack.isEmpty()) {
			String line = contentStack.peek().next();
			if (line == null) {
				contentStack.pop();
				continue;
			}
			return line;
		}
		return null;
	}
	
	/** Get the context for the previously read line */
	public ContentEntry getLineFileContentEntry() {
		return contentStack.peek();
	}
	
	private static final Pattern ASM_LINE = 
		Pattern.compile("(?:((?:[A-Za-z_][A-Za-z0-9_]*)|(?:\\$[0-9])):?(?:\\s*)(.*))|(?:\\s+(.*))");
	
	private void assembleInst(List<IInstruction> asmInsts, String line_, String filename, int lineno) throws ParseException {
		//if (DEBUG>0) log.println(descr+" " +line);
		
		// remove comments
		String line = line_.replaceAll(";.*", "");
		
		Matcher matcher = ASM_LINE.matcher(line);
		if (!matcher.matches())
			return;
		
		DescrDirective descr = new DescrDirective(filename, lineno, line_);
		asmInsts.add(descr);
		
		if (matcher.group(1) != null) {
			// label
			Symbol label = parseLabel(matcher.group(1));
			LabelDirective labelDir = new LabelDirective(label);
			labelTable.put(label, labelDir);
			asmInsts.add(labelDir);
			//instDescrMap.put(labelDir, descr);
			line = matcher.group(2);
		} else {
			line = matcher.group(3);
		}
		
		// check for an instruction
		line = line.trim();
		if (line.length() == 0)
			return;
			
		IInstruction[] instArray = instructionParser.parse(descr.toString(), line);
		/*
		for (IInstruction inst : instArray) {
			instDescrMap.put(inst, descr);
			if (DEBUG>0) log.println(inst);
		}
		*/
		asmInsts.addAll(Arrays.asList(instArray));
	}

	public void reportError(Exception e, String file, int lineno, String line, String message) {
		errorList.add(new AssemblerError(e, file, lineno, line));
		errlog.println(file + ":" + lineno + ": " + message + "\nin " + line);
	}
	public void reportError(Exception e, DescrDirective descr, String line, String message) {
		errorList.add(new AssemblerError(e, descr.getFilename(), descr.getLine(), line));
		errlog.println(descr.toString() + ": " + message + "\nin " + line);
	}
	public void reportError(Exception e) {
		errorList.add(new AssemblerError(e, null, 0, null));
		errlog.println(e.getMessage());
	}

	private Symbol parseLabel(String name) throws ParseException {
		if (DEBUG>0) log.println("Label: "+ name);
		
		Symbol label;
		
		// check for jump target
		if (name.charAt(0) == '$') {
			// redefine if defined before
			label = symbolTable.findSymbol(name);
			if (label != null && labelTable.containsKey(label))
				label = null;
		} else { 
			// see if the label was already defined in this scope
			label = symbolTable.findSymbolLocal(name);
			if (label != null && labelTable.containsKey(label)) {
				reportError(new ParseException("Redefining label: " + name));
			}
		}
		
		// define a label in this scope otherwise
		if (label == null)
			label = symbolTable.createSymbol(name);
		
		return label;
	}

	/** Reference a symbol, either returning the existing symbol
	 * or defining a forward reference
	 * @param string
	 * @return Symbol
	 */
	public Symbol referenceSymbol(String string) {
		Symbol symbol = symbolTable.findOrCreateSymbol(string);
		return symbol;
	}

	public SymbolTable getSymbolTable() {
		return symbolTable;
	}

	public RawInstruction lastInstruction() {
		if (insts == null || insts.size() == 0)
			return null;
		return insts.get(insts.size() - 1);
	}

	/**
	 * Convert high-level instructions into low-level instructions
	 * and resolve symbolic references.  
	 * @param asmInsts
	 * @return low-level machine instructions
	 */
	public List<IInstruction> resolve(List<IInstruction> asmInsts) {
		if (DEBUG>0) log.println("\n\n========= resolving\n");
		
		resolvedToAsmInstMap = new LinkedHashMap<IInstruction, IInstruction>();
		
		constPool.clear();
		
		// first, flatten instructions
		boolean anyErrors = false;
		this.pc = 0;
		List<IInstruction> insts = new ArrayList<IInstruction>();
		IInstruction prevAsmInst = null;
		DescrDirective prevDescr = null;
		for (IInstruction asminst : asmInsts) {
			if (asminst instanceof DescrDirective) {
				prevDescr = ((DescrDirective)asminst);
				insts.add(asminst);
				continue;
			}
			if (asminst instanceof BaseAssemblerInstruction) {
				
				try {
					IInstruction[] instArray = ((BaseAssemblerInstruction)asminst).resolve(
							this, prevAsmInst, false);
					insts.addAll(Arrays.asList(instArray));
					
					for (IInstruction inst : instArray) {
						resolvedToAsmInstMap.put(inst, asminst);
						
						if (inst instanceof LLInstruction) {
							constPool.injectInstruction((LLInstruction) inst);
						}
						if (DEBUG>0) log.println(Utils.toHex4(inst.getPc()) + "\t\t" + 
								Utils.padString(inst.toString(), 20) +"\t\t; " + asminst);
					}
				} catch (ResolveException e) {
					reportError(e, prevDescr, asminst.toString(), e.getMessage());
					anyErrors = true;
				}
			} else {
				insts.add(asminst);
			}
			prevAsmInst = asminst;
		}
		
		if (anyErrors)
			return insts;

		// define the const table if used
		Symbol constTableAddr = constPool.getTableAddr();
		if (constTableAddr != null && !constTableAddr.isDefined()) {
			constTableAddr.setAddr(getPc());
		}
		
		
		// now, iterate once more to handle any remaining forward operands
		IInstruction prevInst = null;
		prevDescr = null;
		for (IInstruction inst : insts) {
			if (inst instanceof DescrDirective) {
				prevDescr = ((DescrDirective)inst);
				continue;
			}
			try {
				// restore previous idea of instruction PC
				setPc(inst.getPc());
				if (inst instanceof BaseAssemblerInstruction) {
					((BaseAssemblerInstruction) inst).resolve(this, prevInst, true);
				}
			} catch (ResolveException e) {
				reportError(e, prevDescr, inst.toString(), e.getMessage());
				anyErrors = true;
			}
			prevInst = inst;
		}
		
		// dump
		if (anyErrors)
			return insts;
		
		if (DEBUG>0) {
			log.println("\n\n===========Final dump:\n");
		
			for (Map.Entry<IInstruction, IInstruction> entry : resolvedToAsmInstMap.entrySet()) {
				IInstruction inst = entry.getKey();
				IInstruction asmInst = entry.getValue();
				log.println(Utils.toHex4(inst.getPc()) + "\t\t" + 
						Utils.padString(inst.toString(), 20) +"\t\t; " + asmInst);
			}
		}
		return insts;
	}

	public int getPc() {
		return pc;
	}

	public void setPc(int immed) {
		this.pc = immed & 0xffff;
	}

	/**
	 * Optimize instructions
	 * @param insts
	 */
	public List<IInstruction> optimize(List<IInstruction> insts) {
		new Simplifier(insts).run();
		return resolve(insts);
	}
	
	/**
	 * Fix up any jumps that go too far
	 * @param insts
	 * @return
	 */
	public List<IInstruction> fixupJumps(List<IInstruction> insts) {
		try {
			return new JumpFixer(this, insts).run();
		} catch (ResolveException e) {
			reportError(e);
			return insts;
		}
	}
	/**
	 * Compile the final list of instructions to memory and
	 * product a listing
	 * @param insts
	 */
	public void generateObject(List<IInstruction> insts) {
		DescrDirective prevDescr = null;
		DescrDirective descr = null;
		boolean showDescr = false;
		byte[] none = new byte[0];
		for (IInstruction inst : insts) {
			if (inst instanceof DescrDirective) {
				if (showDescr) {
					dumpLine(prevDescr, descr, showDescr, -1, none);
				}
				prevDescr = descr;
				descr = (DescrDirective)inst;
				showDescr = true;
				continue;
			}
			if (!(inst instanceof BaseAssemblerInstruction))
				throw new IllegalArgumentException("Non-assembler instruction " + inst);
			
			byte[] mem = new byte[0];
			if (inst instanceof BaseAssemblerInstruction) {
				try {
					mem = ((BaseAssemblerInstruction) inst).getBytes();
					if (mem != null && mem.length > 0) {
						/*
						MemoryRange range = memoryRanges.getRangeContaining(inst.getPc());
						if (range == null) {
							errlog.println("Writing to non-saved memory: " + inst);
						} else {
							
						}*/
						for (int idx = 0; idx <  mem.length; idx++)
							CPUFullRAM.writeByte(inst.getPc() + idx, mem[idx]);
					}
				} catch (ResolveException e) {
					reportError(e, descr, inst.toString(), e.getMessage());
				}
			}
			dumpLine(prevDescr, descr, showDescr, inst.getPc(), mem);
			showDescr = false;
		}
	}

	/** Dump a line of content, either with or without a descr line.
	 * Dump up to 6 bytes on each line.
	 * @param showDescr 
	 * @param descr
	 * @param descr2 
	 * @param pc
	 * @param mem
	 */
	private void dumpLine(DescrDirective prev, DescrDirective cur, boolean showDescr, int pc, byte[] mem) {
		StringBuilder curLines = new StringBuilder();
		for (int offs = 0; showDescr || offs < mem.length; ) {
			if (showDescr) {
				if (prev == null || !prev.getFilename().equals(cur.getFilename())) {
					curLines.append("*** " + cur.getFilename() + "\n");
				}
				curLines.append(Utils.padString("" + cur.getLine(), 5) + " ");
			} else {
				curLines.append(Utils.padString("", 6));
			}
			
			if (pc >= 0) {
				curLines.append('>');
				curLines.append(Utils.toHex4(pc + offs));
				
				curLines.append(offs < mem.length ? '=' : ' ');
			} else {
				curLines.append("      ");
			}
			
			int cnt = 6;
			while (cnt-- >= 0) {
				if (offs < mem.length) {
					// eat a word if we're aligned on a word
					if (((pc + offs) & 1) == 0 && offs + 1 < mem.length) {
						curLines.append('>');
						curLines.append(Utils.toHex4(((mem[offs] & 0xff) << 8) | (mem[offs + 1] & 0xff)));
						offs += 2;
						cnt -= 2;
					} else {
						if (offs + 1 < mem.length)
							curLines.append("  ");
						curLines.append('>');
						curLines.append(Utils.toHex2(mem[offs] & 0xff));
							
						offs++;
						cnt--;
					}
					curLines.append(' ');
				} else {
					curLines.append("      ");
					cnt -= 2;
				}
			}
			
			if (showDescr) {
				curLines.append(cur.getContent());
				showDescr = false;
			}
			
			curLines.append('\n');
		}
		log.print(curLines);
	}

	public MemoryRanges getMemoryRanges() {
		return this.memoryRanges;
	}

	public MemoryDomain getConsole() {
		return StdCPU;
	}

	public void addMemoryEntry(DiskMemoryEntry entry) {
		memoryRanges.addRange(entry.addr, entry.size, true);
		memoryEntries.add(entry);
		entry.map();
	}

	private void saveMemory() {
		for (DiskMemoryEntry entry : memoryEntries) {
			try {
				entry.setDirty(true);
				entry.save();
			} catch (IOException e) {
				errlog.println("Failed to save: " + e.getMessage());
			}
		}
	}

	public void pushSymbolTable() {
		SymbolTable table = new SymbolTable(symbolTable);
		symbolTable = table;
	}

	public void popSymbolTable() throws ParseException {
		if (symbolTable.getParent() == null) {
			throw new ParseException("Cannot pop global symbol table");
		}
		symbolTable = symbolTable.getParent();
	}

	public ConstPool getConstPool() {
		return constPool;
	}

}
