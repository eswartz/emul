/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuState;
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

	private static final int MAX_INST_HISTORY = 250000;
	protected IMachine machine;
	protected IProperty pauseMachine;
	protected IProperty debugging;
	protected IProperty singleStep;
	private Runnable refreshTask;
	private boolean isDirty;
	private LinkedList<InstRow> instHistory = new LinkedList<InstRow>();
	
	public CpuInstructionComposite(Composite parent, int style, IMachine machine) {
		super(parent, style);
		this.machine = machine;
		
		pauseMachine = Settings.get(machine, IMachine.settingPauseMachine);
		debugging = Settings.get(machine, ICpu.settingDebugging);
		singleStep = Settings.get(machine, IExecutor.settingSingleStep);

	}
	
	protected void start() {

		refreshTask = new Runnable() {
			
			@Override
			public void run() {
				if (isDisposed()) 
					return;

				getDisplay().asyncExec(new Runnable() {
					public void run() {
						synchronized (CpuInstructionComposite.this) {
							if (!isDisposed() && isDirty) {
								flush(instHistory);
								isDirty = false;
							}
						}
					}
				});
				
			}
		};
		machine.getFastMachineTimer().scheduleTask(refreshTask, 20);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				machine.getFastMachineTimer().cancelTask(refreshTask);
			}
		});
	}
	
	abstract public void flush(LinkedList<InstRow> instHistory);
	
	abstract public void setupEvents();

	/**
	 * 
	 */
	abstract public void go();

	/**
	 * @param before
	 * @param after_
	 */
	public void executed(InstructionWorkBlock before,
			InstructionWorkBlock after_) {

		InstructionWorkBlock after = after_.copy();
        
        InstRow row = new InstRow(before, after);
        
        synchronized (this) {
        	if (instHistory.size() >= MAX_INST_HISTORY) {
        		instHistory.subList(0, MAX_INST_HISTORY / 2).clear();
        	}
	        if (instHistory.size() > 0 && instHistory.peekLast().isGeneric()) {
	        	instHistory.removeLast();
	        }
	        instHistory.add(row);
	        
	        isDirty = true;
        }
	}

	/**
	 * 
	 */
	public void refresh() {
		ICpuState state = machine.getCpu().getState();
		RawInstruction inst = machine.getInstructionFactory().decodeInstruction(
				state.getPC(), machine.getConsole());
		
		InstructionWorkBlock before = new InstructionWorkBlock(state);
		before.inst = inst;
		before.pc = (short) (state.getPC() + inst.getSize());
		
		InstRow row = new InstRow(before);
		synchronized (this) {
			if (instHistory.size() == 0 || !instHistory.peekLast().isGeneric()) {
				instHistory.add(row);
				isDirty = true;
			}
		}
	}

	/**
	 * 
	 */
	public void clear() {
		synchronized (this) {
			instHistory.clear();
		}
		flush(instHistory);
	}
}
