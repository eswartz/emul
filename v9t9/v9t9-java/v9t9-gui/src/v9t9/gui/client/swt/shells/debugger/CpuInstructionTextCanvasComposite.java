/*
  CpuInstructionTextCanvasComposite.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.ejs.gui.common.FontUtils;

import v9t9.common.cpu.IInstructionEffectLabelProvider.Column;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class CpuInstructionTextCanvasComposite extends CpuInstructionComposite {
	
	private StyledText text;
	private int numRows;
	
	private Runnable refreshTask;
	private TextStyle baseTextStyle;
	private TextStyle bg1Style;
	private TextStyle bg2Style;
	private InstLabelProvider instLabelProvider;
	private Font smallerFont;
	
	public CpuInstructionTextCanvasComposite(Composite parent, int style, IMachine machine) {
		super(parent, style | SWT.V_SCROLL, machine);
		this.instLabelProvider = new InstLabelProvider(machine.getCpu().createInstructionEffectLabelProvider());
		
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

		text.addLineStyleListener(new LineStyleListener() {
			
			@Override
			public void lineGetStyle(LineStyleEvent event) {
				StyleRange[] ranges = layoutStyles(event.lineOffset);
				event.styles = ranges;
			}
		});
		
		start();
		
	}

	protected StyleRange[] layoutStyles(int offset) {
		StyleRange[] ranges = new StyleRange[instLabelProvider.getColumnCount()];
		boolean oddOp = true;
		int rangeIdx = 0;
		for (Column column : instLabelProvider.getColumns()) {
			TextStyle style;
			switch (column.role) {
			case UNKNOWN:
			case SYMBOL:
			case INSTRUCTION:
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
			ranges[rangeIdx].length = column.width + 2;
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
				StringBuilder sb = new StringBuilder();
				int numCols = instLabelProvider.getColumnCount();
				Column[] cols = instLabelProvider.getColumns();
				int visible = numRows;
				int start = Math.min(rowIndex, instHistory.size());
				int end = Math.min(rowIndex + visible, instHistory.size());
				List<InstRow> subList = instHistory.subList(start, end);
				for (InstRow row : subList) {
					for (int i = 0; i < numCols; i++) {
						sb.append(' ');
						sb.append(fieldOf(instLabelProvider.getColumnText(row, i), cols[i].width));
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
	

}
