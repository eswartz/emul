/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.ejs.gui.common.FontUtils;

import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class CpuInstructionTableComposite extends CpuInstructionComposite {
	private TableViewer instTableViewer;
	private InstContentProvider instContentProvider;
	private Font smallerFont;
	private Font tableFont;

	public CpuInstructionTableComposite(Composite parent, int style, IMachine machine_) {
		super(parent, style, machine_);
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		///
		instTableViewer = new TableViewer(this, SWT.V_SCROLL + SWT.BORDER + SWT.VIRTUAL + SWT.NO_FOCUS + SWT.FULL_SELECTION);
		instTableViewer.setUseHashlookup(true);
		instContentProvider = new InstContentProvider();
		instTableViewer.setContentProvider(instContentProvider);
		instTableViewer.setLabelProvider(new InstLabelProvider(
				//getDisplay().getSystemColor(SWT.COLOR_RED)
				));
		final Table table = instTableViewer.getTable();
		GridDataFactory.fillDefaults().grab(true, true).span(1, 1).applyTo(table);
		
		FontDescriptor fontDescriptor = FontUtils.getFontDescriptor(JFaceResources.getTextFont());
		//fontDescriptor = fontDescriptor.increaseHeight(-2);
		tableFont = fontDescriptor.createFont(getDisplay());
		FontDescriptor smallerFontDescriptor = fontDescriptor.increaseHeight(-2);
		smallerFont = smallerFontDescriptor.createFont(getDisplay());
		
		table.setFont(smallerFont);
		
		GC gc = new GC(getDisplay());
		gc.setFont(smallerFont);
		int charWidth = gc.stringExtent("M").x;
		gc.dispose();

		TableColumn column;
		String[] props = new String[6];
		
		props[0] = "Addr";
		column = new TableColumn(table, SWT.LEFT);
		column.setText(props[0]);
		column.setMoveable(true);
		column.setWidth(charWidth * 20);
		
		props[1] = "Inst";
		column = new TableColumn(table, SWT.LEFT);
		column.setText(props[1]);
		column.setMoveable(true);
		column.setWidth(charWidth * 60);
		
		props[2] = "Op1";
		column = new TableColumn(table, SWT.LEFT);
		column.setText(props[2]);
		column.setMoveable(true);
		column.setWidth(charWidth * 20);

		props[3] = "Op2";
		column = new TableColumn(table, SWT.LEFT);
		column.setText(props[3]);
		column.setMoveable(true);
		column.setWidth(charWidth * 20);

		props[4] = "Op3";
		column = new TableColumn(table, SWT.LEFT);
		column.setText(props[4]);
		column.setMoveable(true);
		column.setWidth(charWidth * 20);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		instTableViewer.setColumnProperties(props);
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#setupEvents()
	 */
	@Override
	public void setupEvents() {
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				tableFont.dispose();
				smallerFont.dispose();

			}
		});
	}
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#go()
	 */
	@Override
	public void go() {
		instTableViewer.setInput(new Object());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#executed(v9t9.common.cpu.InstructionWorkBlock, v9t9.common.cpu.InstructionWorkBlock)
	 */
	@Override
	public void executed(final InstructionWorkBlock before,
			final InstructionWorkBlock after_) {
		InstructionWorkBlock after = after_.copy();
        
		changed = true;
        final InstRow row = new InstRow(before, after);
        if (partialInst != null) {
        	instContentProvider.removeInstRow(partialInst);
        	partialInst = null;
        }
        instContentProvider.addInstRow(row);
    	Display.getDefault().syncExec(new Runnable() {
			public void run() {
				if (instTableViewer.getTable().isDisposed())
					return;
				instTableViewer.refresh(row, true);
				//instTableViewer.getTable().setSelection(new int[] { count - 1 });
			}
		});		
	}
	

	volatile private Runnable refreshRunnable;
	private boolean changed;
	protected InstRow partialInst; 
	public void refresh() {
		if (isDisposed())
			return;
		if (changed && refreshRunnable == null) {
			changed = false;
			refreshRunnable = new Runnable() {
				public void run() {
					if (!instTableViewer.getTable().isDisposed()) {
						ICpuState state = machine.getCpu().getState();
						RawInstruction inst = machine.getInstructionFactory().decodeInstruction(
								state.getPC(), machine.getConsole());
						
						//InstructionWorkBlock before = state.createInstructionWorkBlock();
						InstructionWorkBlock before = new InstructionWorkBlock(state);
						before.inst = inst;
						before.pc = (short) (state.getPC() + inst.getSize());
						
						InstRow row = new InstRow(before, before);
						if (partialInst != null) {
							instContentProvider.removeInstRow(partialInst);
							instContentProvider.addInstRow(row);
						} else {
							instContentProvider.addInstRow(row);
						}
						instTableViewer.refresh(row, true);
						partialInst = row;
						//refreshTable();
						
						int count = instContentProvider.getCount();
						instTableViewer.setItemCount(count);
						instTableViewer.getTable().setSelection(new int[] { count - 1 });
					}
					refreshRunnable = null;
				}
			};
			getDisplay().syncExec(refreshRunnable);
		}
	}
	
	protected void resizeTable() {
		for (TableColumn c : instTableViewer.getTable().getColumns()) {
			c.pack();
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#clear()
	 */
	@Override
	public void clear() {
		instContentProvider.clear();
		partialInst = null;
		changed = true;
		
	}
}
