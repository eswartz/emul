/*
  EmulatorButtonBar.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.bars;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import v9t9.common.client.IMonitorEffectSupport;
import v9t9.common.client.ISoundHandler;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.gui.client.swt.ISwtVideoRenderer;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.imageimport.SwtImageImportSupport;
import v9t9.gui.client.swt.shells.ImageImportOptionsDialog;
import v9t9.gui.common.BaseEmulatorWindow;
import v9t9.gui.sound.SoundRecordingHelper;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

/**
 * This is the bar of command buttons on the right-hand side of the main emulator window.
 * This has the main controls for the emulator's state, video, and sound settings.
 * 
 * @author ejs
 *
 */
public class EmulatorButtonBar extends BaseEmulatorBar  {
	private SoundRecordingHelper soundRecordingHelper;
	private SoundRecordingHelper speechRecordingHelper;

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
				machine.notifyEvent(Level.INFO, "Hard reset, reloading");
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
		
		if (window.getVideoRenderer().getMonitorEffectSupport() != null) {
			BasicButton monitorEffectButton = createToggleStateButton(BaseEmulatorWindow.settingMonitorDrawing, 
					IconConsts.MONITOR_EFFECT,  
					IconConsts.CHECKMARK_OVERLAY, "Apply monitor effect to video");
			
			monitorEffectButton.addAreaHandler(new ImageButtonMenuAreaHandler(imageProvider));
			monitorEffectButton.addMenuDetectListener(new MenuDetectListener() {

				public void menuDetected(MenuDetectEvent e) {
					createMonitorEffectMenu(window.getVideoRenderer().getMonitorEffectSupport(), e);
				}
			});


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
				machine,
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
		

		imageImportButton.addAreaHandler(new ImageButtonMenuAreaHandler(imageProvider));
		imageImportButton.addMenuDetectListener(new MenuDetectListener() {

			public void menuDetected(MenuDetectEvent e) {
				ImageImportOptionsDialog.createImageImportMenu(window, imageSupport, e);
			}
		});

		
		createToggleStateButton(BaseEmulatorWindow.settingFullScreen, 
				IconConsts.FULLSCREEN, IconConsts.CHECKMARK_OVERLAY, "Toggle fullscreen");

		
		soundRecordingHelper = new SoundRecordingHelper(machine, 
				soundHandler.getSoundOutput(), 
				ISoundHandler.settingRecordSoundOutputFile, "sound");
		speechRecordingHelper = new SoundRecordingHelper(machine, 
				soundHandler.getSpeechOutput(), 
				ISoundHandler.settingRecordSpeechOutputFile, "speech");
		
		getButtonBar().addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				soundRecordingHelper.dispose();
				speechRecordingHelper.dispose();
			}
		});
		
		final BasicButton soundButton = createStateButton(ISoundHandler.settingPlaySound, 
				true, 
				IconConsts.SOUND_SPEAKER, IconConsts.NO_OVERLAY,
				true, "Sound options");
		
		soundButton.addAreaHandler(new ImageButtonMenuAreaHandler(imageProvider));
		soundButton.addMenuDetectListener(new MenuDetectListener() {

			public void menuDetected(MenuDetectEvent e) {
				createSoundMenu(machine, soundHandler, e);
			}
		});
		
		final IProperty soundRecording = machine.getSettings().get(ISoundHandler.settingRecordSoundOutputFile);
		final IProperty speechRecording = machine.getSettings().get(ISoundHandler.settingRecordSpeechOutputFile);
		final IProperty pauseRecordingProperty = machine.getSettings().get(ISoundHandler.settingPauseSoundRecording);
		
		final Rectangle recordingOverlayBounds = imageProvider.imageIndexToBounds(IconConsts.RECORD_OVERLAY);
		final Rectangle pauseOverlayBounds = imageProvider.imageIndexToBounds(IconConsts.PAUSE_OVERLAY);
		IPropertyListener recordingListener = new IPropertyListener() {
	
			public void propertyChanged(final IProperty setting) {
				Display.getDefault().asyncExec(new Runnable() {
	
					public void run() {
						if (soundButton.isDisposed())
							return;
						if (soundRecording.getString() != null || speechRecording.getString() != null) {
							soundButton.addImageOverlay(recordingOverlayBounds);
							if (pauseRecordingProperty.getBoolean())
								soundButton.addImageOverlay(pauseOverlayBounds);
							else
								soundButton.removeImageOverlay(pauseOverlayBounds);
						} else {
							soundButton.removeImageOverlay(recordingOverlayBounds);
							soundButton.removeImageOverlay(pauseOverlayBounds);
						}
						soundButton.redraw();
					}
				});
			}
		};
		soundRecording.addListenerAndFire(recordingListener);
		speechRecording.addListenerAndFire(recordingListener);
		pauseRecordingProperty.addListener(recordingListener);
		
		soundButton.addAreaHandler(new BaseImageButtonAreaHandler() {
			
			@Override
			public boolean isActive() {
				return soundRecording.getString() != null || speechRecording.getString() != null;
			}
			
			@Override
			public String getTooltip() {
				return pauseRecordingProperty.getBoolean() ? "Resume" : "Pause";
			}
			
			@Override
			public Rectangle getBounds(Point size) {
				return new Rectangle(0, 0, size.x/2, size.y/2);
			}
			
			/* (non-Javadoc)
			 * @see v9t9.gui.client.swt.bars.BaseImageButtonAreaHandler#mouseClicked(int)
			 */
			@Override
			public boolean mouseDown(int button) {
				if (button == 1) {
					pauseRecordingProperty.setBoolean(!pauseRecordingProperty.getBoolean());
					
					if (pauseRecordingProperty.getBoolean()) {
						
					}
					return true;
				}
				return false;
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
		soundRecordingHelper.populateSoundMenu(menu);
		speechRecordingHelper.populateSoundMenu(menu);
		
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

	/**
	 * @param machine
	 * @param soundHandler
	 * @param e
	 */
	private void createMonitorEffectMenu(IMonitorEffectSupport fxSupport, MenuDetectEvent e) {
		Control button = (Control) e.widget;
		Menu menu = new Menu(button);

		final IProperty monitorEffect = machine.getSettings().get(BaseEmulatorWindow.settingMonitorEffect);
				
		for (final String effectId : fxSupport.getIds()) {
			MenuItem item = new MenuItem(menu, SWT.RADIO);
			item.setText(fxSupport.getEffect(effectId).getLabel());
			if (effectId.equals(monitorEffect.getString()))
				item.setSelection(true);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					monitorEffect.setString(effectId);
				}

			});
		}
		swtWindow.showMenu(menu, null, e.x, e.y);
	}

}
