package v9t9.emulator.runtime.interpreter;

import v9t9.emulator.runtime.cpu.Cpu;
import v9t9.engine.cpu.Instruction;

public interface Interpreter {

	/**
	 * Execute an instruction: general entry point
	 * @param cpu
	 * @param op_x if not-null, execute the instruction from an X instruction
	 */
	void execute(Short op_x);

	/**
	 * This version is called when you know nothing needs to monitor instructions
	 * @param cpu
	 * @param op_x
	 */
	void executeFast(Short op_x);

	Instruction getInstruction(Cpu cpu);

}