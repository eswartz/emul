/*
  RegisterViewer.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.debugger;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.ejs.gui.common.FontUtils;

import ejs.base.utils.HexUtils;

import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class RegisterViewer extends Composite {

	class RegisterLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

		/**
		 * @param regProvider
		 */
		public RegisterLabelProvider() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		@Override
		public String getColumnText(Object element, int columnIndex) {
			IRegister reg = (IRegister)element;
			if (columnIndex == 0)
				return reg.getInfo().id;

			int val = reg.getValue();
			int size = reg.getInfo().size;
			return (size == 1) ? HexUtils.toHex2(val) : HexUtils.toHex4(val);
		}
		
	}
	private TableViewer[] regViewers;
	private Font tableFont;
	private Font smallerFont;
	private Runnable updateTask;

	/**
	 * @param parent
	 * @param machine 
	 * @param style
	 */
	public RegisterViewer(Composite parent, IMachine machine, final IRegisterProvider regProvider, int perColumn) {
		super(parent, SWT.NONE);
		
		setLayout(new GridLayout());
		
		Label label = new Label(this, SWT.NONE);
		label.setText(regProvider.getLabel());
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(label);
		

		FontDescriptor fontDescriptor = FontUtils.getFontDescriptor(JFaceResources.getTextFont());
		tableFont = fontDescriptor.createFont(getDisplay());
		FontDescriptor smallerFontDescriptor = fontDescriptor.increaseHeight(-2);
		smallerFont = smallerFontDescriptor.createFont(getDisplay());
		
		ScrolledComposite tableScroller = new ScrolledComposite(this, SWT.H_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 1).applyTo(tableScroller);
		
		Composite tables = new Composite(tableScroller, SWT.NONE);
		tables.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).span(1, 1).applyTo(tables);
		
		tableScroller.setContent(tables);
		tableScroller.setExpandHorizontal(true);
		tableScroller.setExpandVertical(true);
		
		int cnt = regProvider.getRegisterCount();
		if (cnt % perColumn < 3)
			perColumn++;
		
		int numColumns = (cnt + perColumn - 1) / perColumn;
		GridLayoutFactory.swtDefaults().numColumns(numColumns).applyTo(tables);
		
		int startReg = 0;
		regViewers = new TableViewer[numColumns];
		
		int idx = 0;
		while (startReg < cnt) {
			int endReg = Math.min(startReg + perColumn, cnt);

			regViewers[idx] = createTable(tables, regProvider, startReg, endReg - startReg);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(regViewers[idx].getTable());
			
			startReg = endReg;
			idx++;
		}
		
		tableScroller.setMinSize(tables.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				tableFont.dispose();
				smallerFont.dispose();
			}
			
		});
	}

	/**
	 * @param tables
	 * @param regProvider
	 * @param startReg
	 * @param i
	 * @return
	 */
	private TableViewer createTable(Composite tables,
			final IRegisterProvider regProvider, final int startReg, final int count) {

		///
		final TableViewer regViewer = new TableViewer(tables, SWT.BORDER + SWT.VIRTUAL + SWT.NO_FOCUS + SWT.FULL_SELECTION);
		regViewer.setContentProvider(new IStructuredContentProvider() {
			
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			@Override
			public Object[] getElements(Object inputElement) {
				return regProvider.getRegisters(startReg, count);
			}
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				viewer.refresh();
			}
			
			@Override
			public void dispose() {
				
			}
		});
		
		regViewer.setLabelProvider(new RegisterLabelProvider());
		
		final Table table = regViewer.getTable();
		GridDataFactory.fillDefaults().grab(true, true).span(1, 1).applyTo(table);
		
		table.setFont(smallerFont);
		
		GC gc = new GC(getDisplay());
		gc.setFont(smallerFont);
		int charWidth = gc.stringExtent("M").x;
		gc.dispose();

		String[] props = new String[6];
		
		props[0] = "Name";
		final TableColumn nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setText(props[0]);
		nameColumn.setWidth(charWidth * 8 + 4);
		
		props[1] = "Value";
		final TableColumn valColumn = new TableColumn(table, SWT.LEFT);
		valColumn.setText(props[1]);
		valColumn.setMoveable(true);
		valColumn.setWidth(charWidth * 6);

		//table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		regViewer.setColumnProperties(props);

		CellEditor[] editors = new CellEditor[2];
		editors[0] = null;
		editors[1] = new TextCellEditor(table);
		
		regViewer.setColumnProperties(props);
		regViewer.setCellModifier(new RegisterCellModifier(regViewer, regProvider.getNumDigits()));
		regViewer.setCellEditors(editors);
		
		regViewer.setInput(new Object());

		table.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseHover(MouseEvent e) {
				TableItem item = table.getItem(new Point(e.x, e.y));
				if (item != null) {
					for (int i = 0; i <= 16; i++) {
						Rectangle bounds = item.getTextBounds(i);
						if (e.x >= bounds.x && e.x < bounds.x + bounds.width) {
							IRegister reg = (IRegister) item.getData();
							if (reg == null) {
								table.setToolTipText(null);
							} else {
								String tooltip = reg.getTooltip();
								String id = reg.getInfo().id;
								String descr = reg.getInfo().description;
								table.setToolTipText((descr == null ? id : descr) +
										(tooltip != null && tooltip.length() > 0 ? ": " + tooltip : "")
										);
							}
							return;
						}
					}
				}
				setToolTipText(null);
			}
		});

		// oops, key listeners mess up everything else
		/*
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == '\r' && !regViewer.isCellEditorActive()
						&& !regViewer.getSelection().isEmpty()) {
					regViewer.editElement(
							((IStructuredSelection) regViewer.getSelection()).getFirstElement(), 
							1);
					e.doit = false;
				}
			}
		});
		*/
		
		if (false) Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				nameColumn.pack();
				valColumn.pack();
			}
		});
		
		return regViewer;
	}

	public synchronized void update() {
		if (updateTask == null) {
			updateTask = new Runnable() {
				public void run() {
					for (TableViewer v : regViewers) {
						if (!v.getTable().isDisposed())
							v.refresh();
					}
					updateTask = null;
				}
			};
			Display.getDefault().asyncExec(updateTask);
		}
	}
	
}
