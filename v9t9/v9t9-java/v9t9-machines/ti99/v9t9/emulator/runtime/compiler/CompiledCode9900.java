/**
 * 
 */
package v9t9.emulator.runtime.compiler;

import v9t9.emulator.hardware.TI99Machine;
import v9t9.emulator.hardware.memory.mmio.GplMmio;
import v9t9.emulator.hardware.memory.mmio.VdpMmio;
import v9t9.emulator.runtime.cpu.Executor;

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
	public CompiledCode9900(Executor exec) {
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
