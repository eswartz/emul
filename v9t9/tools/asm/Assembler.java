/**
 * 
 */
package v9t9.tools.asm;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.StandardConsoleMemoryModel;
import v9t9.tools.asm.directive.DescrDirective;
import v9t9.tools.asm.directive.LabelDirective;
import v9t9.tools.llinst.MemoryRanges;
import v9t9.tools.llinst.ParseException;
import v9t9.utils.Utils;

/**
 * @author ejs
 *
 */
public class Assembler {
	private int DEBUG = 0;
	
	private Memory memory = new Memory();
    private StandardConsoleMemoryModel memoryModel = new StandardConsoleMemoryModel(memory);
    private MemoryDomain CPU = memoryModel.CPU;
	private List<DiskMemoryEntry> memoryEntries = new ArrayList<DiskMemoryEntry>();

	private PrintStream log = System.out;
	private PrintStream errlog = System.err;
	
    private MemoryRanges memoryRanges = new MemoryRanges();
    
    private Stack<FileContentEntry> fileStack = new Stack<FileContentEntry>();
    private int pc;
	private InstructionParser instructionParser = new InstructionParser();
	private SymbolTable symbolTable;
	private ArrayList<RawInstruction> insts;
	private HashMap<Symbol, IInstruction> symbolRefMap;
	//private HashMap<IInstruction, String> instDescrMap;
	/** Map of raw inst to the asm inst */
	private LinkedHashMap<IInstruction, IInstruction> resolvedToAsmInstMap;
	private List<Symbol> tempSymbolRefs;
	private boolean anyErrors;
    
	private static final Pattern INCL_LINE = Pattern.compile(
			"\\s*incl\\s+(\\S+).*", Pattern.CASE_INSENSITIVE);
	
    public Assembler() {
    	memory = new Memory();
    	
    	OperandParser operandParser = new OperandParser();
    	operandParser.appendStage(new AssemblerOperandParserStage(this));
    	StandardInstructionParserStage instStage = new StandardInstructionParserStage(operandParser);

    	// handle directives first to trap DATA and BYTE
    	instructionParser.appendStage(new DirectiveInstructionParserStage(operandParser));
    	instructionParser.appendStage(instStage);
    	instructionParser.appendStage(new IInstructionParserStage() {

			public IInstruction[] parse(String descr, String string)
					throws ParseException {
				Matcher matcher = INCL_LINE.matcher(string);
				if (matcher.matches()) {
					String filename = matcher.group(1);
					try {
						pushFileEntry(new FileContentEntry(findFile(filename)));
					} catch (IOException e) {
						throw new ParseException("Could not include file: " + e.getMessage());
					}
					return new IInstruction[0];
				}
				return null;
			}
    		
    	});
    	
    	symbolRefMap = new HashMap<Symbol, IInstruction>();
    	//instDescrMap = new HashMap<IInstruction, String>();
    	
    	symbolTable = new SymbolTable();
    	registerPredefinedSymbols();
    	
	}

	private void registerPredefinedSymbols() {
		for (int i = 0; i < 16; i++) {
			symbolTable.addSymbol(new Equate("R" + i, i));
		}
	}

	public void setList(PrintStream stream) {
		log = stream;
	}
	public void setError(PrintStream stream) {
		errlog = stream;
	}
	
	public File findFile(String filename) {
		for (FileContentEntry entry : fileStack) {
			File file = new File(entry.getFile().getParentFile(), filename);
			if (file.exists())
				return file;
		}
		return new File(filename);
	}
	public void pushFileEntry(FileContentEntry entry) {
		fileStack.push(entry);
	}

	public boolean assemble() {
		anyErrors = false;
		
		List<IInstruction> asmInsts = parse();
		if (anyErrors)
			return false;
		
		List<IInstruction> insts = resolve(asmInsts);
		if (anyErrors)
			return false;
		
		compile(insts);
		if (anyErrors)
			return false;
		
		saveMemory();
		
		return true;
	}

	public List<IInstruction> parse() {
		List<IInstruction> asmInsts = new ArrayList<IInstruction>();
		while (!fileStack.isEmpty()) {
			String line = fileStack.peek().next();
			if (line == null) {
				fileStack.pop();
				continue;
			}
			
			FileContentEntry entry = fileStack.peek();
			String filename = entry.getName();
			int lineno = entry.getLine();
			
			try {
				assembleInst(asmInsts, line, filename, lineno);
			} catch (ParseException e) {
				reportError(filename, lineno, line, e.getMessage());
				anyErrors = true;
			}
		}
		return asmInsts;
	}
	
	private static final Pattern ASM_LINE = 
		Pattern.compile("(?:([A-Za-z_][A-Za-z0-9_]*):?(?:\\s*)(.*))|(?:\\s+(.*))");
	
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

	public void reportError(String file, int lineno, String line, String message) {
		errlog.println(file + ":" + lineno + ": " + message + "\nin " + line);
	}
	public void reportError(DescrDirective descr, String line, String message) {
		errlog.println(descr.toString() + ": " + message + "\nin " + line);
	}

	private Symbol parseLabel(String name) throws ParseException {
		if (DEBUG>0) log.println("Label: "+ name);
		Symbol label = symbolTable.findSymbol(name);
		if (label != null && label.isDefined())
			throw new ParseException("Redefining label: " + name);
		label = symbolTable.declareSymbol(name);
		return label;
	}

	/** Reference a symbol, either returning the existing symbol
	 * or defining a forward reference
	 * @param string
	 * @return Symbol
	 */
	public Symbol referenceSymbol(String string) {
		Symbol symbol = symbolTable.declareSymbol(string);
		return symbol;
	}

	public void noteSymbolReference(Symbol symbol) {
		if (tempSymbolRefs != null) {
			tempSymbolRefs.add(symbol);
		}
	}
	
	private void addSymbolReference(Symbol symbol, IInstruction inst) {
		if (!symbolRefMap.containsKey(symbol)) {
			symbolRefMap.put(symbol, inst);
		}
	}
	
	public SymbolTable getSymbolTable() {
		return symbolTable;
	}

	public IInstruction[] resolveInstruction(IInstruction inst) throws ResolveException {
		return inst.resolve(this, null, true);
	}

	public MachineOperand resolveOperand(RawInstruction inst, Operand op) throws ResolveException {
		return op.resolve(this, inst);
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
		
		// first, flatten instructions
		boolean anyErrors = false;
		this.pc = 0;
		List<IInstruction> insts = new ArrayList<IInstruction>();
		IInstruction prevAsmInst = null;
		DescrDirective prevDescr = null;
		tempSymbolRefs = new ArrayList<Symbol>();
		for (IInstruction asminst : asmInsts) {
			if (asminst instanceof DescrDirective) {
				prevDescr = ((DescrDirective)asminst);
				insts.add(asminst);
				continue;
			}
			try {
				tempSymbolRefs.clear();
				IInstruction[] instArray = asminst.resolve(this, prevAsmInst, false);
				insts.addAll(Arrays.asList(instArray));
				
				for (IInstruction inst : instArray) {
					resolvedToAsmInstMap.put(inst, asminst);
					
					for (Symbol symbol : tempSymbolRefs) {
						addSymbolReference(symbol, asminst);
					}
					
					if (DEBUG>0) log.println(Utils.toHex4(inst.getPc()) + "\t\t" + 
							Utils.padString(inst.toString(), 20) +"\t\t; " + asminst);
				}
			} catch (ResolveException e) {
				reportError(prevDescr, asminst.toString(), e.getMessage());
				anyErrors = true;
			}
			prevAsmInst = asminst;
		}
		
		if (anyErrors)
			return insts;

		// now, iterate once more to handle any remaining symbolic operands
		IInstruction prevInst = null;
		prevDescr = null;
		for (IInstruction inst : insts) {
			if (inst instanceof DescrDirective) {
				prevDescr = ((DescrDirective)inst);
				continue;
			}
			try {
				setPc(inst.getPc());
				inst.resolve(this, prevInst, true);
			} catch (ResolveException e) {
				reportError(prevDescr, inst.toString(), e.getMessage());
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

	public void compile(List<IInstruction> insts) {
		DescrDirective prevDescr = null;
		DescrDirective descr = null;
		boolean showDescr = false;
		byte[] none = new byte[0];
		for (IInstruction inst : insts) {
			if (inst instanceof DescrDirective) {
				if (showDescr) {
					dumpLine(prevDescr, descr, showDescr, inst.getPc(), none);
				}
				prevDescr = descr;
				descr = (DescrDirective)inst;
				showDescr = true;
				continue;
			}
			byte[] mem = inst.getBytes();
			if (mem != null && mem.length > 0) {
				/*
				MemoryRange range = memoryRanges.getRangeContaining(inst.getPc());
				if (range == null) {
					errlog.println("Writing to non-saved memory: " + inst);
				} else {
					
				}*/
				for (int idx = 0; idx <  mem.length; idx++)
					CPU.writeByte(inst.getPc() + idx, mem[idx]);
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
			curLines.append('>');
			curLines.append(Utils.toHex4(pc + offs));
			
			curLines.append(offs < mem.length ? '=' : ' ');
			
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
		return CPU;
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
	
}
