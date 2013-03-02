/*
  BaseEmulatorBar.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.bars;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

import v9t9.common.machine.IMachine;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;
import v9t9.gui.client.swt.SwtWindow;

/**
 * @author ejs
 *
 */
public abstract class BaseEmulatorBar {

	protected final SwtWindow swtWindow;
	protected ImageBar buttonBar;
	protected final IMachine machine;
	protected final ImageProvider imageProvider;

	public BaseEmulatorBar(SwtWindow window, ImageProvider imageProvider, Composite parent, 
			IMachine machine, int[] colors, float[] points, int style) {
		this.swtWindow = window;
		this.imageProvider = imageProvider;
		this.machine = machine;
		
		buttonBar = new ImageBar(parent, 
				style,
				new Gradient((style & SWT.HORIZONTAL) == 0, colors, points),
				swtWindow.getFocusRestorer(), true);
		
	}

	protected BasicButton createButton(int iconIndex, String tooltip, SelectionListener selectionListener) {
		BasicButton button = new BasicButton(buttonBar, SWT.PUSH,
				imageProvider, iconIndex, tooltip);
		button.addSelectionListener(selectionListener);
		return button;
	}

	protected BasicButton createStateButton(final SettingSchema schema, final boolean inverted, 
			final int iconIndex,
			final int secondIconIndex, final boolean isSecondOverlay, 
			String tooltip) {
		final BasicButton button = new BasicButton(buttonBar, SWT.PUSH,
				imageProvider, iconIndex, tooltip);
		final IProperty setting = Settings.get(machine, schema);
		addSettingToggleListener(button, setting, iconIndex, secondIconIndex,
				isSecondOverlay, inverted);
		
		addButtonToggleListener(button, setting);
		
		Rectangle secondOverlayBounds = imageProvider.imageIndexToBounds(secondIconIndex);
		if (isSecondOverlay) {
			if (setting.getBoolean() != inverted) {
				button.addImageOverlay(secondOverlayBounds);
				button.setSelection(setting.getBoolean());
			}
		} else {
			if (setting.getBoolean() != inverted) {
				button.setIconIndex(secondIconIndex);
				button.removeImageOverlay(secondOverlayBounds);
			} else {
				button.setIconIndex(iconIndex);
				button.removeImageOverlay(secondOverlayBounds);
			}
			button.setSelection(setting.getBoolean());
		}
		return button;
	}

	protected void addButtonToggleListener(final BasicButton button,
			final IProperty setting) {
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				machine.asyncExec(new Runnable() {
					public void run() {
						setting.setBoolean(!setting.getBoolean());
					}
				});
			}
		});
	}

	protected void addSettingToggleListener(final BasicButton button,
			final IProperty setting, final int iconIndex,
			final int secondIconIndex, final boolean isSecondOverlay,
			final boolean inverted) {
		final Rectangle secondOverlayBounds = imageProvider.imageIndexToBounds(secondIconIndex);
		setting.addListenerAndFire(new IPropertyListener() {
	
			public void propertyChanged(final IProperty setting) {
				Display.getDefault().asyncExec(new Runnable() {
	
					public void run() {
						if (button.isDisposed())
							return;
						if (isSecondOverlay) {
							if (setting.getBoolean() != inverted) {
								button.addImageOverlay(secondOverlayBounds);
							} else
								button.removeImageOverlay(secondOverlayBounds);
						}
						else {
							if (setting.getBoolean() != inverted)
								button.setIconIndex(secondIconIndex);
							else
								button.setIconIndex(iconIndex);
						}
						if (setting.getBoolean() != button.getSelection()) {
							button.setSelection(setting.getBoolean());
						}
						button.redraw();
					}
					
				});
			}
			
		});
	}

	protected BasicButton createToggleStateButton(final SettingSchema schema, int iconIndex, int overlayIndex,
			String tooltip) {
		return createStateButton(schema, false, iconIndex, overlayIndex, true, tooltip);
	}


	protected BasicButton createTwoStateButton(final SettingSchema schema, int iconIndex, int secondIconIndex,
			String tooltip) {
		return createStateButton(schema, false, iconIndex, secondIconIndex, false, tooltip);
	}

	public Point getTooltipLocation() {
		Point pt = buttonBar.getParent().toDisplay(buttonBar.getLocation());
		//System.out.println(pt);
		pt.y += buttonBar.getSize().y;
		pt.x += buttonBar.getSize().x * 3 / 4;
		return pt;
	}

	public ImageBar getButtonBar() {
		return buttonBar;
	}

	public void dispose() {
		
	}

}