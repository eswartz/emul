/**
 * 
 */
package v9t9.tools.asm;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import v9t9.emulator.runtime.HighLevelCodeInfo;
import v9t9.engine.cpu.AssemblerOperand;
import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.StandardConsoleMemoryModel;
import v9t9.tools.llinst.ParseException;

/**
 * @author ejs
 *
 */
public class Assembler {
    Memory memory = new Memory();
    StandardConsoleMemoryModel memoryModel = new StandardConsoleMemoryModel(memory);
    MemoryDomain CPU = memoryModel.CPU;

    private AssemblerOptions options;
    HighLevelCodeInfo highLevel = new HighLevelCodeInfo(CPU);
    
    Stack<FileEntry> fileStack = new Stack<FileEntry>();
	int pc;
	private InstructionParser instructionParser = new InstructionParser();
	private SymbolTable symbolTable;
    
    public Assembler(AssemblerOptions options) {
    	this.options = options;
    	memory = new Memory();
    	
    	OperandParser operandParser = new OperandParser();
    	operandParser.appendStage(new AssemblerOperandParserStage(this));
    	StandardInstructionParserStage instStage = new StandardInstructionParserStage(operandParser);
    	
    	instructionParser.appendStage(instStage);
    	instructionParser.appendStage(new DirectiveInstructionParser(this));
    	
    	symbolTable = new SymbolTable();
    	registerPredefinedSymbols();
	}

	private void registerPredefinedSymbols() {
		symbolTable.addSymbol(new Equate("R0", 0));
		symbolTable.addSymbol(new Equate("R1", 1));
		symbolTable.addSymbol(new Equate("R2", 2));
		symbolTable.addSymbol(new Equate("R3", 3));
		symbolTable.addSymbol(new Equate("R4", 4));
		symbolTable.addSymbol(new Equate("R5", 5));
		symbolTable.addSymbol(new Equate("R6", 6));
		symbolTable.addSymbol(new Equate("R7", 7));
		symbolTable.addSymbol(new Equate("R8", 8));
		symbolTable.addSymbol(new Equate("R9", 9));
		symbolTable.addSymbol(new Equate("R10", 10));
		symbolTable.addSymbol(new Equate("R11", 11));
		symbolTable.addSymbol(new Equate("R12", 12));
		symbolTable.addSymbol(new Equate("R13", 13));
		symbolTable.addSymbol(new Equate("R14", 14));
		symbolTable.addSymbol(new Equate("R15", 15));
	}

	public void pushFileEntry(FileEntry entry) {
		fileStack.push(entry);
	}

	public void assemble() {
		while (!fileStack.isEmpty()) {
			String line = fileStack.peek().next();
			if (line == null) {
				fileStack.pop();
				continue;
			}
			
			String descr = fileStack.peek().describe();
			
			assembleInst(descr, line);
		}
	}

	private static final Pattern ASM_LINE = 
		Pattern.compile("(?:(\\S+)(?:\\s*)(.*))|(?:\\s+(.*))");
	
	private void assembleInst(String descr, String line) {
		System.out.println(descr+" " +line);
		
		// remove comments
		line = line.replaceAll(";.*", "");
		
		Matcher matcher = ASM_LINE.matcher(line);
		if (!matcher.matches())
			return;
		
		
		if (matcher.group(1) != null) {
			// label
			parseLabel(matcher.group(1));
			line = matcher.group(2);
		} else {
			line = matcher.group(3);
		}
		
		// check for an instruction
		line = line.trim();
		if (line.length() == 0)
			return;
			
		try {
			Instruction[] insts = instructionParser.parse(line); 
			for (Instruction inst : insts)
				System.out.println(inst);
		} catch (ParseException e) {
			reportError(descr, line, e.getMessage());
		}
	}

	private void reportError(String descr, String line, String message) {
		System.err.println(descr + ": " + message + "\nin " + line);
	}

	private void parseLabel(String line) {
		System.out.println("Label: "+ line);
	}

	/** Reference a symbol, either returning the existing symbol
	 * or defining a forward reference
	 * @param string
	 * @return Symbol
	 */
	public Symbol referenceSymbol(String string) {
		Symbol symbol = symbolTable.findSymbol(string);
		if (symbol == null)
			symbol = symbolTable.defineSymbol(string);
		return symbol;
	}

	public SymbolTable getSymbolTable() {
		return symbolTable;
	}

	public void resolveInstruction(Instruction inst) throws ResolveException {
		inst.op1 = resolveOperand(inst, inst.op1);
		inst.op2 = resolveOperand(inst, inst.op2);
	}

	public MachineOperand resolveOperand(Instruction inst, Operand op) throws ResolveException {
		if (op instanceof AssemblerOperand)
			return ((AssemblerOperand) op).resolve(this, inst);
		if (op instanceof MachineOperand)
			return (MachineOperand) op;
		throw new ResolveException(inst, op, "Unknown operand type");
	}
	
	

}
