/**
 * 
 */
package v9t9.tools.asm;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.directive.AorgDirective;
import v9t9.tools.asm.directive.AssemblerDirective;
import v9t9.tools.asm.directive.BssDirective;
import v9t9.tools.asm.directive.DefineByteDirective;
import v9t9.tools.asm.directive.DefineWordDirective;
import v9t9.tools.asm.directive.EquDirective;
import v9t9.tools.asm.directive.EvenDirective;
import v9t9.tools.llinst.ParseException;

/**
 * Parse directives
 * @author ejs
 *
 */
public class DirectiveInstructionParserStage implements IInstructionParserStage {

	static class DI {
		int argNum;
		Class<? extends AssemblerDirective> klass;
		public DI(int argNum, Class<? extends AssemblerDirective> klass) {
			this.argNum = argNum;
			this.klass = klass;
		}
	}
	
	private static final Map<String, DI> dirMap = new HashMap<String, DI>();
	private final OperandParser operandParser;
	static {
		dirMap.put("aorg", new DI(1, AorgDirective.class));
		dirMap.put("equ", new DI(1, EquDirective.class));
		dirMap.put("dw", new DI(-1, DefineWordDirective.class));
		dirMap.put("data", new DI(-1, DefineWordDirective.class));
		dirMap.put("db", new DI(-1, DefineByteDirective.class));
		dirMap.put("byte", new DI(-1, DefineByteDirective.class));
		dirMap.put("text", new DI(-1, DefineByteDirective.class));
		dirMap.put("bss", new DI(1, BssDirective.class));
		dirMap.put("even", new DI(0, EvenDirective.class));
		
	}
	public DirectiveInstructionParserStage(OperandParser operandParser) {
		this.operandParser = operandParser;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.IInstructionParserStage#parse(java.lang.String)
	 */
	public IInstruction[] parse(String descr, String string) throws ParseException {
		AssemblerTokenizer tokenizer = new AssemblerTokenizer(string);
		
		tokenizer.match(AssemblerTokenizer.ID);
		String dir = tokenizer.currentToken().toLowerCase();
		
		DI info = dirMap.get(dir);
		if (info == null)
			return null;
		
		int cnt = info.argNum;
		List<Operand> ops = new ArrayList<Operand>();
		while (cnt != 0) {
			Operand op = operandParser.parse(tokenizer);
			ops.add(op);
			if (cnt > 0) {
				cnt--;
				if (cnt == 0)
					break;
			}
			int t = tokenizer.nextToken();
			if (t == AssemblerTokenizer.EOF) {
				if (cnt > 0)
					throw new ParseException("Expected additional arguments");
				break;
			} else if (t != ',') { 
				throw new ParseException("Expected ','");
			}
		}
		if (tokenizer.nextToken() != AssemblerTokenizer.EOF) {
			throw new ParseException("Trailing garbage: " + tokenizer.currentToken());
		}
		
		try {
			Constructor<? extends AssemblerDirective> constructor = info.klass.getConstructor(
					List.class);
			AssemblerDirective directive = constructor.newInstance(ops);
			return new IInstruction[] { directive };
		} catch (Exception e) {
			throw (IllegalStateException) new IllegalStateException().initCause(e);
		}
	}

}
