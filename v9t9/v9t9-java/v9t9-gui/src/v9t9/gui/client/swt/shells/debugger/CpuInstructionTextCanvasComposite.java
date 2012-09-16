/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class CpuInstructionTextCanvasComposite extends CpuInstructionComposite {

	private static final int ADDR_WIDTH = 16;
	private static final int INST_WIDTH = 32;
	private static final int OP1_WIDTH = 12;
	private static final int OP2_WIDTH = 12;
	private static final int OP3_WIDTH = 12;
	
	private StyledText text;
	private int numRows;
	
	private Runnable refreshTask;
	private TextStyle baseTextStyle;
	private TextStyle bg1Style;
	private TextStyle bg2Style;
	
	public CpuInstructionTextCanvasComposite(Composite parent, int style, IMachine machine) {
		super(parent, style | SWT.V_SCROLL, machine);
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		text = new StyledText(this, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(text);

		baseTextStyle = new TextStyle(JFaceResources.getTextFont(), null, null);
		bg1Style = new TextStyle(baseTextStyle);
		bg1Style.background = getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		bg2Style = new TextStyle(baseTextStyle);
		bg2Style.background = getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);

		text.addLineStyleListener(new LineStyleListener() {
			
			@Override
			public void lineGetStyle(LineStyleEvent event) {
				StyleRange[] ranges = layoutStyles(event.lineOffset,
						bg1Style, ADDR_WIDTH,
						baseTextStyle, 2,
						bg2Style, INST_WIDTH,
						baseTextStyle, 2,
						bg1Style, OP1_WIDTH,
						baseTextStyle, 2,
						bg2Style, OP2_WIDTH,
						baseTextStyle, 2,
						bg1Style, OP3_WIDTH);
				
				event.styles = ranges;
			}
		});
		
		start();
		
	}

	protected StyleRange[] layoutStyles(int offset, Object... stylesAndWidths) {
		StyleRange[] ranges = new StyleRange[stylesAndWidths.length / 2];
		for (int i = 0; i  < ranges.length; i++) {
			ranges[i] = new StyleRange((TextStyle) stylesAndWidths[i*2]);
			ranges[i].start = offset;  
			ranges[i].length = (Integer) stylesAndWidths[i*2 + 1];
			offset += ranges[i].length;
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
				GC gc = new GC(text);
				gc.setFont(baseTextStyle.font);
				int fontHeight = gc.getFontMetrics().getHeight();
				gc.dispose();
				int height = text.getClientArea().height;
				numRows = Math.max(1, height / fontHeight);
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
			getVerticalBar().setMaximum(instHistory.size());
			getVerticalBar().setSelection(instHistory.size() - numRows + 1);
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
				StringBuilder sb = new StringBuilder();
				int visible = numRows;
				int start = Math.min(rowIndex, instHistory.size());
				int end = Math.min(rowIndex + visible, instHistory.size());
				List<InstRow> subList = instHistory.subList(start, end);
				for (InstRow row : subList) {
					sb.append(fieldOf(row.getAddress(), ADDR_WIDTH));
					sb.append("  ");
					sb.append(fieldOf(row.getInst(), INST_WIDTH));
					sb.append("  ");
					sb.append(fieldOf(row.getOp1(), OP1_WIDTH));
					sb.append("  ");
					sb.append(fieldOf(row.getOp2(), OP2_WIDTH));
					sb.append("  ");
					sb.append(fieldOf(row.getOp3(), OP3_WIDTH));
					sb.append('\n');
				}
				while (end - start < numRows) {
					sb.append(fieldOf("", ADDR_WIDTH));
					sb.append("  ");
					sb.append(fieldOf("", INST_WIDTH));
					sb.append("  ");
					sb.append(fieldOf("", OP1_WIDTH));
					sb.append("  ");
					sb.append(fieldOf("", OP2_WIDTH));
					sb.append("  ");
					sb.append(fieldOf("", OP3_WIDTH));
					sb.append('\n');
					end++;
				}
				text.setText(sb.toString());
			}
		}
		text.redraw();
		
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
