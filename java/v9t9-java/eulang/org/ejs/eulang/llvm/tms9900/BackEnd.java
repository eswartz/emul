/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.ArrayList;
import java.util.List;

import org.ejs.eulang.ITarget;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.directives.LLDefineDirective;

import v9t9.tools.asm.assembler.SymbolTable;

/**
 * @author ejs
 *
 */
public class BackEnd {

	private final TypeEngine typeEngine;
	private final ITarget target;
	private SymbolTable table;

	private List<Routine> routines;

	public BackEnd(TypeEngine typeEngine, ITarget target) {
		this.typeEngine = typeEngine;
		this.target = target;
		this.table = new SymbolTable();
		this.routines = new ArrayList<Routine>();
	}

	/**
	 * @return
	 */
	public List<Routine> getRoutines() {
		return routines;
	}

	/**
	 * @param dir
	 */
	public void generateDirective(LLBaseDirective dir) {
		if (dir instanceof LLDefineDirective) {
			Routine routine = generateDefine((LLDefineDirective) dir);
			routines.add(routine);
		}
	}

	
	public Routine generateDefine(LLDefineDirective def) {
		def.accept(new RenumberInstructionsVisitor());
		
		CodeGenVisitor cg = new CodeGenVisitor();
		def.accept(cg);
		
		return cg.getRoutine();
	}


}
