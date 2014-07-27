/*
  DescrDirective.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.directive;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.IAssembler;
import v9t9.tools.asm.SourceRef;

/**
 * @author Ed
 *
 */
public class DescrDirective extends Directive {

	private final SourceRef ref;

	public DescrDirective(SourceRef ref) {
		this.ref = ref;
	}

	@Override
	public String toString() {
		return ref.filename + ":" + ref.line;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IInstruction#resolve(v9t9.tools.asm.Assembler, v9t9.engine.cpu.IInstruction)
	 */
	public IInstruction[] resolve(IAssembler assembler, IInstruction previous, boolean finalPass)
			throws ResolveException {
		return new IInstruction[] { this };
	}

	public String getFilename() {
		return ref.filename;
	}
	public int getLine() {
		return ref.lineno;
	}
	public String getContent() {
		return ref.line;
	}

	/**
	 * @return
	 */
	public SourceRef getRef() {
		return ref;
	}


}
