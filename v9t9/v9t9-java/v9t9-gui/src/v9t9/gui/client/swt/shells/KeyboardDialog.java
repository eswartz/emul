/**
 * 
 */
package v9t9.gui.client.swt.shells;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ejs.base.utils.Pair;

import v9t9.common.keyboard.IKeyboardListener;
import v9t9.common.keyboard.IKeyboardMapping;
import v9t9.common.keyboard.IKeyboardMapping.PhysKey;
import v9t9.common.keyboard.IKeyboardMode;
import v9t9.common.keyboard.IKeyboardModeListener;
import v9t9.common.keyboard.KeyboardConstants;
import v9t9.common.machine.IMachine;
import v9t9.gui.client.swt.bars.ImageBar;

/**
 * Shows a keyboard which allows input and shows converted keystrokes
 * @author ejs
 *
 */
public class KeyboardDialog extends Composite implements IKeyboardModeListener, IKeyboardListener {

	private static final int BASE_Y_SIZE = 8;
	private static final int BASE_X_SIZE = 8;
	
	public static final String KEYBOARD_TOOL_ID = "keyboard";

	public static IToolShellFactory getToolShellFactory(final IMachine machine, final ImageBar buttonBar) {
		return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "KeyboardWindowBounds";
				behavior.centering = null;
				behavior.centerOverControl = buttonBar;
				behavior.dismissOnClickOutside = false;
			}
			public Control createContents(Shell shell) {
				return new KeyboardDialog(shell, machine);
			}
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}

	
	private final IMachine machine;
	private IKeyboardMapping keyboardMapping;
	private PhysKey[] physKeys;
	private Map<PhysKey, Button> keyButtons;
	private Set<Integer> pressedKeycodes = new HashSet<Integer>();
	private IKeyboardMode currentMode;
	private int zoom = 1;
	private boolean buttonUpdateWaiting;
	
	public KeyboardDialog(Shell shell, IMachine machine_) {
		
		super(shell, SWT.NONE);
		
		this.machine = machine_;
		
		shell.setText("Keyboard");

		GridLayoutFactory.fillDefaults().applyTo(this);
		
		keyboardMapping = machine.getKeyboardMapping();
		
		if (keyboardMapping == null) {
			Label label = new Label(this, SWT.WRAP);
			label.setText("Sorry, no keyboard mapping known for this machine");
			
			GridDataFactory.fillDefaults().grab(true, true).applyTo(label);
			return;
		}
		
		physKeys = keyboardMapping.getPhysicalLayout();
		
		keyButtons = new HashMap<PhysKey, Button>();
		
		createKeys(this);

		machine.addKeyboardModeListener(this);
		machine.getKeyboardState().addKeyboardListener(this);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				machine.removeKeyboardModeListener(KeyboardDialog.this);
			}
		});
		
		keyboardModeChanged(machine.getKeyboardMode());
		updateButtons();

		layoutKeys();
		
	}

	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardModeListener#keyboardModeChanged(java.lang.String)
	 */
	@Override
	public synchronized void keyboardModeChanged(String modeId) {
		IKeyboardMode newMode = keyboardMapping.getMode(modeId);
		if (newMode != currentMode) {
			currentMode = newMode; 
			scheduleButtonUpdate();
		}
	}
		
	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardListener#joystickChangeEvent(int, byte)
	 */
	@Override
	public void joystickChangeEvent(int num, byte mask) {
		
	}
	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardListener#keyEvent(int, boolean)
	 */
	@Override
	public synchronized void keyEvent(Collection<Integer> keys, boolean pressed) {
		if (pressed)
			pressedKeycodes.addAll(keys);
		else
			pressedKeycodes.removeAll(keys);

		scheduleButtonUpdate();
	}
	/**
	 * 
	 */
	private synchronized void scheduleButtonUpdate() {
		if (!buttonUpdateWaiting && !isDisposed()) {
			buttonUpdateWaiting = true;
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (!isDisposed())
						updateButtons();
					synchronized (KeyboardDialog.this) {
						buttonUpdateWaiting = false;
					}
				}
			});
		}
		
	}

	/**
	 * 
	 */
	protected void updateButtons() {
		if (currentMode == null) {
			for (Button button : keyButtons.values()) {
				button.setText("");
			}
			return;
		}
		
		byte shiftLockMask = (byte) (machine.getKeyboardState().getShiftMask()
				| machine.getKeyboardState().getLockMask());
		
		Map<PhysKey, Pair<Integer, String>> shiftMap = currentMode.getShiftLockMaskMap(shiftLockMask);
		Map<PhysKey, Pair<Integer, String>> normalMap = currentMode.getShiftLockMaskMap((byte) 0);
		
		for (Map.Entry<PhysKey, Button> ent : keyButtons.entrySet()) {
			
			Pair<Integer, String> info = shiftMap.get(ent.getKey());
			if (info == null)
				info = normalMap.get(ent.getKey());
			if (info == null) {
				ent.getValue().setText("");
			} else {
				ent.getValue().setSelection(pressedKeycodes.contains(info.first));
				ent.getValue().setText(info.second);
			}
			
		}
	}
	
	/**
	 */
	private void createKeys(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		//GridLayoutFactory.fillDefaults().applyTo(composite);
		//composite.setLayout(new FillLayout());
		for (PhysKey key : physKeys) {
			Control control = createKey(composite, key);
			control.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		}
	}

	/**
	 * @param composite
	 * @param key
	 * @return
	 */
	private Control createKey(Composite parent, final PhysKey key) {
		final Button button = new Button(parent, SWT.BORDER | SWT.TOGGLE);
		
//		GridDataFactory.fillDefaults().span(key.width, key.height)
//			.minSize(BASE_X_SIZE*key.width, BASE_Y_SIZE*key.height)
//			.applyTo(label);
		//.indent(key.x * BASE_X_SIZE, key.y * BASE_Y_SIZE).		
		keyButtons.put(key, button);
		
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				byte shiftLockMask = (byte) (machine.getKeyboardState().getShiftMask()
						| machine.getKeyboardState().getLockMask());
				
				int keycode = currentMode.getKeycode(shiftLockMask, key);
				if (keycode != KeyboardConstants.KEY_UNKNOWN)
					machine.getKeyboardState().stickyApplyKey(keycode, button.getSelection());
			}
			
		});
		return button;
	}
	
	protected void layoutKeys() {
		for (PhysKey key : physKeys) {
			Control control = keyButtons.get(key);
			
			control.setLocation(key.x * BASE_X_SIZE * zoom, key.y * BASE_Y_SIZE * zoom);
			control.setSize(key.width * BASE_X_SIZE * zoom, key.height * BASE_Y_SIZE * zoom);

		}
	}

}
