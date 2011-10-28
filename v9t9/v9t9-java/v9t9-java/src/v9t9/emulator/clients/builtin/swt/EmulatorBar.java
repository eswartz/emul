/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;
import org.ejs.coffee.core.properties.SettingProperty;

import v9t9.emulator.common.Machine;

/**
 * @author ejs
 *
 */
public class EmulatorBar {

	protected static final String MODULE_SELECTOR_TOOL_ID = "module.selector";
	protected static final String DISK_SELECTOR_TOOL_ID = "disk.selector";
	protected static final String DEBUGGER_TOOL_ID = "debugger";
	protected final SwtWindow swtWindow;
	protected ImageBar buttonBar;
	protected final Machine machine;
	protected final ImageProvider imageProvider;

	/**
	 * @param isHorizontal 
	 * @param midPoint 
	 * @param colors 
	 * @param machine2 
	 * @param parent 
	 * @param imageProvider2 
	 * @param window 
	 * 
	 */
	public EmulatorBar(SwtWindow window, ImageProvider imageProvider, Composite parent, Machine machine, int[] colors, float midPoint, boolean isHorizontal) {
		this.swtWindow = window;
		this.imageProvider = imageProvider;
		this.machine = machine;
		
		buttonBar = new ImageBar(parent, 
				isHorizontal ? SWT.HORIZONTAL : SWT.VERTICAL,
				new Gradient(!isHorizontal, colors, new float[] { midPoint, 1 - midPoint }),
				swtWindow.getFocusRestorer(), true);
		
	}

	protected BasicButton createButton(int iconIndex, String tooltip, SelectionListener selectionListener) {
		BasicButton button = new BasicButton(buttonBar, SWT.PUSH,
				imageProvider, iconIndex, tooltip);
		button.addSelectionListener(selectionListener);
		return button;
	}

	protected BasicButton createStateButton(final SettingProperty setting, final boolean inverted, final Point noClickCorner,
			int iconIndex, final int overlayIndex, String tooltip) {
		final BasicButton button = new BasicButton(buttonBar, SWT.PUSH,
				imageProvider, iconIndex, tooltip);
		setting.addListener(new IPropertyListener() {
	
			public void propertyChanged(final IProperty setting) {
				Display.getDefault().asyncExec(new Runnable() {
	
					public void run() {
						if (button.isDisposed())
							return;
						if (setting.getBoolean() != inverted) {
							button.setOverlayBounds(imageProvider.imageIndexToBounds(overlayIndex));
						} else {
							button.setOverlayBounds(null);
						}
						if (setting.getBoolean() != button.getSelection()) {
							button.setSelection(setting.getBoolean());
						}
						button.redraw();
					}
					
				});
			}
			
		});
		
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (noClickCorner != null) {
					if (e.x >= noClickCorner.x || e.y >= noClickCorner.y)
						return;
				}
				machine.asyncExec(new Runnable() {
					public void run() {
						setting.setBoolean(!setting.getBoolean());
					}
				});
			}
		});
		
		if (setting.getBoolean() != inverted) {
			button.setOverlayBounds(imageProvider.imageIndexToBounds(overlayIndex));
			button.setSelection(setting.getBoolean());
		}
		return button;
	}

	protected BasicButton createStateButton(final SettingProperty setting, int iconIndex, int overlayIndex,
			String tooltip) {
		return createStateButton(setting, false, null, iconIndex, overlayIndex, tooltip);
	}

	/**
	 * @return
	 */
	public Point getTooltipLocation() {
		Point pt = buttonBar.getParent().toDisplay(buttonBar.getLocation());
		//System.out.println(pt);
		pt.y += buttonBar.getSize().y;
		pt.x += buttonBar.getSize().x * 3 / 4;
		return pt;
	}

	/**
	 * @return
	 */
	public ImageBar getButtonBar() {
		return buttonBar;
	}

	/**
	 * 
	 */
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}