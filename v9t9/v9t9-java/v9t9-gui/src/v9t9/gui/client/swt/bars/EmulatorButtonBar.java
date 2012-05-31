/**
 * Mar 11, 2011
 */
package v9t9.gui.client.swt.bars;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import v9t9.common.client.ISoundHandler;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.gui.client.swt.ISwtVideoRenderer;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.imageimport.SwtImageImportSupport;
import v9t9.gui.client.swt.shells.ImageImportOptionsDialog;
import v9t9.gui.client.swt.shells.SpeechDialog;
import v9t9.gui.common.BaseEmulatorWindow;
import v9t9.gui.sound.JavaSoundHandler;
import ejs.base.properties.IProperty;

/**
 * This is the bar of command buttons on the right-hand side of the main emulator window.
 * This has the main controls for the emulator's state, video, and sound settings.
 * 
 * @author ejs
 *
 */
public class EmulatorButtonBar extends BaseEmulatorBar  {
	/**
	 * @param parent 
	 * @param isHorizontal 
	 * @param parent
	 * @param style
	 * @param focusRestorer
	 * @param smoothResize
	 */
	public EmulatorButtonBar(final SwtWindow window, ImageProvider imageProvider, Composite parent, 
			final IMachine machine,
			final ISoundHandler soundHandler,
			int[] colors, float[] points, int style) {
		super(window, imageProvider, parent, machine, colors, points, style);
		
		if ((style & SWT.HORIZONTAL) != 0) {
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
		

		BasicButton reset = createButton(IconConsts.RESET, "Reset the computer",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						machine.getCpu().reset();
					}
				});
		reset.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				machine.reset();
			}
		});

		createToggleStateButton(IMachine.settingPauseMachine, 
				IconConsts.PAUSE,
				IconConsts.CHECKMARK_OVERLAY, "Pause machine");

		
		createButton(IconConsts.PASTE_KEYBOARD, "Paste into keyboard",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						swtWindow.pasteClipboardToKeyboard();
					}
			});
		
		createButton(IconConsts.LOAD_SAVE_STATE, "Load or save machine state",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Control button = (Control) e.widget;
						Point size = button.getSize();
						swtWindow.showMenu(createFilePopupMenu(button), button, size.x / 2, size.y / 2);
					}

			});
		
		if (window.getVideoRenderer().supportsMonitorEffect()) {
			createToggleStateButton(BaseEmulatorWindow.settingMonitorDrawing, IconConsts.MONITOR_EFFECT,  
					IconConsts.CHECKMARK_OVERLAY, "Apply monitor effect to video");
		}
		
		createButton(IconConsts.SCREENSHOT, "Take screenshot",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						File file = swtWindow.screenshot();
						if (file != null) {
							swtWindow.getEventNotifier().notifyEvent(e, Level.INFO, "Recorded screenshot to " + file);
						}
					}
			});
	

		final SwtImageImportSupport imageSupport = new SwtImageImportSupport(
				swtWindow.getEventNotifier(), swtWindow.getVideoRenderer());
		BasicButton imageImportButton = createButton(IconConsts.IMAGE_IMPORT, "Import image",
			new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					swtWindow.toggleToolShell(ImageImportOptionsDialog.IMAGE_IMPORTER_ID, 
							ImageImportOptionsDialog.getToolShellFactory(buttonBar, imageSupport, swtWindow));
				}
			}
		);
		//imageSupport.setImageImportDnDControl(imageImportButton);
		imageSupport.setImageImportDnDControl(((ISwtVideoRenderer) window.getVideoRenderer()).getControl());
		

		imageImportButton.setMenuOverlayBounds(imageProvider.imageIndexToBounds(IconConsts.MENU_OVERLAY));
		imageImportButton.addMenuDetectListener(new MenuDetectListener() {

			public void menuDetected(MenuDetectEvent e) {
				ImageImportOptionsDialog.createImageImportMenu(window, imageSupport, e);
			}
		});

		
		createToggleStateButton(BaseEmulatorWindow.settingFullScreen, 
				IconConsts.FULLSCREEN, IconConsts.CHECKMARK_OVERLAY, "Toggle fullscreen");

		
		final BasicButton soundButton = createStateButton(ISoundHandler.settingPlaySound, 
				true, 
				IconConsts.SOUND_SPEAKER, IconConsts.NO_OVERLAY,
				true, "Sound options");
		
		soundButton.setMenuOverlayBounds(imageProvider.imageIndexToBounds(IconConsts.MENU_OVERLAY));
		soundButton.addMenuDetectListener(new MenuDetectListener() {

			public void menuDetected(MenuDetectEvent e) {
				createSoundMenu(machine, soundHandler, e);
			}
		});

	}

	/*
	private Menu createZoomMenu(final Control parent) {
		final Menu menu = new Menu(parent);
		return populateZoomMenu(menu);
	}

	private Menu populateZoomMenu(final Menu menu) {
		int curZoom = videoRenderer.getZoom();
		int[] zooms = { 1, 2, 3, 4, 5, 6, 7, 8 };
		for (final int zoom : zooms) {
			MenuItem item = new MenuItem(menu, SWT.RADIO);
			item.setText("" + zoom);
			if (zoom == curZoom)
				item.setSelection(true);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setScreenZoom(zoom);
					EmulatorSettings.getInstance().getApplicationSettings().put("ZoomLevel", zoom);
				}

			});
		}
		return menu;
	}
	
	protected void setScreenZoom(int zoom) {
		System.out.println("Set zoom to " + zoom);
		videoRenderer.setZoom(zoom);
	}
	 */
	private Menu createFilePopupMenu(final Control parent) {
		final Menu menu = new Menu(parent);
		return swtWindow.populateFileMenu(menu);
	}


	/**
	 * @param machine
	 * @param soundHandler
	 * @param e
	 */
	private void createSoundMenu(final IMachine machine,
			final ISoundHandler soundHandler, MenuDetectEvent e) {
		Control button = (Control) e.widget;
		Menu menu = new Menu(button);
		if (soundHandler instanceof JavaSoundHandler) {
			JavaSoundHandler javaSoundHandler = (JavaSoundHandler) soundHandler;
			javaSoundHandler.getSoundRecordingHelper().populateSoundMenu(menu);
			javaSoundHandler.getSpeechRecordingHelper().populateSoundMenu(menu);
		}
		
		MenuItem speechItem = new MenuItem(menu, SWT.PUSH);
		speechItem.setText("Speech Options...");
		speechItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				swtWindow.toggleToolShell(SpeechDialog.SPEECH_DIALOG_TOOL_ID, 
						SpeechDialog.getToolShellFactory(buttonBar, machine, swtWindow));
			}
		});
		
		MenuItem vitem = new MenuItem(menu, SWT.CASCADE);
		vitem.setText("Volume");
		
		final Menu volumeMenu = new Menu(vitem);

		final IProperty soundVolume = Settings.get(machine, ISoundHandler.settingSoundVolume);
		int curVol = soundVolume.getInt();
		int[] vols = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		for (final int vol : vols) {
			MenuItem item = new MenuItem(volumeMenu, SWT.RADIO);
			item.setText("" + vol);
			if (vol == curVol)
				item.setSelection(true);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					soundVolume.setInt(vol);
				}

			});
		}
		vitem.setMenu(volumeMenu);
		swtWindow.showMenu(menu, null, e.x, e.y);
	}
}
