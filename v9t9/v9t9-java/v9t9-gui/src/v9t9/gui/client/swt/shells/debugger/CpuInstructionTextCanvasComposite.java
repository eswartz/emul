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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class CpuInstructionTextCanvasComposite extends CpuInstructionComposite {

	private TextCanvas text;
	private Runnable refreshTask;
	
	public CpuInstructionTextCanvasComposite(Composite parent, int style, IMachine machine) {
		super(parent, style | SWT.V_SCROLL, machine);
		
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
		addListener(SWT.Resize, new Listener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			@Override
			public void handleEvent(Event event) {
				getVerticalBar().setIncrement(text.getRows());
			}
		});
		getVerticalBar().addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				redrawLines();
			}
		});
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
		synchronized (instHistory) {
			getVerticalBar().setMaximum(instHistory.size());
			getVerticalBar().setSelection(instHistory.size() - text.getRows() + 1);
			redrawLines();
		}
	}

	/**
	 * 
	 */
	private void redrawLines() {
		int rowIndex = getVerticalBar().getSelection();
		synchronized (text) {
			synchronized (instHistory) {
				text.clear();
				int visible = text.getRows();
				int start = Math.min(rowIndex, instHistory.size());
				int end = Math.min(rowIndex + visible, instHistory.size());
				List<InstRow> subList = instHistory.subList(start, end);
				for (InstRow row : subList) {
					text.addLine(row.getInst());
				}
			}
		}
		text.redraw();
		
	}
	

}
