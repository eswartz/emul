/**
 * 
 */
package v9t9.tools.asm.assembler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import v9t9.engine.cpu.IInstruction;

/**
 * Parse macro uses
 * @author ejs
 *
 */
public class MacroInstructionParserStage implements IInstructionParserStage {

	private static final Map<String, MacroInfo> macroMap = new HashMap<String, MacroInfo>();
	private final OperandParser operandParser;
	private final Assembler assembler;
	
	static {
		macroMap.put("si", new MacroInfo(new String[] { "reg", "immed" },
				" AI ${reg},-${immed}"));
	}
	public MacroInstructionParserStage(Assembler assembler, OperandParser operandParser) {
		this.assembler = assembler;
		this.operandParser = operandParser;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.IInstructionParserStage#parse(java.lang.String)
	 */
	public IInstruction[] parse(String descr, String string) throws ParseException {
		AssemblerTokenizer tokenizer = new AssemblerTokenizer(string);
		
		tokenizer.match(AssemblerTokenizer.ID);
		String macroName = tokenizer.currentToken().toLowerCase();
		
		if (macroName.equalsIgnoreCase("define")) {
			parseDefine(tokenizer, descr);
			return new IInstruction[0];
		}
		else if (macroName.equalsIgnoreCase("foreach")) {
			return parseForEach(tokenizer, descr);
		}
		else if (macroName.equalsIgnoreCase("pushscope")) {
			return parsePushScope(tokenizer, descr);
		}
		else if (macroName.equalsIgnoreCase("popscope")) {
			return parsePopScope(tokenizer, descr);
		}
		else {
			return parseMacroCall(tokenizer, macroName);
		}
	}

	/**
	 * Define a macro, syntax:
	 * <p>
	 * define macroname arg1,arg2,... {
	 * ...line1...
	 * ...more lines...
	 * }
	 * </p>
	 * @param tokenizer
	 * @param descr
	 */
	private void parseDefine(AssemblerTokenizer tokenizer, String descr) throws ParseException {
		List<String> argNames = new ArrayList<String>();
		
		tokenizer.match(AssemblerTokenizer.ID);
		String macroName = tokenizer.currentToken();
		
		int t;
		do {
			t = tokenizer.nextToken();
			if (t == AssemblerTokenizer.ID) {
				String argName = tokenizer.currentToken();
				if (argNames.contains(argName))
					throw new ParseException("Repeated argument " + argName);
				
				argNames.add(argName);
				t = tokenizer.nextToken();
				if (t == ',') {
					continue;
				}
			} else if (t == '.') {
				tokenizer.match('.');
				tokenizer.match('.');
				argNames.add("...");
				t = tokenizer.nextToken();
				break;
			}
			if (t != '[') {
				throw new ParseException("Expected '['");
			}
			
			// ensure we're at EOL here
			tokenizer.match(AssemblerTokenizer.EOF);
			break;
			
		} while (true);

		// now, commandeer the parser
		StringBuilder macroBody = new StringBuilder();
		String line;
		while ((line = assembler.getNextLine()) != null) {
			if (line.trim().equals("]")) 
				break;
			macroBody.append(line);
			macroBody.append('\n');
		}
		if (line == null)
			throw new ParseException("Unterminated macro definition for " + macroName+ " started at "
					+ descr);
		
		macroMap.put(macroName.toLowerCase(), new MacroInfo(
				(String[]) argNames.toArray(new String[argNames.size()]),
				macroBody.toString()));
	}

	private IInstruction[] parseMacroCall(AssemblerTokenizer tokenizer, String macroName)
			throws ParseException {
		MacroInfo info = macroMap.get(macroName);
		if (info == null)
			return null;
		
		// gather args
		final Map<String, String> argMap = new HashMap<String, String>();
		List<String> varargs = null;
		
		int argCnt = info.argNames.length;
		for (String argName : info.argNames) {
			
			if (argName.equals("...")) {
				varargs = new ArrayList<String>();
				break;
			}
			
			String argValue = parseArgument(tokenizer, ",");
			
			argMap.put(argName, argValue.trim());
			argCnt--;
			if (argCnt > 0) {
				int t = tokenizer.nextToken();
				if (t == AssemblerTokenizer.EOF) {
					throw new ParseException("Expected additional arguments after " + argName);
				} else if (t != ',') { 
					throw new ParseException("Expected ',' after " + argName);
				}
			}
		}
		
		if (varargs != null) {
			while (true) {
				String argValue = parseArgument(tokenizer, ",");
				
				varargs.add(argValue);
				int t = tokenizer.nextToken();
				if (t == AssemblerTokenizer.EOF) {
					break;
				} else if (t != ',') { 
					throw new ParseException("Expected ',' after " + argValue);
				}
			}
		}
		
		if (tokenizer.nextToken() != AssemblerTokenizer.EOF) {
			throw new ParseException("Trailing garbage: " + tokenizer.currentToken());
		}

		argMap.put("#", ""+ (varargs != null ? varargs.size() : 0));
		
		// now, push this stuff, and pretend we read something
		String expansion = info.expand(argMap);
		assembler.pushContentEntry(new MacroContentEntry(
				"<expansion of " + macroName + ">", 
				expansion,
				varargs));
		
		return new IInstruction[0];
	}

	
	private String parseArgument(AssemblerTokenizer tokenizer, String terminal) throws ParseException {
		final StringBuilder argBuilder = new StringBuilder();
		
		ITokenListener listener = new ITokenListener() {

			public void tokenRead(int pos, boolean followsSpace, String image) {
				if (followsSpace)
					argBuilder.append(' ');
				argBuilder.append(image);
			}
			
		};
		
		tokenizer.addTokenListener(listener);
		
		/*Operand op =*/ operandParser.parse(tokenizer);
		
		tokenizer.removeTokenListener(listener);
		
		// TODO: support pushback tokens... as it is, we get notice
		// of the trailing comma
		String argValue = argBuilder.toString();
		argValue = argValue.replaceAll("(.*)(" + terminal + ")\\s*", "$1");
		return argValue;
		
	}

	private IInstruction[] parseForEach(AssemblerTokenizer tokenizer, String descr) throws ParseException {

		// see if we're going backwards
		int direction = 1;
		
		int t = tokenizer.nextToken();
		if (t == '-') 
			direction = -1;
		else
			tokenizer.pushBack();
		
		// get the name for the item and the index var
		tokenizer.match(AssemblerTokenizer.ID);
		String eachVarName = tokenizer.currentToken();
		
		tokenizer.match(',');
		
		tokenizer.match(AssemblerTokenizer.ID);
		String eachIdxName = tokenizer.currentToken();
		
		MacroContentEntry macroContentEntry = null;
		List<String> varargs = null;
		
		Stack<ContentEntry> contentEntryStack = assembler.getContentEntryStack();
		for (ContentEntry entry : contentEntryStack) {
			if (entry instanceof MacroContentEntry) {
				macroContentEntry = (MacroContentEntry) entry;
				break;
			}
		}
		
		if (macroContentEntry == null) {
			// read items directly in parenthesized list
			tokenizer.match('(');
			
			varargs = new ArrayList<String>();
			
			while (true) {
				varargs.add(parseArgument(tokenizer, ",|\\)"));
				t = tokenizer.nextToken();
				if (t == ',')
					continue;
				if (t == ')')
					break;
				tokenizer.match(',');
			}
		} else {
			// get varargs from macro expansion
			varargs = macroContentEntry.getVarargs();
			if (varargs == null) {
				throw new ParseException("The macro " + macroContentEntry.getName() + " does not have varargs");
			}
		}
		
		// parse body of foreach { ... }
		tokenizer.match('{');
		
		StringBuilder foreachBody = new StringBuilder();
		String line;
		while ((line = assembler.getNextLine()) != null) {
			if (line.trim().equals("}")) 
				break;
			foreachBody.append(line);
			foreachBody.append('\n');
		}
		if (line == null)
			throw new ParseException("Unterminated foreach definition started at "
					+ descr);

		// now, iterate and append a copy for each argument
		StringBuilder foreachExpansion = new StringBuilder();
		
		int idx = direction > 0 ? 0 : varargs.size() - 1;
		if (direction < 0)
			Collections.reverse(varargs);
		
		for (String argument : varargs) {
			String expansion = foreachBody.toString().replaceAll(
					"\\$\\{" + eachVarName + "\\}",
					argument).replaceAll(
							"\\$\\{" + eachIdxName + "\\}",
							"" + idx);
			foreachExpansion.append(expansion);
			idx += direction;
		}
		
		// now, push this stuff, and pretend we read something
		assembler.pushContentEntry(new ContentEntry(
				"<expansion of foreach>", 
				foreachExpansion.toString()));
		
		return new IInstruction[0];
	}
	
	/**
	 * @param tokenizer  
	 * @param descr 
	 */
	private IInstruction[] parsePushScope(AssemblerTokenizer tokenizer, String descr) throws ParseException {

		assembler.pushSymbolTable();
		
		return new IInstruction[0];
	}	
	/**
	 * @param tokenizer  
	 * @param descr 
	 */
	private IInstruction[] parsePopScope(AssemblerTokenizer tokenizer, String descr) throws ParseException {
		
		assembler.popSymbolTable();
		
		return new IInstruction[0];
	}	
}
