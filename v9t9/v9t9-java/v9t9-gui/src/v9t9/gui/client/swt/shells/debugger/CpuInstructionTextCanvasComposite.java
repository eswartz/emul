/*
  CpuInstructionTextCanvasComposite.java

  (c) 2012-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.debugger;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.ejs.gui.common.FontUtils;
import org.ejs.gui.common.SwtUtils;

import v9t9.common.cpu.BreakpointManager;
import v9t9.common.cpu.IBreakpoint;
import v9t9.common.cpu.IBreakpointListener;
import v9t9.common.cpu.IInstructionEffectLabelProvider.Column;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class CpuInstructionTextCanvasComposite extends CpuInstructionComposite implements IBreakpointListener {
	
	private StyledText text;
	private int numRows;
	
	private Runnable refreshTask;
	private TextStyle baseTextStyle;
	private TextStyle bg1Style;
	private TextStyle bg2Style;
	private TextStyle bpStyle;
	private InstLabelProvider instLabelProvider;
	private Font smallerFont;
	private int topRow;
	
	public CpuInstructionTextCanvasComposite(Composite parent, int style, IMachine machine) {
		super(parent, style | SWT.V_SCROLL, machine);
		this.instLabelProvider = new InstLabelProvider(machine.getCpu(),
				machine.getCpu().createInstructionEffectLabelProvider());
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		text = new StyledText(this, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(text);

		
		FontDescriptor fontDescriptor = FontUtils.getFontDescriptor(JFaceResources.getTextFont());
		FontDescriptor smallerFontDescriptor = fontDescriptor.increaseHeight(-2);
		smallerFont = smallerFontDescriptor.createFont(getDisplay());
		
		baseTextStyle = new TextStyle(smallerFont, null, null);
		bg1Style = new TextStyle(baseTextStyle);
		bg1Style.background = getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		bg2Style = new TextStyle(baseTextStyle);
		bg2Style.background = getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);

		bpStyle = new TextStyle(baseTextStyle);
		bpStyle.background = getDisplay().getSystemColor(SWT.COLOR_RED);

		text.addLineStyleListener(new LineStyleListener() {
			
			@Override
			public void lineGetStyle(LineStyleEvent event) {
				StyleRange[] ranges = layoutStyles(event.lineOffset);
				event.styles = ranges;
			}
		});
		

		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
				//System.out.println(text.getParent().getVerticalBar().getThumb());
				ScrollBar bar = text.getParent().getVerticalBar();
				if (e.keyCode == SWT.PAGE_DOWN) {
					bar.setSelection(bar.getSelection() + bar.getPageIncrement());
				} else if (e.keyCode == SWT.PAGE_UP) {
					bar.setSelection(bar.getSelection() - bar.getPageIncrement());
				} else if (e.keyCode == SWT.ARROW_DOWN) {
					bar.setSelection(bar.getSelection() + 1);
				} else if (e.keyCode == SWT.ARROW_UP) {
					bar.setSelection(bar.getSelection() - 1);
				} else if (e.keyCode == SWT.END && (e.stateMask & SWT.CTRL) != 0) {
					bar.setSelection(bar.getMaximum());
				} else if (e.keyCode == SWT.HOME && (e.stateMask & SWT.CTRL) != 0) {
					bar.setSelection(bar.getMinimum());
				} else {
					return;
				}
				redrawLines();
				e.doit = false;

			}
		});
		
		text.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				redrawLines();
			}
		});
		
		start();
		
	}

	protected StyleRange[] layoutStyles(int offset) {
		int rowN = topRow + text.getLineAtOffset(offset);
		synchronized (instHistory) {
			// mark breakpoints
			if (rowN >= 0 && rowN < instHistory.size()) {
				InstRow row = instHistory.get(rowN);
				IBreakpoint bp = machine.getExecutor().getBreakpoints().findBreakpoint(
						row.getInst().pc & 0xffff);
				if (bp != null) {
					StyleRange range = new StyleRange(bpStyle);
					range.start = offset;  
					range.length = text.getLine(rowN - topRow).length();
					return new StyleRange[] { range };
				}
			}
		}

		// mark line style
		StyleRange[] ranges = new StyleRange[instLabelProvider.getColumnCount()];
		boolean oddOp = true;
		int rangeIdx = 0;
		for (Column column : instLabelProvider.getColumns()) {
			TextStyle style;
			switch (column.role) {
			case UNKNOWN:
			case INSTRUCTION:
			case SYMBOL:
			default:
				style = baseTextStyle;
				break;
			case INPUT:
			case ADDRESS:
				style = bg1Style;
				break;
			case OUTPUT:
				style = bg2Style;
				break;
			case OPERAND:
				style = oddOp ? bg2Style : bg1Style;
				oddOp = !oddOp;
				break;
			}
			ranges[rangeIdx] = new StyleRange(style);
			ranges[rangeIdx].start = offset;  
			ranges[rangeIdx].length = column.width + 2 + (rangeIdx == 0 ? 2 : 0);  // implicit breakpoint column
			offset += ranges[rangeIdx].length;
			rangeIdx++;
		}
		return ranges;
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#setupEvents()
	 */
	@Override
	public void setupEvents() {
		text.addListener(SWT.Resize, new Listener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			@Override
			public void handleEvent(Event event) {
				text.setFont(smallerFont);
				int height = text.getClientArea().height;
				numRows = Math.max(1, height / text.getLineHeight());
				getVerticalBar().setIncrement(1);
				getVerticalBar().setPageIncrement(numRows);
			}
		});
		
		text.addMenuDetectListener(new MenuDetectListener() {
			
			@Override
			public void menuDetected(MenuDetectEvent e) {
				Point pt = getDisplay().map(null, text, e.x, e.y);
				int rowN = pt.y / text.getLineHeight() + topRow;
				synchronized (instHistory) {
					if (rowN >= 0 && rowN < instHistory.size()) {
						InstRow row = instHistory.get(rowN);
						System.out.println(row.getInst());
						final int pc = row.getInst().pc & 0xffff;
						
						Menu menu = new Menu(text);
						
						DebuggerWindow.addBreakpointActions(machine, menu, pc);
						
						SwtUtils.runMenu(null, e.x, e.y, menu);
					}
				}
			}
		});
		
		getVerticalBar().addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				redrawLines();
			}
		});
		
		machine.getExecutor().getBreakpoints().addListener(CpuInstructionTextCanvasComposite.this);

		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				machine.getFastMachineTimer().cancelTask(refreshTask);
				machine.getExecutor().getBreakpoints().removeListener(CpuInstructionTextCanvasComposite.this);
				smallerFont.dispose();
			}
		});
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#go()
	 */
	@Override
	public void go() {
		
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#refresh()
	 */
	@Override
	public void flush() {
		synchronized (instHistory) {
			redrawLines();
			getVerticalBar().setMaximum(instHistory.size());
			getVerticalBar().setSelection(instHistory.size() - numRows + 1);
		}
	}

	/**
	 * 
	 */
	private void redrawLines() {
		int rowIndex = getVerticalBar().getSelection();
		synchronized (text) {
			synchronized (instHistory) {
				final BreakpointManager bpMgr = machine.getExecutor().getBreakpoints();
				StringBuilder sb = new StringBuilder();
				int numCols = instLabelProvider.getColumnCount();
				Column[] cols = instLabelProvider.getColumns();
				int visible = numRows;
				int start = Math.min(rowIndex, instHistory.size());
				int end = Math.min(rowIndex + visible, instHistory.size());
				List<InstRow> subList = instHistory.subList(start, end);
				topRow = rowIndex;
				for (InstRow row : subList) {
					IBreakpoint bp = bpMgr.findBreakpoint(row.getInst().pc);
					if (bp == null) {
						sb.append("  ");
					} else {
						sb.append("• ");
					}
					
					for (int i = 0; i < numCols; i++) {
						String field = fieldOf(instLabelProvider.getColumnText(row, i), cols[i].width);
						sb.append(' ');
						sb.append(field);
						sb.append(' ');
					}
					sb.append('\n');
				}
				while (end - start < numRows) {
					for (int i = 0; i < numCols; i++) {
						sb.append(fieldOf("", cols[i].width + 2));
					}
					sb.append('\n');
					end++;
				}
				text.setText(sb.toString());
			}
		}
		//text.redraw();
		
	}

	/**
	 * @param op3
	 * @param i
	 * @return
	 */
	private String fieldOf(String str, int len) {
		if (str == null)
			str = "";
		if (str.length() == len)
			return str;
		if (str.length() < len) {
			while (str.length() < len) {
				str += "    ";
			}
		}
		if (str.length() > len) {
			return str.substring(0, len);
		}
		return str;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IBreakpointListener#breakpointAdded(v9t9.common.cpu.IBreakpoint)
	 */
	@Override
	public void breakpointChanged(IBreakpoint bp, boolean added) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				redrawLines();
			}
		});
	}
	

}
