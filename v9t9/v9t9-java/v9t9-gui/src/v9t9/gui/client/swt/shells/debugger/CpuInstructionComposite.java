/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import org.eclipse.swt.widgets.Composite;

import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.IExecutor;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public abstract class CpuInstructionComposite extends Composite {

	protected IMachine machine;
	protected IProperty pauseMachine;
	protected IProperty debugging;
	protected IProperty singleStep;

	public CpuInstructionComposite(Composite parent, int style, IMachine machine) {
		super(parent, style);
		this.machine = machine;
		
		pauseMachine = Settings.get(machine, IMachine.settingPauseMachine);
		debugging = Settings.get(machine, ICpu.settingDebugging);
		singleStep = Settings.get(machine, IExecutor.settingSingleStep);

	}
	
	abstract public void setupEvents();

	/**
	 * 
	 */
	abstract public void go();

	/**
	 * @param before
	 * @param after_
	 */
	abstract public void executed(InstructionWorkBlock before,
			InstructionWorkBlock after_);

	/**
	 * 
	 */
	abstract public void refresh();

	/**
	 * 
	 */
	abstract public void clear();
}
