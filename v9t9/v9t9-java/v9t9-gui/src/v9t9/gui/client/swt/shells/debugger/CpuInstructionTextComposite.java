/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 * @deprecated
 */
public class CpuInstructionTextComposite extends CpuInstructionComposite {

	private Text text;

	public CpuInstructionTextComposite(Composite parent, int style, IMachine machine) {
		super(parent, style, machine);
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		text = new Text(this, SWT.WRAP | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(text);
		
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#setupEvents()
	 */
	@Override
	public void setupEvents() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#go()
	 */
	@Override
	public void go() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#executed(v9t9.common.cpu.InstructionWorkBlock, v9t9.common.cpu.InstructionWorkBlock)
	 */
	@Override
	public void executed(InstructionWorkBlock before,
			InstructionWorkBlock after_) {
		
		InstructionWorkBlock after = after_.copy();
        
        final InstRow row = new InstRow(before, after);
//        if (partialInst != null) {
//        	instContentProvider.removeInstRow(partialInst);
//        	partialInst = null;
//        }
    	

		getDisplay().syncExec(new Runnable() {
			public void run() {
				if (!text.isDisposed()) {
					text.append(row.getInst());
					text.append("\n");
				}
			}
		});
		
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#refresh()
	 */
	@Override
	public void refresh() {
		ICpuState state = machine.getCpu().getState();
		RawInstruction inst = machine.getInstructionFactory().decodeInstruction(
				state.getPC(), machine.getConsole());
		
		InstructionWorkBlock before = new InstructionWorkBlock(state);
		before.inst = inst;
		before.pc = (short) (state.getPC() + inst.getSize());
		
//		if (partialInst != null) {
//			instContentProvider.removeInstRow(partialInst);
//			instContentProvider.addInstRow(row);
//		} else {
//			instContentProvider.addInstRow(row);
//		}
		//refreshTable();
		
		getDisplay().syncExec(new Runnable() {
			public void run() {
				if (!text.isDisposed()) {
//					text.append(row.getInst());
//					text.append("\n");
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#clear()
	 */
	@Override
	public void clear() {
		text.setText("");
	}

}
