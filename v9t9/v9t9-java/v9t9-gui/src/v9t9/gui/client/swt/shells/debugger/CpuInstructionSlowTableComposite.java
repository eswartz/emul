/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.ejs.gui.common.FontUtils;

import v9t9.common.machine.IMachine;

/**
 * Hmm, this is very slow, with the virtual table manager in the background...
 * @author ejs
 * @deprecated
 */
public class CpuInstructionSlowTableComposite extends CpuInstructionComposite {
	private TableViewer instTableViewer;
	private Font smallerFont;
	private Font tableFont;
	private List<InstRow> tableInsts = new ArrayList<InstRow>();

	public CpuInstructionSlowTableComposite(Composite parent, int style, IMachine machine_) {
		super(parent, style, machine_);
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		///
		instTableViewer = new TableViewer(this, SWT.V_SCROLL + SWT.BORDER + SWT.VIRTUAL + SWT.NO_FOCUS + SWT.FULL_SELECTION);
		instTableViewer.setUseHashlookup(true);
		instTableViewer.setContentProvider(new ArrayContentProvider());
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
		instTableViewer.setInput(tableInsts);
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#clear()
	 */
	@Override
	public void clear() {
		super.clear();
		tableInsts.clear();
		//instContentProvider.clear();
	}
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#flush(java.util.LinkedList)
	 * 
	 * called with instHistory locked
	 */
	@Override
	public void flush() {
		Table table = instTableViewer.getTable();
		table.setRedraw(false);
		int count;
		int instIdx = -1;
		if (tableInsts.size() > instHistory.size()) {
			// was flushed
			instTableViewer.remove(tableInsts.toArray());
			instTableViewer.setItemCount(0);
		}
		for (ListIterator<InstRow> iter = tableInsts.listIterator(tableInsts.size()); iter.hasPrevious(); ) {
			// remove obsolete entries
			InstRow row = iter.previous();
			instIdx = instHistory.lastIndexOf(row);
			if (instIdx < 0) {
				instTableViewer.remove(row);
				iter.remove();
			} else {
				break;
			}
		}
		
		// add new entries
		List<InstRow> newList = instHistory.subList(instIdx+1, instHistory.size());
		instTableViewer.add(newList.toArray());
		tableInsts.addAll(newList);
		
		count = tableInsts.size();
		//instTableViewer.setItemCount(count);
		if (count >= 0) {
			table.setSelection(new int[] { count - 1 });
		}
		table.setRedraw(true);
	}
	
	protected void resizeTable() {
		for (TableColumn c : instTableViewer.getTable().getColumns()) {
			c.pack();
		}
	}

}
