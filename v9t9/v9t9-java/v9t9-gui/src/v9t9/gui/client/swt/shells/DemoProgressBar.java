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
import v9t9.gui.client.swt.bars.ImageBar;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingProperty;

/**
 * @author ejs
 *
 */
public class DemoProgressBar extends Composite {
	/**
	 * 
	 */
	private static final double TICKS_TO_TIME = 1000;
	
	public static final String DEMO_PROGRESS_BAR_ID = "demo.progress.bar";
	private IProperty pauseProperty;
	private IProperty reverseProperty;
	private IProperty generateSpeechProperty;
	private IDemoListener demoListener;
	protected IDemoPlayer player;
	private long prev;

	public DemoProgressBar(Shell shell, final SwtWindow window, final IMachine machine) {
		super(shell, SWT.NONE);
		
		shell.setText("Demo Timeline");

		GridLayoutFactory.fillDefaults().applyTo(this);
		
		pauseProperty = (ISettingProperty) Settings.get(
				machine, IDemoHandler.settingDemoPaused);
		reverseProperty = (ISettingProperty) Settings.get(
				machine, IDemoHandler.settingDemoReversing);
		generateSpeechProperty = (ISettingProperty) Settings.get(
				machine, ISpeechChip.settingGenerateSpeech);
		
		final Scale control = new Scale(this, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(control);
		
		demoListener = new IDemoHandler.IDemoPlaybackListener() {

			@Override
			public void firedEvent(NotifyEvent event) {
			}
			

			@Override
			public void started(final IDemoPlayer player) {
				DemoProgressBar.this.player = player;
				getDisplay().syncExec(new Runnable() {
					public void run() {
						double totalTime = player.getTotalTime();
						int max = (int) (totalTime * TICKS_TO_TIME);
						control.setMaximum(max);
						control.setPageIncrement(1 + (int) (max / 20));
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
						control.setSelection((int) (time * TICKS_TO_TIME));
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
					else if (player.getCurrentTime() == player.getTotalTime() && pauseProperty.getBoolean())
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
							player.seekToTime(control.getSelection() / TICKS_TO_TIME);
							prev = System.currentTimeMillis();
						} catch (IOException e1) {
							control.setSelection((int) (player.getCurrentTime() * TICKS_TO_TIME));
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
		
		//pack();
	}


	/**
	 * @param buttonBar
	 * @return
	 */
	public static IToolShellFactory getToolShellFactory(final ImageBar buttonBar,
			final IMachine machine,
			final SwtWindow window) {
		return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "DemoProgressBarBounds";
				//behavior.centering = Centering.OUTSIDE;
				behavior.centerOverControl = buttonBar.getShell();
				behavior.dismissOnClickOutside = false;
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
