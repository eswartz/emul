/**
 * Dec 27, 2011
 */
package v9t9.gui.client.swt.bars;

import java.io.File;
import java.net.URI;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.ejs.gui.common.SwtDialogUtils;

import v9t9.common.cpu.ICpu;
import v9t9.common.demo.IDemoHandler;
import v9t9.common.demo.IDemoHandler.IDemoListener;
import v9t9.common.events.NotifyEvent;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.shells.CpuMetricsCanvas;
import v9t9.gui.client.swt.shells.debugger.DebuggerWindow;
import ejs.base.properties.IProperty;

/**
 * This is the bar of command buttons and status indicators for
 * use when developing the emulator or being a coder in general.
 * The bar is present only when the user explicitly enables it,
 * since it has some confusing or obscure commands inside.
 * 
 * @author ejs
 *
 */
public class EmulatorRnDBar extends BaseEmulatorBar  {
	private Canvas cpuMetricsCanvas;
	private IDemoListener demoListener;

	
	/**
	 * @param parent 
	 * @param isHorizontal 
	 * @param parent
	 * @param style
	 * @param focusRestorer
	 * @param smoothResize
	 */
	public EmulatorRnDBar(final SwtWindow window, ImageProvider imageProvider, Composite parent, 
			final IMachine machine,
			int[] colors, float[] points, boolean isHorizontal) {
		super(window, imageProvider, parent, machine, colors, points, isHorizontal);
		
		if (isHorizontal) {
			GridData gd = ((GridData) buttonBar.getLayoutData());
			gd.verticalSpan = 2;
		}
		
		buttonBar.addControlListener(new ControlAdapter() {
			@Override
			public void controlMoved(ControlEvent e) {
				swtWindow.recenterToolShells();
			}
			@Override
			public void controlResized(ControlEvent e) {
				swtWindow.recenterToolShells();
				buttonBar.setMaxIconSize(Math.max(16, Math.min(48, swtWindow.getShell().getSize().y / 8)));
			}
		});
		
		buttonBar.getDisplay().addFilter(SWT.MouseUp, new Listener() {
			
			public void handleEvent(Event event) {
				if (!(event.widget instanceof Control))
					return;
				if (((Control) event.widget).getShell() != swtWindow.getShell())
					return;
				if (event.button == 1) {
					Point pt = ((Control)event.widget).toDisplay(event.x, event.y);
					swtWindow.handleClickOutsideToolWindow(pt);
				}
			}
		});
		
		buttonBar.setMaxIconSize(48);
		buttonBar.setMinIconSize(16);

		createButton(IconConsts.INTERRUPT, "Send a non-maskable interrupt",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						machine.getCpu().nmi();
					}
				});

		createToggleStateButton(ICpu.settingDumpFullInstructions,
				IconConsts.CPU_LOGGING, 
				IconConsts.CHECKMARK_OVERLAY, "Toggle CPU logging");

		createButton(IconConsts.DEBUGGER,
				"Create debugger window", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						swtWindow.toggleToolShell(DebuggerWindow.DEBUGGER_TOOL_ID, 
								DebuggerWindow.getToolShellFactory(machine, buttonBar, swtWindow.getToolUiTimer()));
					}
			}
		);
		

		createDemoButton(IconConsts.DEMO,
				IconConsts.PLAY_OVERLAY, IconConsts.RECORD_OVERLAY,
				IconConsts.PAUSE_OVERLAY,
				"Play or record demo");


		cpuMetricsCanvas = new CpuMetricsCanvas(buttonBar.getComposite(), 
				SWT.BORDER | (isHorizontal ? SWT.HORIZONTAL : SWT.VERTICAL), 
				machine.getCpuMetrics(), true);
		GridDataFactory.fillDefaults()
			.align(isHorizontal ? SWT.LEFT : SWT.FILL, isHorizontal ? SWT.FILL : SWT.BOTTOM)
			//.grab(false, false)
			.indent(4, 4).exclude(true).applyTo(cpuMetricsCanvas);

	}
	
	/**
	 * @param iDemoHandler 
	 * @param demo
	 * @param playOverlay
	 * @param recordOverlay
	 * @param pauseOverlay
	 * @param string
	 */
	private BasicButton createDemoButton( 
			int demoIconIndex,
			final int playOverlay, final int recordOverlay,
			final int pauseOverlay, String tooltip) {
		
		final BasicButton button = new BasicButton(buttonBar, SWT.PUSH,
				imageProvider, demoIconIndex, tooltip);
		
		final IProperty pauseSetting = Settings.get(machine, IDemoHandler.settingDemoPaused);
		final IProperty recordSetting = Settings.get(machine, IDemoHandler.settingRecordDemo);
		final IProperty playSetting = Settings.get(machine, IDemoHandler.settingPlayingDemo);
		
		addSettingToggleListener(button, recordSetting, demoIconIndex, recordOverlay,
				true, false);
		
		addSettingToggleListener(button, playSetting, demoIconIndex, playOverlay,
				true, false);
		
		// the demo button controls pausing (when something active)
		// else triggers the menu
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (recordSetting.getBoolean() || playSetting.getBoolean()) {
					if (pauseSetting.getBoolean())
						button.setOverlayBounds(imageProvider.imageIndexToBounds(
								recordSetting.getBoolean() ? recordOverlay : playOverlay));
					else
						button.setOverlayBounds(imageProvider.imageIndexToBounds(pauseOverlay));
					
					pauseSetting.setBoolean(!pauseSetting.getBoolean());

					button.redraw();

				} else {
					// need to start playing or recording...
					IDemoHandler demoHandler = machine.getDemoHandler();
					if (demoHandler != null) {
						showDemoMenu(demoHandler, e, e.x, e.y,
								recordSetting, playSetting, pauseSetting);
					}
				}
			}
		});

		button.setMenuOverlayBounds(imageProvider.imageIndexToBounds(IconConsts.MENU_OVERLAY));
		button.addMenuDetectListener(new MenuDetectListener() {

			public void menuDetected(MenuDetectEvent e) {
				final IDemoHandler handler = machine.getDemoHandler();
				if (handler != null) {

					if (demoListener == null) {
						demoListener = new IDemoListener() {
							
							@Override
							public void stopped(NotifyEvent event) {
								try {
									handler.stopPlayback();
									handler.stopRecording();
								} catch (NotifyException ex) {
									//machine.getEventNotifier().notifyEvent(ex.getEvent());
								}
								machine.getEventNotifier().notifyEvent(event);
							}
						};
						
						handler.addListener(demoListener);
					}
					
					showDemoMenu(handler, e, e.x, e.y, 
							recordSetting, playSetting, pauseSetting);
				}
			}
		});


		// initialize state
		if (recordSetting.getBoolean() || playSetting.getBoolean()) {
			if (!pauseSetting.getBoolean()) {
				button.setOverlayBounds(imageProvider.imageIndexToBounds(
						recordSetting.getBoolean() ? recordOverlay : playOverlay));
			} else {
				button.setOverlayBounds(imageProvider.imageIndexToBounds(pauseOverlay));
			}
		}					
		button.setSelection(pauseSetting.getBoolean());
		
		return button;
		
	}

	private void showDemoMenu(final IDemoHandler demoHandler, TypedEvent e, int x, int y, 
			final IProperty recordSetting, final IProperty playSetting,
			final IProperty pauseSetting) {
		Control button = (Control) e.widget;
		final Menu menu = new Menu(button);
		
		final IProperty recordPath = Settings.get(machine, IDemoHandler.settingRecordedDemosPath);
		final IProperty searchPath = Settings.get(machine, IDemoHandler.settingDemosPath);
		
		String currentFilename = null;
		
		if (playSetting.getBoolean()) {
			URI uri = demoHandler.getPlaybackURI();
			currentFilename = String.valueOf(uri);
		}
		if (currentFilename != null) {
			final MenuItem stopItem = new MenuItem(menu, SWT.RADIO);
			stopItem.setText("Stop playing " + currentFilename);
			stopItem.setSelection(true);
			stopItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						demoHandler.stopPlayback();
					} catch (NotifyException ex) {
						machine.getEventNotifier().notifyEvent(ex.getEvent());
					}
				}

			});
		} 
		
		final MenuItem playItem = new MenuItem(menu, SWT.RADIO);
		if (recordSetting.getBoolean())
			playItem.setEnabled(false);
		playItem.setSelection(false);
		playItem.setText("Play demo...");
		playItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filename = SwtDialogUtils.openFileSelectionDialog(
						menu.getShell(),
						"Select demo file", 
						(String) (searchPath.getList().isEmpty() ? "/tmp" : searchPath.getList().get(0)), 
						null, false,
						IDemoHandler.DEMO_EXTENSIONS);
				if (filename != null) {
					File playFile = new File(filename);
					String parent = playFile.getParentFile().getAbsolutePath(); 
					if (!searchPath.getList().contains(parent)) {
						searchPath.getList().add(parent);
						searchPath.firePropertyChange();
					}
					
					try {
						demoHandler.startPlayback(playFile.toURI());
					} catch (NotifyException ex) {
						machine.getEventNotifier().notifyEvent(ex.getEvent());
					}
				}
			}
		});
		
		final MenuItem recordItem = new MenuItem(menu, SWT.RADIO);
		if (recordSetting.getBoolean()) {
			URI uri = demoHandler.getRecordingURI();
			currentFilename = String.valueOf(uri);
		} else {
			currentFilename = null;
		}
		if (currentFilename != null) {
			recordItem.setText("Stop recording " + currentFilename);
			recordItem.setSelection(true);
			recordItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						demoHandler.stopRecording();
					} catch (NotifyException ex) {
						machine.getEventNotifier().notifyEvent(ex.getEvent());
					}
				}

			});
		} else {
			if (playSetting.getBoolean())
				recordItem.setEnabled(false);
			recordItem.setSelection(false);
			recordItem.setText("Record demo...");
			recordItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String filenameBase = SwtDialogUtils.openFileSelectionDialog(
							menu.getShell(),
							"Record demo file",
							recordPath.getString(),
							"demo", true,
							IDemoHandler.DEMO_EXTENSIONS);
					File saveFile = null;
					if (filenameBase != null) {
						saveFile = SwtDialogUtils.getUniqueFile(filenameBase);
						if (saveFile == null) {
							SwtDialogUtils.showErrorMessage(menu.getShell(), "Save error", 
									"Too many demo files here!");
							return;
						}
						
						URI parent = saveFile.getParentFile().toURI();
						recordPath.setString(parent.toString());
						
						try {
							demoHandler.startRecording(saveFile.toURI());
						} catch (NotifyException ex) {
							machine.getEventNotifier().notifyEvent(ex.getEvent());
						}
					}
					
				}

			});
		}
		
		
		swtWindow.showMenu(menu, button, x, y);
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.bars.BaseEmulatorBar#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		
		cpuMetricsCanvas.dispose();

		
	}

}
