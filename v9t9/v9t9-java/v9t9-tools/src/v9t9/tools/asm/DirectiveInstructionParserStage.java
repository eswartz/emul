/*
  DirectiveInstructionParserStage.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.IOperand;
import v9t9.tools.asm.directive.AorgDirective;
import v9t9.tools.asm.directive.BssDirective;
import v9t9.tools.asm.directive.ConstPoolDirective;
import v9t9.tools.asm.directive.DefineByteDirective;
import v9t9.tools.asm.directive.DefineWordDirective;
import v9t9.tools.asm.directive.Directive;
import v9t9.tools.asm.directive.EquDirective;
import v9t9.tools.asm.directive.EvenDirective;
import v9t9.tools.asm.directive.IgnoreDirective;

/**
 * Parse directives
 * @author ejs
 *
 */
public class DirectiveInstructionParserStage implements IInstructionParserStage {

	static class DirectiveInfo {
		int argNum;
		Class<? extends Directive> klass;
		public DirectiveInfo(int argNum, Class<? extends Directive> klass) {
			this.argNum = argNum;
			this.klass = klass;
		}
	}
	
	private static final Map<String, DirectiveInfo> dirMap = new HashMap<String, DirectiveInfo>();
	private final OperandParser operandParser;
	static {
		dirMap.put("aorg", new DirectiveInfo(1, AorgDirective.class));
		dirMap.put("equ", new DirectiveInfo(1, EquDirective.class));
		dirMap.put("dw", new DirectiveInfo(-1, DefineWordDirective.class));
		dirMap.put("data", new DirectiveInfo(-1, DefineWordDirective.class));
		dirMap.put("db", new DirectiveInfo(-1, DefineByteDirective.class));
		dirMap.put("byte", new DirectiveInfo(-1, DefineByteDirective.class));
		dirMap.put("text", new DirectiveInfo(-1, DefineByteDirective.class));
		dirMap.put("bss", new DirectiveInfo(1, BssDirective.class));
		dirMap.put("even", new DirectiveInfo(0, EvenDirective.class));
		dirMap.put("consttable", new DirectiveInfo(0, ConstPoolDirective.class));
		
		dirMap.put("def", new DirectiveInfo(-1, IgnoreDirective.class));
		dirMap.put("idt", new DirectiveInfo(-1, IgnoreDirective.class));
		dirMap.put("titl", new DirectiveInfo(-1, IgnoreDirective.class));
		dirMap.put("end", new DirectiveInfo(0, IgnoreDirective.class));
		dirMap.put("rorg", new DirectiveInfo(0, IgnoreDirective.class));
		
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
		
		DirectiveInfo info = dirMap.get(dir);
		if (info == null)
			return null;
		
		int cnt = info.argNum;
		List<IOperand> ops = new ArrayList<IOperand>();
		while (cnt != 0) {
			try {
				IOperand op = operandParser.parse(tokenizer);
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
					if (cnt > 0) {
						throw new ParseException("Expected ','");
					}
					// assume comment
					tokenizer.skipToEOF();
					break;
				}
			} catch (ParseException e) {
				if (e.getMessage().contains("Unexpected end of line") && cnt < 0) {
					// assume comment
					tokenizer.skipToEOF();
					break;
				} else {
					throw e;
				}
			}
		}
		if (tokenizer.nextToken() != AssemblerTokenizer.EOF) {
			if (cnt < 0) {
				throw new ParseException("Trailing garbage: " + tokenizer.currentToken());
			} else {
				// assume comment
				tokenizer.skipToEOF();
			}
		}
		
		try {
			Constructor<? extends Directive> constructor = info.klass.getConstructor(
					List.class);
			Directive directive = constructor.newInstance(ops);
			return new IInstruction[] { directive };
		} catch (Exception e) {
			throw (IllegalStateException) new IllegalStateException().initCause(e);
		}
	}

}
