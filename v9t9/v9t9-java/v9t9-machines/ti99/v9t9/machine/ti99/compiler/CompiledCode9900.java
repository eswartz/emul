/**
 * 
 */
package v9t9.machine.ti99.compiler;

import v9t9.common.cpu.IExecutor;
import v9t9.engine.compiler.CompiledCode;
import v9t9.engine.memory.GplMmio;
import v9t9.engine.memory.VdpMmio;
import v9t9.machine.ti99.machine.TI99Machine;

/**
 * @author ejs
 *
 */
public class CompiledCode9900 extends CompiledCode {

    // used for debugging
    protected VdpMmio vdpMmio;
    protected GplMmio gplMmio;
    
	/**
	 * 
	 */
	public CompiledCode9900() {
	}

	/**
	 * @param exec
	 */
	public CompiledCode9900(IExecutor exec) {
		super(exec);
		
		TI99Machine ti99Machine = (TI99Machine) exec.getCpu().getMachine();
		this.cru = ti99Machine.getCruManager();
		this.vdpMmio = ti99Machine.getVdpMmio();
		this.gplMmio = ti99Machine.getGplMmio();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.compiler.CompiledCode#run()
	 */
	@Override
	public boolean run() {
		// TODO Auto-generated method stub
		return false;
	}

}
