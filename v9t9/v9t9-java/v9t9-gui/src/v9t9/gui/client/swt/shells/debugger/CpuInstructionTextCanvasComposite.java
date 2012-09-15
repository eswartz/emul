/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class CpuInstructionTextCanvasComposite extends CpuInstructionComposite {

	private TextCanvas text;
	private Runnable refreshTask;

	public CpuInstructionTextCanvasComposite(Composite parent, int style, IMachine machine) {
		super(parent, style, machine);
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		text = new TextCanvas(this, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(text);
		
		start();
		
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#setupEvents()
	 */
	@Override
	public void setupEvents() {
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				machine.getFastMachineTimer().cancelTask(refreshTask);
			}
		});
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#go()
	 */
	@Override
	public void go() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#refresh()
	 */
	@Override
	public void flush() {
		synchronized (text) { 
			text.clear();
			int visible = text.getRows();
			List<InstRow> subList = instHistory.subList(Math.max(0, instHistory.size() - visible), instHistory.size());
			for (InstRow row : subList) {
				text.addLine(row.getInst());
			}
		}
		text.redraw();
	}
	

}
