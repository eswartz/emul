package v9t9.emulator.runtime.compiler;

import v9t9.emulator.runtime.cpu.Cpu;

public class NullCompilerStrategy implements ICompilerStrategy {

	@Override
	public ICompiledCode getCompiledCode(Cpu cpu) {
		return null;
	}

}
