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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
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
		playPauseButton = new Button(buttonBar, SWT.TOGGLE);
		GridDataFactory.swtDefaults()/*.hint(24, 24)*/.applyTo(playPauseButton);
		playPauseButton.setToolTipText("Run or pause the machine");
		
		updatePlayPauseButtonImage();
		playPauseButton.setSelection(Machine.settingPauseMachine.getBoolean());
		playPauseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						Machine.settingPauseMachine.setBoolean(playPauseButton.getSelection());
						updatePlayPauseButtonImage();	
						refreshTable();
					}
				});
			}
		});
		playPauseButton.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (e.keyCode == 'p') {
							Machine.settingPauseMachine.setBoolean(!Machine.settingPauseMachine.getBoolean());
							refreshTable();
						}
					}
				});
			}
		});
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
				getDisplay().asyncExec(new Runnable() {
					public void run() {
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
		watchButton.setToolTipText("If pressed, record every instruction executed; else only update periodically");
		
		GridDataFactory.swtDefaults()/*.hint(24, 24)*/.applyTo(watchButton);
		
		watchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isWatching = watchButton.getSelection();
			}
		});
		///
		
		instTableViewer = new TableViewer(this, SWT.V_SCROLL + SWT.BORDER + SWT.VIRTUAL + SWT.NO_FOCUS);
		instContentProvider = new InstContentProvider();
		instTableViewer.setContentProvider(instContentProvider);
		instTableViewer.setLabelProvider(new InstLabelProvider(
				getDisplay().getSystemColor(SWT.COLOR_RED)
				));
		final Table table = instTableViewer.getTable();
		GridDataFactory.fillDefaults().grab(true, true).span(1, 1).applyTo(table);
		
		String[] props = new String[1 + 1];
		props[0] = "Addr";
		new TableColumn(table, SWT.CENTER).setText(props[0]);
		props[1] = "Inst";
		new TableColumn(table, SWT.LEFT).setText(props[1]);
		
		FontDescriptor fontDescriptor = Utils.getFontDescriptor(JFaceResources.getTextFont());
		//fontDescriptor = fontDescriptor.increaseHeight(-2);
		table.setFont(fontDescriptor.createFont(getDisplay()));
		
		for (TableColumn column : table.getColumns()) {
			column.pack();
		}
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
					if (machine.isExecuting())
						refreshTable();
				}
			}
			
		};
		timer.schedule(refreshTask, 0, 1000);
		
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
		Image sub = new Image(getDisplay(), new Rectangle(0, 0, w, h));
		GC gc = new GC(sub);
		gc.drawImage(icons, x, y, w, h, 0, 0, w, h);
		gc.dispose();
		return sub;
	}



	@Override
	public void dispose() {
		machine.getExecutor().removeInstructionListener(this);
		if (refreshTask != null) {
			refreshTask.cancel();
			refreshTask = null;
		}
		Machine.settingPauseMachine.removeListener(pauseListener);
		playImage.dispose();
		pauseImage.dispose();
		stepImage.dispose();
		
		Machine.settingPauseMachine.setBoolean(false);
		
		super.dispose();
	}
	

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionWorkBlock, v9t9.engine.cpu.InstructionWorkBlock)
	 */
	public void executed(final InstructionWorkBlock before, InstructionWorkBlock after) {
		if (isWatching || showNextInstruction) {
			String inst = before.inst.toString();
			String addr = Utils.toHex4(before.pc);
			InstRow row = new InstRow(addr, inst);
			instContentProvider.addInstRow(row);
			showNextInstruction = false;
			refreshTable();
		}
		if (Executor.settingSingleStep.getBoolean()) {
			Executor.settingSingleStep.setBoolean(false);
			Machine.settingPauseMachine.setBoolean(true);
		}
		//throw new AbortedException();
		machine.getExecutor().interruptExecution = Boolean.TRUE;
	}

	protected void refreshTable() {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				//instContentProvider.refreshTable();
				//instTableViewer.refresh();
				if (!instTableViewer.getTable().isDisposed()) {
					//instTableViewer.setSelection(null);
					instTableViewer.setItemCount(instContentProvider.getCount());
					instTableViewer.getTable().setSelection(new int[] { instContentProvider.getCount() - 1 });
					
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
}
