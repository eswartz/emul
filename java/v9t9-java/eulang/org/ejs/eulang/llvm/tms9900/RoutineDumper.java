/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.Collection;

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
		boolean any = false;
		
		Locals locals = routine.getLocals();
		Collection<? extends ILocal> values;
		values = locals.getArgumentLocals();
		if (!values.isEmpty()) {
			any = true;
			System.out.println("Arguments:");
			for (ILocal local : values) {
				dumpLocal(local);
			}
		}
		values = locals.getRegLocals().values();
		if (!values.isEmpty()) {
			any = true;
			System.out.println("Registers:");
			for (ILocal local : values) {
				dumpLocal(local);
			}
		}
		values = locals.getStackLocals().values();
		if (!values.isEmpty()) {
			any = true;
			System.out.println("Stack:");
			for (ILocal local : values) {
				dumpLocal(local);
			}
		}
		if (any)
			System.out.println();
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
		int init = local.getDefs().nextSetBit(0);
		if (init < 0) {
			System.out.println("; no init"); return; 
		}
		else if (init == 0)
			System.out.print("; argument");
		else
			System.out.print("; initialized: " + init);
		if (local.isOutgoing())
			System.out.print("; return");
		System.out.print("; uses: " + local.getUses());
		System.out.println("; defs: " + local.getDefs());
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
