/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.IExecutor;
import v9t9.common.cpu.IInstructionListener;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.gui.EmulatorGuiData;
import v9t9.gui.common.FontUtils;

/**
 * @author ejs
 *
 */
public class CpuViewer extends Composite implements IInstructionListener {
	public interface ICpuTracker {
		void updateForInstruction();
	}
	
	private IProperty pauseMachine;
	
	private Button playPauseButton;
	private Image playImage;
	private Image pauseImage;
	private Image stepImage;
	private Button stepButton;
	private IPropertyListener pauseListener;
	private final IMachine machine;

	private TableViewer instTableViewer;
	private InstContentProvider instContentProvider;
	private TimerTask refreshTask;
	private Image watchImage;
	private Button watchButton;
	protected boolean isWatching;
	private boolean showNextInstruction;
	
	private Image clearImage;
	private Button clearButton;
	private Font tableFont;
	private Font smallerFont;
	private ICpuTracker tracker;
	private InstRow partialInst;
	private boolean changed;
	private boolean isVisible;

	private IProperty singleStep;
	
	public CpuViewer(Composite parent, int style, final IMachine machine, Timer timer) {
		super(parent, style);
		this.machine = machine;
		
		pauseMachine = Settings.get(machine, IMachine.settingPauseMachine);
		singleStep = Settings.get(machine, IExecutor.settingSingleStep);
		
		setLayout(new GridLayout());
		
		Composite buttonBar = new Composite(this, SWT.NONE);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.RIGHT, SWT.CENTER).applyTo(buttonBar);
		GridLayoutFactory.swtDefaults().numColumns(10).applyTo(buttonBar);

		Image icons = EmulatorGuiData.loadImage(getDisplay(), "icons/cpu.png");

		/////
		
		playImage = getSubImage(icons, 0, 0, 24, 24);
		pauseImage = getSubImage(icons, 24, 0, 24, 24);
		playPauseButton = new Button(buttonBar, SWT.TOGGLE | SWT.TRANSPARENT);
		GridDataFactory.swtDefaults()/*.hint(24, 24)*/.applyTo(playPauseButton);
		playPauseButton.setToolTipText("Run or pause the machine");
		
		updatePlayPauseButtonImage();
		playPauseButton.setSelection(pauseMachine.getBoolean());
		playPauseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				machine.asyncExec(new Runnable() {
					public void run() {
						partialInst = null;
						pauseMachine.setBoolean(!pauseMachine.getBoolean());
						//resizeTable();
					}
				});
			}
		});
		/*
		playPauseButton.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (e.keyCode == 'p') {
							Machine.settingPauseMachine.setBoolean(!Machine.settingPauseMachine.getBoolean());
							refreshTable();
							resizeTable();
						}
					}
				});
			}
		});*/
		pauseListener = new IPropertyListener() {

			public void propertyChanged(final IProperty setting) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (!playPauseButton.isDisposed()) {
							playPauseButton.setSelection(setting.getBoolean());
							updatePlayPauseButtonImage();
							refreshTable();
						}
					}
				});
			}
			
		};
		//Machine.settingPauseMachine.addListener(pauseListener);
		////
		
		stepImage = getSubImage(icons, 48, 0, 24, 24);
		stepButton = new Button(buttonBar, SWT.PUSH);
		stepButton.setImage(stepImage);
		stepButton.setToolTipText("Single step over the next instruction (pauses if running)");
		
		GridDataFactory.swtDefaults()/*.hint(24, 24)*/.applyTo(stepButton);
		
		stepButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				machine.asyncExec(new Runnable() {
					public void run() {
						//if (!Machine.settingPauseMachine.getBoolean())
						//	resizeTable();
						singleStep.setBoolean(true);
						showNextInstruction = true;
						pauseMachine.setBoolean(false);
					}
				});
				
			}
		});
		
		///

		watchImage = getSubImage(icons, 72, 0, 24, 24);
		watchButton = new Button(buttonBar, SWT.TOGGLE);
		watchButton.setImage(watchImage);
		watchButton.setToolTipText("If pressed, record every instruction executed when running; else only update periodically");
		
		GridDataFactory.swtDefaults()/*.hint(24, 24)*/.applyTo(watchButton);
		isWatching = true;
		watchButton.setSelection(true);
		
		watchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isWatching = watchButton.getSelection();
			}
		});
		///
		
		clearImage = getSubImage(icons, 96, 0, 24, 24);
		clearButton = new Button(buttonBar, SWT.PUSH);
		clearButton.setImage(clearImage);
		clearButton.setToolTipText("Clear the instruction list");
		
		GridDataFactory.swtDefaults()/*.hint(24, 24)*/.applyTo(clearButton);
		
		clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				instContentProvider.clear();
				partialInst = null;
				changed = true;
			}
		});
		
		icons.dispose();
		
		///
		instTableViewer = new TableViewer(this, SWT.V_SCROLL + SWT.BORDER + SWT.VIRTUAL + SWT.NO_FOCUS + SWT.FULL_SELECTION);
		instContentProvider = new InstContentProvider();
		instTableViewer.setContentProvider(instContentProvider);
		instTableViewer.setLabelProvider(new InstLabelProvider(
				//getDisplay().getSystemColor(SWT.COLOR_RED)
				));
		final Table table = instTableViewer.getTable();
		GridDataFactory.fillDefaults().grab(true, true).span(1, 1).applyTo(table);
		
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				isVisible = event.type == SWT.Show;
				if (isVisible) {
					machine.getExecutor().addInstructionListener(CpuViewer.this);
					pauseMachine.addListener(pauseListener);
					pauseMachine.setBoolean(true);
				} else {
					machine.getExecutor().removeInstructionListener(CpuViewer.this);
					pauseMachine.removeListener(pauseListener);
					pauseMachine.setBoolean(false);
				}
			}
		};
		getShell().addListener(SWT.Show, listener);
		getShell().addListener(SWT.Hide, listener);
		
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
		
		////
		
		refreshTask = new TimerTask() {

			volatile boolean working;
			@Override
			public void run() {
				if (working)
					return;
				if (refreshTask == null)
					return;
				if (CpuViewer.this.isDisposed())
					return;
				if (!machine.isExecuting())
					return;
				working = true;
				try {
					if (tracker != null)
						tracker.updateForInstruction();
					if (!isWatching) {
						showNextInstruction = true;
					}
					refreshTable();
				} finally {
					working = false;
				}
			}
			
		};
		timer.schedule(refreshTask, 0, 250);
		
		instTableViewer.setInput(new Object());
		
		addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				machine.getExecutor().removeInstructionListener(CpuViewer.this);
				if (refreshTask != null) {
					refreshTask.cancel();
					refreshTask = null;
				}
				tableFont.dispose();
				smallerFont.dispose();
				pauseMachine.removeListener(pauseListener);
				playImage.dispose();
				pauseImage.dispose();
				stepImage.dispose();
				watchImage.dispose();
				clearImage.dispose();
				pauseMachine.setBoolean(false);				
			}
			
		});
		
		machine.getExecutor().addInstructionListener(CpuViewer.this);
		pauseMachine.addListener(pauseListener);
		pauseMachine.setBoolean(true);
	}



	private void updatePlayPauseButtonImage() {
		if (pauseMachine.getBoolean()) {
			playPauseButton.setImage(pauseImage);
		} else {
			playPauseButton.setImage(playImage);
		}
		playPauseButton.update();
	}



	private Image getSubImage(Image icons, int x, int y, int w, int h) {
		ImageData iconData = icons.getImageData();
		ImageData data = new ImageData(w, h, iconData.depth, iconData.palette);
		int ibps = iconData.depth/8;
		data.alphaData = new byte[data.width * data.height];
		for (int r = 0; r < h; r++) {
			for (int c = 0; c < w; c++) {
				data.alphaData[r * data.width + c] = iconData.alphaData[(r + y) * iconData.width + (x + c)];
				System.arraycopy(iconData.data, (r + y) * iconData.bytesPerLine + (x + c) * ibps, 
						data.data, r * data.bytesPerLine + c * ibps, 3);
			}
		}
		
		Image sub = new Image(getDisplay(), data);
		/*
		data.transparentPixel = iconData.getPixel(0, 0);
		//Image sub = new Image(getDisplay(), w, h);
		GC gc = new GC(sub);
		//gc.setAdvanced(true);
		//gc.setAlpha(255);
		//gc.fillRectangle(0, 0, w, h);
		//gc.setAlpha(255);
		gc.drawImage(icons, x, y, w, h, 0, 0, w, h);
		gc.dispose();
		*/
		return sub;
	}



	public void executed(final InstructionWorkBlock before, InstructionWorkBlock after_) {
		if (!isVisible)
			return;
		
		if (isWatching || showNextInstruction) {
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
					instTableViewer.refresh(row);
					//instTableViewer.getTable().setSelection(new int[] { count - 1 });
				}
			});
			showNextInstruction = false;
		}
		if (singleStep.getBoolean()) {
			singleStep.setBoolean(false);
			pauseMachine.setBoolean(true);
			machine.getExecutor().interruptExecution();
			if (tracker != null)
				tracker.updateForInstruction();
		}
		//throw new AbortedException();
	}

	volatile private Runnable refreshRunnable; 
	protected void refreshTable() {
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
						partialInst = row;
						//refreshTable();
						
						if (tracker != null)
							tracker.updateForInstruction();
						
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
	
	public void setTracker(ICpuTracker tracker) {
		this.tracker = tracker;
	}
}
