/**
 * 
 */
package v9t9.emulator.clients.builtin.swt.debugger;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import v9t9.emulator.Machine;
import v9t9.emulator.hardware.V9t9;
import v9t9.emulator.runtime.Executor;
import v9t9.emulator.runtime.InstructionListener;
import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.InstructionWorkBlock;
import v9t9.engine.settings.ISettingListener;
import v9t9.engine.settings.Setting;
import v9t9.utils.Utils;

/**
 * @author ejs
 *
 */
public class CpuViewer extends Composite implements InstructionListener {

	private Button playPauseButton;
	private Image playImage;
	private Image pauseImage;
	private Image stepImage;
	private Button stepButton;
	private ISettingListener pauseListener;
	private final Machine machine;

	private TableViewer instTableViewer;
	private InstContentProvider instContentProvider;
	private TimerTask refreshTask;
	private Text nextInstructionText;
	private Image watchImage;
	private Button watchButton;
	protected boolean isWatching;
	private boolean showNextInstruction;
	
	private boolean sizedColumns;
	private Image clearImage;
	private Button clearButton;
	private Font tableFont;
	private Font smallerFont;
	
	public CpuViewer(Composite parent, int style, final Machine machine, Timer timer) {
		super(parent, style);
		this.machine = machine;
		
		setLayout(new GridLayout());
		
		Composite buttonBar = new Composite(this, SWT.NONE);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.RIGHT, SWT.CENTER).applyTo(buttonBar);
		GridLayoutFactory.swtDefaults().numColumns(10).applyTo(buttonBar);

		Image icons = new Image(getDisplay(), V9t9.getDataFile("icons/cpu.png").getAbsolutePath()); 

		/////
		
		playImage = getSubImage(icons, 0, 0, 24, 24);
		pauseImage = getSubImage(icons, 24, 0, 24, 24);
		playPauseButton = new Button(buttonBar, SWT.TOGGLE | SWT.TRANSPARENT);
		GridDataFactory.swtDefaults()/*.hint(24, 24)*/.applyTo(playPauseButton);
		playPauseButton.setToolTipText("Run or pause the machine");
		
		updatePlayPauseButtonImage();
		playPauseButton.setSelection(Machine.settingPauseMachine.getBoolean());
		playPauseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				machine.asyncExec(new Runnable() {
					public void run() {
						Machine.settingPauseMachine.setBoolean(!Machine.settingPauseMachine.getBoolean());
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
		pauseListener = new ISettingListener() {

			public void changed(final Setting setting, Object oldValue) {
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
		Machine.settingPauseMachine.addListener(pauseListener);
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
						Executor.settingSingleStep.setBoolean(true);
						showNextInstruction = true;
						Machine.settingPauseMachine.setBoolean(false);
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
			}
		});
		
		icons.dispose();
		
		///
		instTableViewer = new TableViewer(this, SWT.V_SCROLL + SWT.BORDER + SWT.VIRTUAL + SWT.NO_FOCUS);
		instContentProvider = new InstContentProvider();
		instTableViewer.setContentProvider(instContentProvider);
		instTableViewer.setLabelProvider(new InstLabelProvider(
				getDisplay().getSystemColor(SWT.COLOR_RED)
				));
		final Table table = instTableViewer.getTable();
		GridDataFactory.fillDefaults().grab(true, true).span(1, 1).applyTo(table);
		

		FontDescriptor fontDescriptor = Utils.getFontDescriptor(JFaceResources.getTextFont());
		//fontDescriptor = fontDescriptor.increaseHeight(-2);
		tableFont = fontDescriptor.createFont(getDisplay());
		table.setFont(tableFont);
		
		GC gc = new GC(getDisplay());
		gc.setFont(tableFont);
		int charWidth = gc.stringExtent("M").x;
		gc.dispose();

		FontDescriptor smallerFontDescriptor = fontDescriptor.increaseHeight(-2);
		smallerFont = smallerFontDescriptor.createFont(getDisplay());
		
		TableColumn column;
		String[] props = new String[5];
		
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

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		instTableViewer.setColumnProperties(props);
		
		nextInstructionText = new Text(this, SWT.READ_ONLY | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(nextInstructionText);
		
		////
		
		Machine.settingPauseMachine.setBoolean(true);
		machine.getExecutor().addInstructionListener(this);
		
		refreshTask = new TimerTask() {

			@Override
			public void run() {
				if (refreshTask == null)
					return;
				showNextInstruction = true;
				synchronized (machine.getExecutionLock()) {
					if (machine.isExecuting()) {
						getDisplay().asyncExec(new Runnable() {
							public void run() {
								refreshTable();
							}
						});
					}
				}
			}
			
		};
		timer.schedule(refreshTask, 0, 250);
		
		instTableViewer.setInput(new Object());
	}



	private void updatePlayPauseButtonImage() {
		if (Machine.settingPauseMachine.getBoolean()) {
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



	@Override
	public void dispose() {
		machine.getExecutor().removeInstructionListener(this);
		if (refreshTask != null) {
			refreshTask.cancel();
			refreshTask = null;
		}
		tableFont.dispose();
		smallerFont.dispose();
		Machine.settingPauseMachine.removeListener(pauseListener);
		playImage.dispose();
		pauseImage.dispose();
		stepImage.dispose();
		watchImage.dispose();
		clearImage.dispose();
		Machine.settingPauseMachine.setBoolean(false);
		
		super.dispose();
	}
	

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionWorkBlock, v9t9.engine.cpu.InstructionWorkBlock)
	 */
	public void executed(final InstructionWorkBlock before, InstructionWorkBlock after_) {
		if (isWatching || showNextInstruction) {
			InstructionWorkBlock after= new InstructionWorkBlock();
	        after_.copyTo(after);
			InstRow row = new InstRow(before, after);
			instContentProvider.addInstRow(row);
			showNextInstruction = false;
			//refreshTable();
		}
		if (Executor.settingSingleStep.getBoolean()) {
			Executor.settingSingleStep.setBoolean(false);
			Machine.settingPauseMachine.setBoolean(true);
			machine.getExecutor().interruptExecution = Boolean.TRUE;
		}
		//throw new AbortedException();
	}

	protected void refreshTable() {
		if (isDisposed())
			return;
		getDisplay().syncExec(new Runnable() {
			public void run() {
				//instContentProvider.refreshTable();
				//instTableViewer.refresh();
				if (!instTableViewer.getTable().isDisposed()) {
					//instTableViewer.setSelection(null);
					int count = instContentProvider.getCount();
					instTableViewer.setItemCount(count);
					instTableViewer.getTable().setSelection(new int[] { count - 1 });
					
					if (!sizedColumns && count > 100) {
						//resizeTable();
						sizedColumns = true;
					}
					if (false) {
						for (TableColumn column1 : instTableViewer.getTable().getColumns()) {
							column1.pack();
						}
					}
						//sizedColumns = true;
					//}
					
					if (isWatching || Machine.settingPauseMachine.getBoolean()) {
						Instruction inst = machine.getExecutor().interp.getInstruction(machine.getCpu());
						nextInstructionText.setText(inst.toString());
					} else {
						nextInstructionText.setText("");
					}
				}
			}
		});
	}
	
	protected void resizeTable() {
		for (TableColumn column1 : instTableViewer.getTable().getColumns()) {
			column1.pack();
		}
	}
}
