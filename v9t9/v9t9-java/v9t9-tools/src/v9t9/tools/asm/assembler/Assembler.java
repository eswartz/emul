/**
 * 
 */
package v9t9.tools.asm.assembler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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

import ejs.base.utils.HexUtils;


import v9t9.common.asm.IInstruction;
import v9t9.common.asm.MemoryRanges;
import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.StockRamArea;
import v9t9.tools.asm.assembler.AssemblerError;
import v9t9.tools.asm.assembler.BaseAssemblerInstruction;
import v9t9.tools.asm.assembler.ConditionalInstructionParserStage;
import v9t9.tools.asm.assembler.ContentEntry;
import v9t9.tools.asm.assembler.DirectiveInstructionParserStage;
import v9t9.tools.asm.assembler.Equate;
import v9t9.tools.asm.assembler.FileContentEntry;
import v9t9.tools.asm.assembler.IAssembler;
import v9t9.tools.asm.assembler.IInstructionParserStage;
import v9t9.tools.asm.assembler.InstructionParser;
import v9t9.tools.asm.assembler.LLInstruction;
import v9t9.tools.asm.assembler.MacroInstructionParserStage;
import v9t9.tools.asm.assembler.OperandParser;
import v9t9.tools.asm.assembler.ParseException;
import v9t9.tools.asm.assembler.directive.DescrDirective;
import v9t9.tools.asm.assembler.directive.LabelDirective;
import v9t9.tools.asm.assembler.transform.ConstPool;

/**
 * @author ejs
 *
 */
public abstract class Assembler implements IAssembler {

	private int DEBUG = 0;

	public abstract List<IInstruction> fixupJumps(List<IInstruction> insts);

	public abstract List<IInstruction> optimize(List<IInstruction> insts);

	public abstract void setProcessor(String proc);

	/** memory domain for area-sensitive view of the world */
	protected IMemoryDomain StdCPU = new MemoryDomain(IMemoryDomain.NAME_CPU, "CPU Std");
	/** memory domain for the assembler's view of the world */
	protected MemoryDomain CPUFullRAM = new MemoryDomain(IMemoryDomain.NAME_CPU, "CPU Write");
	protected MemoryEntry CPUFullRAMEntry = new MemoryEntry("Assembler RAM",
	    		CPUFullRAM, 0, 0x10000, new StockRamArea(0x10000));
	private List<IMemoryEntry> memoryEntries = new ArrayList<IMemoryEntry>();
	private PrintStream log = System.out;
	private PrintStream errlog = System.err;
	private MemoryRanges memoryRanges = new MemoryRanges();
	private Stack<ContentEntry> contentStack = new Stack<ContentEntry>();
	private int pc;
	private InstructionParser instructionParser = new InstructionParser();
	protected SymbolTable symbolTable;
	protected IdentityHashMap<Symbol, LabelDirective> labelTable;
	private ArrayList<RawInstruction> insts;
	/** Map of raw inst to the asm inst */
	private LinkedHashMap<IInstruction, IInstruction> resolvedToAsmInstMap;
	protected ArrayList<AssemblerError> errorList;
	private ConstPool constPool = new ConstPool(this);
	protected IAsmInstructionFactory instructionFactory;
	protected int basicSize;
	private static final Pattern INCL_LINE = Pattern.compile(
				"\\s*incl\\s+(\\S+).*", Pattern.CASE_INSENSITIVE);
	private static final Pattern ASM_LINE = Pattern.compile("(?:((?:[A-Za-z_][A-Za-z0-9_]*)|(?:\\$[0-9])):?(?:\\s*)(.*))|(?:\\s+(.*))");

	/**
	 * @param operandParserStage 
	 * @param instStage 
	 * 
	 */
	protected void configureParser(OperandParser operandParser, IInstructionParserStage instStage) {
	
	
		// handle directives first to trap DATA and BYTE
		instructionParser.appendStage(new ConditionalInstructionParserStage(this, operandParser));
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
				
	}

	public void setList(PrintStream stream) {
		if (stream == null)
			stream = new PrintStream(new ByteArrayOutputStream());
		log = stream;
	}

	public void setError(PrintStream stream) {
		if (stream == null)
			stream = new PrintStream(new ByteArrayOutputStream());
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

	public static final String PROC_9900 = "9900";
	public static final String PROC_MFP201 = "MFP201";

	/**
	 * 
	 */
	public Assembler() {
		super();
	}

	private void assembleInst(List<IInstruction> asmInsts, String line_, String filename,
			int lineno) throws ParseException {
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

	public void reportError(Exception e, String file, int lineno,
			String line, String message) {
				errorList.add(new AssemblerError(e, file, lineno, line));
				errlog.println(file + ":" + lineno + ": " + message + "\nin " + line);
			}

	public void reportError(Exception e, DescrDirective descr, String line,
			String message) {
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

	public Symbol findForwardLocalLabel(String labelName) {
		// forward ref: don't use current one unless it's also unresolved
		Symbol symbol = getSymbolTable().findSymbolLocal(labelName);
		if (symbol == null || labelTable.containsKey(symbol))
			symbol = getSymbolTable().createSymbol(labelName);
		return symbol;
	}

	public Symbol findBackwardLocalLabel(String labelName) throws ParseException {
		// back ref: use previous one
		Symbol symbol = getSymbolTable().findSymbolLocal(labelName);
		if (symbol == null)
			throw new ParseException("No previous label " + labelName);
		return symbol;
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
							injectInstruction((LLInstruction) inst);
						}
						if (DEBUG>0) log.println(HexUtils.toHex4(inst.getPc()) + "\t\t" + 
								HexUtils.padString(inst.toString(),20) +"\t\t; " + asminst);
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
				log.println(HexUtils.toHex4(inst.getPc()) + "\t\t" + 
						HexUtils.padString(inst.toString(),20) +"\t\t; " + asmInst);
			}
		}
		return insts;
	}

	/** Add the opcode from the instruction to the const pool */
	abstract protected void injectInstruction(LLInstruction inst);

	public int getPc() {
		return pc;
	}

	public void setPc(int immed) {
		this.pc = immed & 0xffff;
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
					mem = ((BaseAssemblerInstruction) inst).getBytes(instructionFactory);
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
				} catch (Exception e) {
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
	private void dumpLine(DescrDirective prev, DescrDirective cur, boolean showDescr,
			int pc, byte[] mem) {
				StringBuilder curLines = new StringBuilder();
				for (int offs = 0; showDescr || offs < mem.length; ) {
					if (showDescr) {
						if (prev == null || !prev.getFilename().equals(cur.getFilename())) {
							curLines.append("*** " + cur.getFilename() + "\n");
						}
						curLines.append(HexUtils.padString(("" + cur.getLine()),5) + " ");
					} else {
						curLines.append(HexUtils.padString("",6));
					}
					
					if (pc >= 0) {
						curLines.append('>');
						curLines.append(HexUtils.toHex4((pc + offs)));
						
						curLines.append(offs < mem.length ? '=' : ' ');
					} else {
						curLines.append("      ");
					}
					
					int cnt = 6;
					while (cnt-- >= 0) {
						if (offs < mem.length) {
							if (basicSize == 2) {
								// eat a word if we're aligned on a word
								if (((pc + offs) & 1) == 0 && offs + 1 < mem.length) {
									curLines.append('>');
									curLines.append(HexUtils.toHex4((((mem[offs] & 0xff) << 8) | (mem[offs + 1] & 0xff))));
									offs += 2;
									cnt -= 2;
								} else {
									if (offs + 1 < mem.length)
										curLines.append("  ");
									curLines.append('>');
									curLines.append(HexUtils.toHex2((mem[offs] & 0xff)));
										
									offs++;
									cnt--;
								}
							} else {
								curLines.append('>');
								curLines.append(HexUtils.toHex2(mem[offs]));
								offs++;
								cnt--;
							}
							curLines.append(' ');
						} else {
							if (basicSize == 2) {
								curLines.append("      ");
								cnt -= 2;
							} else {
								curLines.append("    ");
								cnt --;
							}
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

	public IMemoryDomain getConsole() {
		return StdCPU;
	}

	public IMemoryDomain getWritableConsole() {
		return CPUFullRAM;
	}

	public void addMemoryEntry(IMemoryEntry entry) {
		memoryRanges.addRange(entry.getAddr(), entry.getSize(), true);
		memoryEntries.add(entry);
		getWritableConsole().mapEntry(entry);
	}

	private void saveMemory() {
		for (IMemoryEntry entry : memoryEntries) {
			try {
				if (entry instanceof DiskMemoryEntry) {
					DiskMemoryEntry diskEntry = (DiskMemoryEntry) entry;
					diskEntry.setDirty(true);
					diskEntry.save();
					
					writeSymbolTable(diskEntry);
				}
			} catch (IOException e) {
				errlog.println("Failed to save: " + e.getMessage());
			}
		}
	}

	private void writeSymbolTable(DiskMemoryEntry entry) throws IOException {
		FileOutputStream fos = new FileOutputStream(entry.getSymbolFilepath());
		PrintStream ps = new PrintStream(fos);
		for (Symbol symbol : getSymbolTable().getSymbols()) {
			ps.println(HexUtils.toHex4(symbol.getAddr()) + " " + symbol.getName());
		}
		ps.close();
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

	public void defineEquate(String equ) {
		int val = 1;
		int idx = equ.indexOf('=');
		if (idx > 0) {
			val = Integer.parseInt(equ.substring(idx+1));
			equ = equ.substring(0, idx);
		}
		Equate equate = new Equate(getSymbolTable(), equ, val);
		equate.setDefined(true);
		getSymbolTable().addSymbol(equate);
	}

	/**
	 * @return
	 */
	public IAsmInstructionFactory getInstructionFactory() {
		return instructionFactory;
	}

	/**
	 * @return
	 */
	public int getBasicAlignment() {
		return basicSize;
	}

}