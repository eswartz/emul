/*
  TI99InlineWord.java

  (c) 2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.ti99;

import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.machine.ti99.asm.RawInstructionFactory9900;
import v9t9.tools.asm.LLInstruction;
import v9t9.tools.asm.inst9900.AsmInstructionFactory9900;
import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.DictEntry;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;
import v9t9.tools.forthcomp.words.INativeCodeWord;
import v9t9.tools.forthcomp.words.IPrimitiveWord;
import v9t9.tools.forthcomp.words.TargetContext;
import v9t9.tools.forthcomp.words.TargetWord;

/**
 * @author ejs
 *
 */
public class TI99PrimitiveWord extends TargetWord implements IPrimitiveWord {

	private final LLInstruction[] insts;
	private AsmInstructionFactory9900 asmInstrFactory = new AsmInstructionFactory9900();
	private boolean inline;

	/**
	 * @param entry
	 */
	public TI99PrimitiveWord(DictEntry entry, 
			LLInstruction[] insts_, boolean inline_) {
		super(entry);
		this.insts = insts_;
		this.inline = inline_;
		
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				if (targetContext.isNativeDefinition()) {
					if (inline) {
						for (LLInstruction inst : insts) {
							((TI99TargetContext) targetContext).compileInstr(inst);
						}
					} else {
						targetContext.compileCall(TI99PrimitiveWord.this);
					}
				} else {
					targetContext.buildCall(TI99PrimitiveWord.this);
				}
				getEntry().use();
			}
		});
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.IPrimitiveWord#getSize()
	 */
	@Override
	public int getPrimitiveSize() {
		int total = 0;
		for (LLInstruction inst : insts) {
			RawInstruction rawInstr;
			try {
				rawInstr = asmInstrFactory.createRawInstruction(inst);
				total += rawInstr.getSize();
			} catch (ResolveException e) {
				e.printStackTrace();
				total += 2;
			}
		}
		return total;
	}

}
