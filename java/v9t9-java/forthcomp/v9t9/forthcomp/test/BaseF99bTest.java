/**
 * 
 */
package v9t9.forthcomp.test;

import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;


import v9t9.emulator.hardware.F99bMachine;
import v9t9.emulator.hardware.F99bMachineModel;
import v9t9.emulator.runtime.cpu.CpuF99b;
import v9t9.emulator.runtime.cpu.CpuStateF99b;
import v9t9.emulator.runtime.cpu.DumpFullReporterF99b;
import v9t9.emulator.runtime.interpreter.Interpreter;
import v9t9.emulator.runtime.interpreter.InterpreterF99b;
import v9t9.forthcomp.F99bTargetContext;
import v9t9.forthcomp.words.TargetContext;

/**
 * @author ejs
 *
 */
public class BaseF99bTest extends BaseForthCompTest {

	final protected static String stockDictDefs = 
		"Variable dp\n"+
		"here dp !\n"+
		": here dp @ ; \n"+
		": , here ! 2 dp +! ; \n"+
		": c, here c! 1 dp +! ; \n"+
		"";
	
	final protected static String compileLiteral = 
		": literal ( n -- ) dup -8 >= over 8 < and  if\n" +
		" 	$f and $20 or c,  else\n" +
		"dup -128 >= over 128 < and  if\n" +
		" 	$78 c, c,\n" +
		"else\n" +
		"	$79 c, ,\n" +
		"then then\n"+
		"; immediate target-only \n";
	
	final protected static String compileMeta =
		"Variable state\n"+
		"Variable lastxt\n"+
		": create ; target-only\n"+
		": : ; target-only\n"+
		": ; $70 c, ; immediate target-only \n"+

		": compile, 1 ursh $8000 OR postpone LITERAL ;\n"+
		"";
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.test.BaseForthCompTest#initCpu()
	 */
	@Override
	protected void initCpu() {
		f99bMachineModel = new F99bMachineModel();
		f99Machine = (F99bMachine) f99bMachineModel.createMachine();
		cpu = (CpuF99b) f99Machine.getCpu();
	
		DumpFullReporterF99b dump = new DumpFullReporterF99b((CpuF99b) cpu,  new PrintWriter(System.out));
		f99Machine.getExecutor().addInstructionListener(dump);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.test.BaseForthCompTest#createInterpreter()
	 */
	@Override
	protected Interpreter createInterpreter() {
		return new InterpreterF99b(f99Machine);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.test.BaseForthCompTest#createTargetContext()
	 */
	@Override
	protected TargetContext createTargetContext() {
		F99bTargetContext targCtx = new F99bTargetContext(4096);
		targCtx.setBaseDP(0x400);
		return targCtx;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.test.BaseForthCompTest#doInterpret(int)
	 */
	@Override
	protected void doInterpret(int pc) {
		((CpuF99b) cpu).rpush((short) 0);
		cpu.setPC((short) pc);
		((InterpreterF99b) interp).setShowSymbol();
		while (cpu.getPC() != 0)
			((InterpreterF99b) interp).execute();
		
		assertTrue(((CpuStateF99b) cpu.getState()).getSP() <= ((CpuStateF99b) cpu.getState()).getBaseSP());
		assertTrue(((CpuStateF99b) cpu.getState()).getRP() <= ((CpuStateF99b) cpu.getState()).getBaseRP());
		
	}
	

}
