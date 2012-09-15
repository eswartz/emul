/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.IBreakpoint;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.IExecutor;
import v9t9.common.cpu.IInstructionListener;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.common.cpu.SimpleBreakpoint;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.gui.EmulatorGuiData;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

/**
 * @author ejs
 *
 */
public class CpuViewer extends Composite implements IInstructionListener {
	public interface ICpuTracker {
		void updateForInstruction();
	}
	
	private IProperty pauseMachine;
	private IProperty debugging;

	private Button playPauseButton;
	private Image playImage;
	private Image pauseImage;
	private Image stepImage;
	private Image stepOverImage;
	private Button stepButton;
	private Button stepOverButton;
	private IPropertyListener pauseListener;
	private final IMachine machine;

	private TimerTask refreshTask;
	private Image watchImage;
	private Button watchButton;
	protected boolean isWatching;
	private boolean showNextInstruction;
	
	private Image clearImage;
	private Button clearButton;
	private ICpuTracker tracker;
	private boolean isVisible;

	private IProperty singleStep;
	private CpuInstructionComposite instructionComposite;
	
	public CpuViewer(Composite parent, int style, final IMachine machine_, Timer timer) {
		super(parent, style);
		this.machine = machine_;
		
		pauseMachine = Settings.get(machine, IMachine.settingPauseMachine);
		debugging = Settings.get(machine, ICpu.settingDebugging);
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
						//partialInst = null;
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
							instructionComposite.refresh();
						}
					}
				});
			}
			
		};
		
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
		stepOverImage = getSubImage(icons, 120, 0, 24, 24);
		stepOverButton = new Button(buttonBar, SWT.PUSH);
		stepOverButton.setImage(stepOverImage);
		stepOverButton.setToolTipText("Skip this instruction (e.g., a call, a jump in a loop)");
		
		GridDataFactory.swtDefaults()/*.hint(24, 24)*/.applyTo(stepOverButton);
		
		stepOverButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				machine.asyncExec(new Runnable() {
					public void run() {
						ICpuState state = machine.getCpu().getState();
						RawInstruction curInst = machine.getInstructionFactory().decodeInstruction(
								state.getPC(), machine.getConsole());
						int bpPc = (state.getPC() + curInst.getSize());

						IBreakpoint bp = new SimpleBreakpoint(bpPc & 0xffff, true);
						machine.getExecutor().getBreakpoints().addBreakpoint(bp);
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
				instructionComposite.clear();
			}
		});
		
		icons.dispose();
	
		instructionComposite = new CpuInstructionTableComposite(this, SWT.NONE, machine);
//		instructionComposite = new CpuInstructionListComposite(this, SWT.NONE, machine);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(instructionComposite);
		
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
					instructionComposite.refresh();
				} finally {
					working = false;
				}
			}
			
		};
		timer.schedule(refreshTask, 0, 250);
		
		instructionComposite.setupEvents();
		
		
		addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				machine.getExecutor().removeInstructionListener(CpuViewer.this);
				if (refreshTask != null) {
					refreshTask.cancel();
					refreshTask = null;
				}
				pauseMachine.removeListener(pauseListener);
				playImage.dispose();
				pauseImage.dispose();
				stepImage.dispose();
				stepOverImage.dispose();
				watchImage.dispose();
				clearImage.dispose();
				
				pauseMachine.setBoolean(false);
				debugging.setBoolean(false);
			}
			
		});
		

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
		
		instructionComposite.go();
		
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

	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IInstructionListener#preExecute(v9t9.common.cpu.InstructionWorkBlock)
	 */
	@Override
	public boolean preExecute(InstructionWorkBlock before) {
		return true;
	}

	public void executed(final InstructionWorkBlock before, InstructionWorkBlock after_) {
		if (!isVisible)
			return;
		
		if (isWatching || showNextInstruction) {
			instructionComposite.executed(before, after_);

			if (tracker != null)
				tracker.updateForInstruction();
			
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

	public void setTracker(ICpuTracker tracker) {
		this.tracker = tracker;
	}
}
