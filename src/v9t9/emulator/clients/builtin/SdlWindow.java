/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import sdljava.SDLException;
import sdljava.event.SDLEvent;
import sdljava.event.SDLExposeEvent;
import sdljava.event.SDLMouseButtonEvent;
import sdljava.event.SDLMouseMotionEvent;
import sdljava.event.SDLResizeEvent;
import sdljava.image.SDLImage;
import sdljava.video.SDLRect;
import sdljava.video.SDLSurface;
import sdljava.video.SDLVideo;
import sdljava.x.swig.SDLPressedState;
import sdljavax.gfx.SDLGfx;
import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.video.SdlVideoRenderer;
import v9t9.emulator.runtime.Executor;
import v9t9.engine.settings.Setting;

/**
 * @author Ed
 *
 */
public class SdlWindow extends BaseEmulatorWindow {

	private static final int BUTTON_HEIGHT = 64;
	private static final int BUTTONS_WIDTH = 64;
	private SDLSurface icons;
	private SDLSurface window;
	private List<SDLButton> buttons = new ArrayList<SDLButton>();
	private SDLRect screenRect;
	private SDLRect buttonsRect;
	private int screenWidth;
	private int screenHeight;
	private boolean fullExpose;
	private boolean resizePending;
	private SDLButton hoverButton;
	
	/** only for SWT dialogs */
	private Display _display;
	/** only for SWT dialogs */
	private Shell _shell;
	
	public SdlWindow(Machine machine) throws SDLException {
		super(machine);

		_display = Display.getDefault();
		_shell = new Shell(_display); 
		
		videoRenderer = new SdlVideoRenderer();
		((SdlVideoRenderer) videoRenderer).setSdlWindow(this);
		File iconsFile = new File("icons/icons.png");
		icons = SDLImage.load(iconsFile.getAbsolutePath());
		
		createButton(new SDLRect(0, 64, 64, 64), 
				"Send a NMI interrupt",
				new ButtonPressHandler() {

					public void pressed() {
						sendNMI();
					}
			
				});

		createButton(new SDLRect(0, 256, 64, 64), "Reset the computer",
				new ButtonPressHandler() {
					public void pressed() {
						sendReset();
					}
				});
		
		createToggleButton(Executor.settingDumpFullInstructions,
				new SDLRect(0, 128, 64, 64),
				new SDLRect(0, 0, 64, 64),
				"Toggle CPU logging");
		
		createButton(new SDLRect(0, 192, 64, 64), 
						"Paste into keyboard",
				new ButtonPressHandler() {
					public void pressed() {
						//pasteClipboardToKeyboard();
					}
				});
		
		createButton(new SDLRect(0, 384, 64, 64),
				"Save machine state",
				new ButtonPressHandler() {
					public void pressed() {
						saveMachineState();
					}

			});
		
		createButton(new SDLRect(0, 448, 64, 64),
				"Load machine state",
				new ButtonPressHandler() {
					public void pressed() {
						loadMachineState();
					}
			});

		createToggleButton(Machine.settingPauseMachine, 
				new SDLRect(0, 512, 64, 64),
				new SDLRect(0, 0, 64, 64),
				"Pause machine");
		
		((SdlVideoRenderer)videoRenderer).setDesiredScreenSize(256 * 3, 192 * 3);
		
	}
	
	@Override
	public void dispose() {
		super.dispose();
		_shell.dispose();
		_display.dispose();
	}
	
	protected Shell getShell() {
		return _shell;
	}
	
	protected void createButton(SDLRect imageRect, String tooltip,
			ButtonPressHandler buttonPressHandler) {
		SDLButton button = new SDLButton(icons,
				new SDLRect(0, buttons.size() * BUTTON_HEIGHT, 64, 64),
				imageRect, null,
				tooltip);
		button.setHandler(buttonPressHandler);
		buttons.add(button);
		
	}
	
	protected void createToggleButton(final Setting setting,
			SDLRect imageRect, final SDLRect checkRect,
			String tooltip) {
		final SDLButton button = new SDLButton(icons,
				new SDLRect(0, buttons.size() * BUTTON_HEIGHT, 64, 64),
				imageRect, setting.getBoolean() ? checkRect : null,
				tooltip);
		button.setHandler(new ButtonPressHandler() {

			public void pressed() {
				setting.setBoolean(!setting.getBoolean());
				if (setting.getBoolean()) {
					button.setCheckRect(checkRect);
				} else {
					button.setCheckRect(null);
				}
				redrawButton(button);
			}
			
		});
		buttons.add(button);
		
	}
	
	
	protected void redrawButton(SDLButton button) {
		SDLRect bounds = button.getBounds();
		try {
			handleExpose(bounds.x + buttonsRect.x, bounds.y + buttonsRect.y, 
					bounds.width, bounds.height);
		} catch (SDLException e) {
			e.printStackTrace();
		}
	}

	protected synchronized void resizeWindow() throws SDLException {
		int width = ((SdlVideoRenderer)videoRenderer).getDesiredWidth() + BUTTONS_WIDTH;
		int height = Math.max(((SdlVideoRenderer)videoRenderer).getDesiredHeight(), buttons.size() * BUTTON_HEIGHT);
		resizeWindow(width, height);
		resizePending = false;
		((SdlVideoRenderer) videoRenderer).setResizePending(false);
		
	}
	protected synchronized void resizeWindow(int width, int height) throws SDLException {
		// this is the actual size the user chose or that the WM selected

		screenWidth = width - BUTTONS_WIDTH;
		screenHeight = height;

		if (window != null && window.getSwigSurface() != null)
			window.freeSurface();
		
		
		window = SDLVideo.setVideoMode(width, height, 24,
				SDLVideo.SDL_SWSURFACE | SDLVideo.SDL_RESIZABLE);
		if (window == null || window.getSwigSurface() == null) {
			throw new SDLException("Could not resize window");
		}
		screenRect = new SDLRect(0, 0, screenWidth, screenHeight);
		buttonsRect = new SDLRect(screenWidth, 0, BUTTONS_WIDTH, height);		
		
		fullExpose = true;
		//handleExpose(0, 0, width, height);
	}
	
	public void handleExpose(SDLExposeEvent event) throws SDLException {
		if (window == null || window.getSwigSurface() == null)
			return;
		
		handleExpose(0, 0, window.getWidth(), window.getHeight());
	}

	public void handleResize(SDLResizeEvent event) throws SDLException {
		if (videoRenderer != null) {
			// this will call back and update the desired size
			((SdlVideoRenderer)videoRenderer).updateWidgetOnResize(
					event.getWidth() - BUTTONS_WIDTH, event.getHeight());
		}
		resizeWindow(event.getWidth(), event.getHeight());
	}

	public synchronized void handleExpose(int x, int y, int width, int height) throws SDLException {
		//System.out.println("Expose? " + x + "/" +y + "/"+width + "/" +height);
		if (resizePending || ((SdlVideoRenderer) videoRenderer).isResizePending()) {
			resizeWindow();
			return;
		}
		
		if (fullExpose) {
			x = y = 0;
			width = window.getWidth();
			height = window.getHeight();
			fullExpose = false;
		}
		
		//System.out.println("Propagating " + x + "/" +y + "/"+width + "/" +height);
		if (SdlUtils.pointInRect(buttonsRect, x, y)
				|| SdlUtils.pointInRect(buttonsRect, x + width - 1, y + height - 1)) {
			redrawButtons(Math.max(0, x - buttonsRect.x), Math.max(0, y - buttonsRect.y), 
					Math.max(0, x + width - buttonsRect.x), Math.max(0, y + height - buttonsRect.y));
		}
		if (SdlUtils.pointInRect(screenRect, x, y)
				|| SdlUtils.pointInRect(screenRect, x + width - 1, y + height - 1)) {
			redrawScreen(Math.max(0, x - screenRect.x), Math.max(0, y - screenRect.y), 
					Math.max(0, width - screenRect.width), Math.max(0, height - screenRect.height));
		}
		window.updateRect(x, y, width, height);
		
	}
	public void handleMouse(SDLEvent event) {
		int x,y ;
		if (event instanceof SDLMouseButtonEvent) {
			x = ((SDLMouseButtonEvent) event).getX();
			y = ((SDLMouseButtonEvent) event).getY();
		} else if (event instanceof SDLMouseMotionEvent) {
			x = ((SDLMouseMotionEvent) event).getX();
			y = ((SDLMouseMotionEvent) event).getY();
		} else {
			return;
		}
		if (SdlUtils.pointInRect(screenRect, x, y)) {
			handleScreenMouseEvent(x - screenRect.getX(), y - screenRect.getY(), 
					event);
		}
		else if (SdlUtils.pointInRect(buttonsRect, x, y)) {
			handleButtonMouseEvent(x - buttonsRect.getX(), y - buttonsRect.getY(), 
					event);
		} 
		else {
			System.out.println("Unknown mouse event: " + event + " at "+ x + "," + y);
		}
	}

	protected void handleButtonMouseEvent(int x, int y, SDLEvent event) {
		if (event.getType() == SDLEvent.SDL_MOUSEBUTTONDOWN) {
			int mbutton = ((SDLMouseButtonEvent) event).getButton();
			SDLPressedState state = ((SDLMouseButtonEvent) event).getState();
			if (mbutton == 1 && state == SDLPressedState.PRESSED) {
				for (SDLButton button : buttons) {
					if (SdlUtils.pointInRect(button.getBounds(), x, y)) {
						button.firePressed();
					}
				}
			}
		} else if (event.getType() == SDLEvent.SDL_MOUSEMOTION) {
			for (SDLButton button : buttons) {
				if (SdlUtils.pointInRect(button.getBounds(), x, y)) {
					if (hoverButton != null && hoverButton != button) {
						hoverButton.setHighlight(false);
						redrawButton(hoverButton);
					}
					hoverButton = button;
					hoverButton.setHighlight(true);
					redrawButton(hoverButton);
				}
			}
		}
	}
	protected void handleScreenMouseEvent(int x, int y, SDLEvent event) {
		if (hoverButton != null) {
			hoverButton.setHighlight(false);
			redrawButton(hoverButton);
			hoverButton = null;
		}
	}


	class SDLButton {
		private SDLRect bounds;
		private final SDLRect imageRect;
		private SDLRect checkRect;
		private final String tooltip;
		private final SDLSurface icons;
		private ButtonPressHandler buttonPressHandler;
		private boolean highlight;

		public SDLButton(SDLSurface icons, SDLRect bounds, SDLRect imageRect, SDLRect checkRect, 
				String tooltip) {
			this.icons = icons;
			this.bounds = bounds;
			this.imageRect = imageRect;
			this.checkRect = checkRect;
			this.tooltip = tooltip;
		}
		
		public void setHighlight(boolean b) {
			this.highlight = b;
		}

		public void setBounds(int x, int y, int width, int height) {
			this.bounds = new SDLRect(x, y, width, height);
		}

		public void setCheckRect(SDLRect checkRect) {
			this.checkRect = checkRect;
		}

		public void firePressed() {
			if (buttonPressHandler != null)
				buttonPressHandler.pressed();
		}

		public SDLRect getBounds() {
			return bounds;
		}

		public void setHandler(ButtonPressHandler buttonPressHandler) {
			this.buttonPressHandler = buttonPressHandler;
		}

		public void draw(SDLSurface composite, SDLRect buttonsRect) throws SDLException {
			SDLRect pos = SdlUtils.copyRect(bounds);
			pos.x += buttonsRect.x;
			pos.y += buttonsRect.y;

			if (highlight) {
				SDLGfx.rectangleRGBA(composite,
						pos.x, pos.y, pos.x + pos.width - 1, pos.y + pos.height - 1, 
						0, 0, 0, 255);
						
			}

			icons.blitSurface(imageRect, composite, pos);
			if (checkRect != null) {
				icons.blitSurface(checkRect, composite, pos);
			}
			
			if (highlight) {
				SDLGfx.stringRGBA(composite, pos.x, pos.y + pos.height - 8,
						tooltip, 0, 0, 0, 255);
			}
		}

		protected boolean isChecked() {
			return false;
		}
		
		
	}
	
	/**
	 * Redraw buttonsRect-relative rectangle.
	 */ 
	protected void redrawButtons(int x, int y, int width, int height) throws SDLException {
		if (buttonsRect.getX() + buttonsRect.getWidth() <= window.getWidth()) {
			SDLRect updateRect = new SDLRect(x, y, width, height); 
			SDLRect windowUpdateRect = new SDLRect(buttonsRect.x + x, buttonsRect.y + y, width, height); 
			//System.out.println("updateRect = " + windowUpdateRect + "; buttonsRect = "+ buttonsRect+"; window = "+ window.getWidth()+"x"+window.getHeight());
			window.lockSurface();
			window.fillRect(windowUpdateRect, window.mapRGB(0xe0, 0xe0, 0xe0));
			window.unlockSurface();
			for (SDLButton button : buttons) {
				if (SdlUtils.rectIntersectsRect(button.getBounds(), updateRect))
					button.draw(window, buttonsRect);
			}
		}
	}
	
	/**
	 * Redraw screenRect-relative rectangle.
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @throws SDLException
	 */
	protected void redrawScreen(int x, int y, int width, int height) throws SDLException {
		if (videoRenderer == null)
			return;
		synchronized (videoRenderer) {
			SdlVideoRenderer sdlVideoRenderer = (SdlVideoRenderer) videoRenderer;
			// add the current expose to the renderer for next time
			sdlVideoRenderer.update(new SDLRect(x, y, width, height));
			
			// get what it previously rendered
			SDLSurface screen = sdlVideoRenderer.getSurface();
			screen.blitSurface(window, SdlUtils.copyRect(screenRect));
			
		}
	}
	
	public SdlVideoRenderer getVideoRenderer() {
		return (SdlVideoRenderer) videoRenderer;
	}
	public SDLRect getScreenRect() {
		return screenRect;
	}

	@Override
	protected String openFileSelectionDialog(String title, String directory,
			String fileName, boolean isSave) {
		FileDialog dialog = new FileDialog(getShell(), isSave ? SWT.SAVE : SWT.OPEN);
		dialog.setFilterPath(directory);
		dialog.setFileName(fileName);
		String filename = dialog.open();
		return filename;
	}

	@Override
	protected void showErrorMessage(String title, String msg) {
		// TODO Auto-generated method stub
		
	}

	
}
