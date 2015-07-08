/*
  EmulatorButtonBar.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.bars;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TypedEvent;
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
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.common.sound.ISoundGenerator;
import v9t9.common.sound.ISoundRecordingHelper;
import v9t9.common.sound.MultiSoundOutputHandler;
import v9t9.common.sound.SoundRecordingHelper;
import v9t9.gui.client.swt.ISwtVideoRenderer;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.imageimport.SwtImageImportSupport;
import v9t9.gui.client.swt.shells.ImageImportOptionsDialog;
import v9t9.gui.common.BaseEmulatorWindow;
import v9t9.gui.sound.ISwtSoundRecordingHelper;
import v9t9.gui.sound.SwtSoundRecordingHelper;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.sound.ISoundOutput;
import ejs.base.utils.TextUtils;

/**
 * This is the bar of command buttons on the right-hand side of the main emulator window.
 * This has the main controls for the emulator's state, video, and sound settings.
 * 
 * @author ejs
 *
 */
public class EmulatorButtonBar extends BaseEmulatorBar  {
	private MultiSoundOutputHandler multiSoundHandler;

	/**
	 * @param parent 
	 * @param isHorizontal 
	 * @param parent
	 * @param style
	 * @param focusRestorer
	 * @param smoothResize
	 */
	public EmulatorButtonBar(final SwtWindow window, IImageProvider imageProvider_, Composite parent, 
			final IMachine machine,
			int[] colors, float[] points, int style) {
		super(window, imageProvider_, parent, machine, colors, points, style);
		
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
		
		final BasicButton loadSave = createButton(IconConsts.LOAD_SAVE_STATE, "Load or save machine state");
		
		final Rectangle toUpperLeftBounds = imageProvider.imageIndexToBounds(IconConsts.TO_UPPER_LEFT);
		final Rectangle toLowerRightBounds = imageProvider.imageIndexToBounds(IconConsts.TO_LOWER_RIGHT);

		final BaseImageButtonAreaHandler loadHandler = new BaseImageButtonAreaHandler() {

			@Override
			public boolean isInBounds(int x, int y, Point size) {
				return x + (size.x - y) > size.x;
			}

			@Override
			public boolean isActive() {
				return true;
			}

			@Override
			public String getTooltip() {
				return "Load state";
			}
			
			/* (non-Javadoc)
			 * @see v9t9.gui.client.swt.bars.BaseImageButtonAreaHandler#mouseEnter()
			 */
			@Override
			public void mouseEnter() {
				super.mouseEnter();
				loadSave.addImageOverlay(toUpperLeftBounds);
			}
			
			/* (non-Javadoc)
			 * @see v9t9.gui.client.swt.bars.BaseImageButtonAreaHandler#mouseExit()
			 */
			@Override
			public void mouseExit() {
				loadSave.removeImageOverlay(toUpperLeftBounds);
				super.mouseExit();
			}

			
		};
		final BaseImageButtonAreaHandler saveHandler = new BaseImageButtonAreaHandler() {
			
			@Override
			public boolean isInBounds(int x, int y, Point size) {
				return x + (size.x - y) < size.x;
			}
			
			@Override
			public boolean isActive() {
				return true;
			}
			
			@Override
			public String getTooltip() {
				return "Save state";
			}
			
			/* (non-Javadoc)
			 * @see v9t9.gui.client.swt.bars.BaseImageButtonAreaHandler#mouseEnter()
			 */
			@Override
			public void mouseEnter() {
				super.mouseEnter();
				loadSave.addImageOverlay(toLowerRightBounds);
			}
			
			/* (non-Javadoc)
			 * @see v9t9.gui.client.swt.bars.BaseImageButtonAreaHandler#mouseExit()
			 */
			@Override
			public void mouseExit() {
				loadSave.removeImageOverlay(toLowerRightBounds);
				super.mouseExit();
			}
		};
		
		loadSave.addAreaHandler(loadHandler);
		loadSave.addAreaHandler(saveHandler);

		loadSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (loadHandler.isInBounds(e.x, e.y, loadSave.getSize())) {
					swtWindow.loadMachineState();
				}
				else if (saveHandler.isInBounds(e.x, e.y, loadSave.getSize())) {
					swtWindow.saveMachineState();
				}
			}
		});

		if (window.getVideoRenderer().getMonitorEffectSupport() != null) {
			BasicButton monitorEffectButton = createToggleStateButton(BaseEmulatorWindow.settingMonitorDrawing, 
					IconConsts.MONITOR_EFFECT,  
					IconConsts.CHECKMARK_OVERLAY, "Apply monitor effect to video");
			
			monitorEffectButton.addAreaHandler(new ImageButtonMenuAreaHandler(imageProvider,
					new IMenuHandler() {
						
						@Override
						public void fillMenu(Menu menu) {
							fillMonitorEffectMenu(
									window.getVideoRenderer().getMonitorEffectSupport(),
									menu
									);
						}
					}
					));
		}
		
		ImageButton screenshotButton = createButton(IconConsts.SCREENSHOT, "Take screenshot",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						takeScreenshot(e, false);
					}
			});
		screenshotButton.addAreaHandler(new ImageButtonMenuAreaHandler(imageProvider,
				new IMenuHandler() {
					
					@Override
					public void fillMenu(Menu menu) {
						String cur = machine.getSettings().get(BaseEmulatorWindow.settingScreenShotsBase).getString();
						if (!TextUtils.isEmpty(cur)) {
							MenuItem current = new MenuItem(menu, SWT.NONE);
							current.setEnabled(false);
							current.setText("Base path: " + cur);
						}
						
						MenuItem saveAs = new MenuItem(menu, SWT.PUSH);
						saveAs.setText("Save as...");
						saveAs.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								takeScreenshot(e, true);
							}
						});
						
						final IProperty prop = machine.getSettings().get(
								BaseEmulatorWindow.settingScreenshotPlain);

						MenuItem plainBitmap = new MenuItem(menu, SWT.CHECK);
						plainBitmap.setText("Save plain bitmap");
						plainBitmap.setSelection(prop.getBoolean());
						
						plainBitmap.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								prop.setBoolean(!prop.getBoolean());
								
							}
						}); 
						
					}
				}));

		
	

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
		

		imageImportButton.addAreaHandler(new ImageButtonMenuAreaHandler(imageProvider,
				new IMenuHandler() {
					
					@Override
					public void fillMenu(Menu menu) {
						ImageImportOptionsDialog.fillImageImportMenu(window, imageSupport, menu); 
					}
				}));

		
		createToggleStateButton(BaseEmulatorWindow.settingFullScreen, 
				IconConsts.FULLSCREEN, IconConsts.CHECKMARK_OVERLAY, "Toggle fullscreen");

		multiSoundHandler = new MultiSoundOutputHandler(machine);
		ISoundHandler handler = machine.getClient().getSoundHandler();
		for (Map.Entry<ISoundGenerator, ISoundOutput> ent : handler.getGeneratorToOutputMap().entrySet()) {
			ISoundGenerator generator = ent.getKey();
			if (generator.getRecordingSettingSchema() == null)
				continue;
			
			SoundRecordingHelper helper = new SwtSoundRecordingHelper(machine, ent.getValue(), 
					generator.getRecordingSettingSchema(), 
					generator.getName(),
					generator.getAudioFormat(),
					generator.isSilenceRecorded());
			multiSoundHandler.register(helper);
		}

		getButtonBar().addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				multiSoundHandler.dispose();
			}
		});
		
		final BasicButton soundButton = createStateButton(ISoundHandler.settingPlaySound, 
				true, 
				IconConsts.SOUND_SPEAKER, IconConsts.NO_OVERLAY,
				true, "Sound options");
		
		soundButton.addAreaHandler(new ImageButtonMenuAreaHandler(imageProvider,
				new IMenuHandler() {
					
					@Override
					public void fillMenu(Menu menu) {
						fillSoundMenu(machine, menu);
					}
				}));
		
		final IProperty pauseRecordingProperty = machine.getSettings().get(ISoundHandler.settingPauseSoundRecording);
		
		final Rectangle recordingOverlayBounds = imageProvider.imageIndexToBounds(IconConsts.RECORD_OVERLAY);
		final Rectangle pauseOverlayBounds = imageProvider.imageIndexToBounds(IconConsts.PAUSE_OVERLAY);
		IPropertyListener recordingListener = new IPropertyListener() {
	
			public void propertyChanged(final IProperty setting) {
				Display.getDefault().asyncExec(new Runnable() {
	
					public void run() {
						if (soundButton.isDisposed())
							return;
						if (multiSoundHandler.isRecording()) {
							soundButton.addImageOverlay(recordingOverlayBounds);
							if (pauseRecordingProperty.getBoolean())
								soundButton.addImageOverlay(pauseOverlayBounds);
							else
								soundButton.removeImageOverlay(pauseOverlayBounds);
						} else {
							soundButton.removeImageOverlay(recordingOverlayBounds);
							soundButton.removeImageOverlay(pauseOverlayBounds);
						}
						//soundButton.redraw();
					}
				});
			}
		};
		
		multiSoundHandler.addListenerAndFire(recordingListener);
		pauseRecordingProperty.addListener(recordingListener);
		
		soundButton.addAreaHandler(new BaseImageButtonAreaHandler() {
			
			@Override
			public boolean isActive() {
				return multiSoundHandler.isRecording();
			}
			
			@Override
			public String getTooltip() {
				return (pauseRecordingProperty.getBoolean() ? "Resume recording to " : "Pause recording to ")
						+ TextUtils.catenateStrings(multiSoundHandler.getRecordings(), ", ");
			}
			
			/* (non-Javadoc)
			 * @see v9t9.gui.client.swt.bars.IImageButtonAreaHandler#isInBounds(int, int, org.eclipse.swt.graphics.Point)
			 */
			@Override
			public boolean isInBounds(int x, int y, Point size) {
				return x < size.x/2 && y < size.y/2;
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

	/**
	 * @param saveAs
	 */
	protected void takeScreenshot(TypedEvent e, boolean saveAs) {
		if (saveAs)
			machine.getSettings().get(BaseEmulatorWindow.settingScreenShotsBase).setValue("");

		swtWindow.screenshot();
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

	/**
	 * @param machine
	 * @param soundHandler
	 * @param e
	 */
	private void fillSoundMenu(final IMachine machine, Menu menu) {
		for (ISoundRecordingHelper helper : multiSoundHandler.getRecordingHelpers()) {
			if (helper instanceof ISwtSoundRecordingHelper)
				((ISwtSoundRecordingHelper) helper).populateSoundMenu(menu);
		}
		
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
	}

	/**
	 * @param machine
	 * @param soundHandler
	 * @param e
	 */
	private void fillMonitorEffectMenu(IMonitorEffectSupport fxSupport, Menu menu) {
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
	}

}
