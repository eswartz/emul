/**
 * 
 */
package v9t9.gui.client.swt.shells;

import java.io.IOException;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.demos.IDemoHandler;
import v9t9.common.demos.IDemoHandler.IDemoListener;
import v9t9.common.demos.IDemoPlayer;
import v9t9.common.events.NotifyEvent;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.bars.ImageCanvas;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingProperty;

/**
 * @author ejs
 *
 */
public class DemoProgressBar extends Composite {
	public static final String DEMO_PROGRESS_BAR_ID = "demo.progress.bar";
	private IProperty pauseProperty;
	private IProperty reverseProperty;
	private IProperty generateSpeechProperty;
	private IDemoListener demoListener;
	protected IDemoPlayer player;
	private long prev;
	private double totalTime;
	private int maxDemoScale;
	

	public DemoProgressBar(Shell shell, final SwtWindow window, final IMachine machine) {
		super(shell, SWT.NONE);
		
		shell.setText("Demo Timeline");

		maxDemoScale = getDisplay().getBounds().width;
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		pauseProperty = (ISettingProperty) Settings.get(
				machine, IDemoHandler.settingDemoPaused);
		reverseProperty = (ISettingProperty) Settings.get(
				machine, IDemoHandler.settingDemoReversing);
		generateSpeechProperty = (ISettingProperty) Settings.get(
				machine, ISpeechChip.settingGenerateSpeech);
		
		final Scale control = new Scale(this, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(control);
		
//		control.setVisible(false);
//		shell.setVisible(false);
		
		demoListener = new IDemoHandler.IDemoPlaybackListener() {

			@Override
			public void firedEvent(NotifyEvent event) {
			}
			

			@Override
			public void started(final IDemoPlayer player) {
				DemoProgressBar.this.player = player;
				getDisplay().syncExec(new Runnable() {

					public void run() {
						totalTime = player.getTotalTime();
						control.setMaximum(maxDemoScale);
						
						int incr = (int) (maxDemoScale / (int) (totalTime / 1000));
						control.setIncrement(incr);
						control.setPageIncrement(Math.max(10, incr));
						
						prev = System.currentTimeMillis();
					}
				});
			}
			
			@Override
			public void updatedPosition(final double time) {
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (control.isDisposed())
							return;
						prev = System.currentTimeMillis();
						control.setSelection((int)( time * maxDemoScale / totalTime));
					}
				});
			}
			
			@Override
			public void stopped() {
				
			}
			
		};

		machine.getDemoHandler().addListener(demoListener);

		control.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					pauseProperty.setBoolean(true);
				}
				else if (e.button == 3) {
					if (player.getCurrentTime() == 0 && pauseProperty.getBoolean())
						reverseProperty.setBoolean(false);
					else if (player.getCurrentTime() >= totalTime && pauseProperty.getBoolean())
						reverseProperty.setBoolean(true);
					else
						reverseProperty.setBoolean(! reverseProperty.getBoolean());
					
				}
			}
			@Override
			public void mouseUp(MouseEvent e) {
				pauseProperty.setBoolean(false);
			}
		});

		control.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				long now = System.currentTimeMillis();
				if (prev == 0)
					prev = now;
				if (player != null) {
					synchronized (player) {
						boolean orig = generateSpeechProperty.getBoolean();
						try {
							//pauseProperty.setBoolean(false);
							generateSpeechProperty.setBoolean(false);
							player.seekToTime(control.getSelection() * totalTime / maxDemoScale);
							prev = System.currentTimeMillis();
						} catch (IOException e1) {
							control.setSelection((int) (player.getCurrentTime() * maxDemoScale / totalTime));
						} finally {
							generateSpeechProperty.setBoolean(orig);
						}
					}
				}
			}
			
		});
		
		control.addKeyListener(new KeyAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.KeyAdapter#keyPressed(org.eclipse.swt.events.KeyEvent)
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == ' ') {
					pauseProperty.setBoolean(! pauseProperty.getBoolean());
				}
			}
			
		});
		
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				machine.getDemoHandler().removeListener(demoListener);				
			}
		});
		
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				Point sz = window.getShell().getSize();
				Point csz = control.computeSize(sz.x, -1);
				getShell().setSize(sz.x, Math.max(32, csz.y));
				
				getShell().open();

			}
		});
	}


	/**
	 * @param buttonBar
	 * @return
	 */
	public static IToolShellFactory getToolShellFactory(final ImageCanvas buttonBar,
			final IMachine machine,
			final SwtWindow window) {
		return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				//behavior.boundsPref = "DemoProgressBarBounds";
				behavior.centering = Centering.BELOW;
				behavior.centerOverControl = window.getShell();
				behavior.dismissOnClickOutside = false;
				behavior.style = SWT.TOOL;
			}
			public Control createContents(Shell shell) {
				DemoProgressBar dialog = new DemoProgressBar(shell, window, machine);
				return dialog;
			}
			@Override
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}



}
