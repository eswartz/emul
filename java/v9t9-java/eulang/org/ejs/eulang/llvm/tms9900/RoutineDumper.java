/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.Collection;

import org.ejs.coffee.core.utils.Pair;

/**
 * @author ejs
 *
 */
public class RoutineDumper extends CodeVisitor {

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ICodeVisitor#getWalk()
	 */
	@Override
	public Walk getWalk() {
		return Walk.LINEAR;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.CodeVisitor#enterCode(org.ejs.eulang.llvm.tms9900.Routine)
	 */
	@Override
	public boolean enterRoutine(Routine routine) {
		System.out.println("Routine: " + routine.getName());
		
		Locals locals = routine.getLocals();
		Collection<? extends ILocal> values;
		values = locals.getArgumentLocals();
		if (!values.isEmpty()) {
			System.out.println("Arguments:");
			for (ILocal local : values) {
				dumpLocal(local);
			}
		}
		values = locals.getRegLocals().values();
		if (!values.isEmpty()) {
			System.out.println("Registers:");
			for (ILocal local : values) {
				dumpLocal(local);
			}
		}
		values = locals.getStackLocals().values();
		if (!values.isEmpty()) {
			System.out.println("Stack:");
			for (ILocal local : values) {
				dumpLocal(local);
			}
		}
		return super.enterRoutine(routine);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.CodeVisitor#exitRoutine(org.ejs.eulang.llvm.tms9900.Routine)
	 */
	@Override
	public void exitRoutine(Routine directive) {
		System.out.println();
		super.exitRoutine(directive);
	}

	private void dumpLocal(ILocal local) {
		System.out.print("\t" + local + " ");
		Pair<Block, AsmInstruction> init = local.getInit();
		if (init == null)
			System.out.println("; no init");
		else if (init.first == null)
			System.out.println("; argument");
		else
			System.out.println("; initialized: " + init.first.getLabel() + " at " + init.second.getNumber());
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.CodeVisitor#enterBlock(org.ejs.eulang.llvm.tms9900.Block)
	 */
	@Override
	public boolean enterBlock(Block block) {
		System.out.print("Block: " + block.getLabel() + "\t\tsucc: ");
		for (Block succ : block.succ()) {
			System.out.print(succ.getLabel() + " ");
		}
		System.out.print("\tpred: ");
		for (Block pred : block.pred()) {
			System.out.print(pred.getLabel() + " ");
		}
		System.out.println();
		return super.enterBlock(block);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.CodeVisitor#enterInstr(org.ejs.eulang.llvm.tms9900.Block, org.ejs.eulang.llvm.tms9900.AsmInstruction)
	 */
	@Override
	public boolean enterInstr(Block block, AsmInstruction instr) {
		System.out.println(instr);
		return false;
	}
}
