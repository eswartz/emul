/*
  ImageDeviceIndicator.java

  (c) 2011-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.bars;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.machine.IMachine;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.shells.disk.DeviceSettingsDialog;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

/**
 * @author ejs
 *
 */
public class ImageDeviceIndicator extends ImageButton {

	private Rectangle overlayBounds;
	private final IDeviceIndicatorProvider provider;
	private IPropertyListener listener;

	public ImageDeviceIndicator(IImageBar parentDrawer, int style,
			IImageProvider imageProvider, final IDeviceIndicatorProvider provider,
			final IMachine machine, final SwtWindow window) {
		super(parentDrawer, style, imageProvider, provider.getBaseIconIndex(), provider.getToolTip());
		if (provider.getActiveIconIndex() >= 0)
			setOverlayBounds(imageProvider.imageIndexToBounds(provider.getActiveIconIndex()));
		this.provider = provider;
		listener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (!isDisposed())
							redraw();
					}
				});
			}
		};
		provider.getActiveProperty().addListenerAndFire(listener);
		
		final String[] groups = provider.getGroups();
		if (groups != null && groups.length > 0) {
			addAreaHandler(new BaseImageButtonAreaHandler() {

				@Override
				public boolean isActive() {
					return true;
				}
				
				@Override
				public String getTooltip() {
					return groups.length == 1 ? groups[0] : "Edit settings";
				}
				
				/* (non-Javadoc)
				 * @see v9t9.gui.client.swt.bars.IImageButtonAreaHandler#isInBounds(int, int, org.eclipse.swt.graphics.Point)
				 */
				@Override
				public boolean isInBounds(int x, int y, Point size) {
					return true;
				}
				
				/* (non-Javadoc)
				 * @see v9t9.gui.client.swt.bars.BaseImageButtonAreaHandler#mouseClicked(int)
				 */
				@Override
				public boolean mouseDown(int button) {
					if (button == 1) {
						window.toggleToolShell(DeviceSettingsDialog.DEVICE_SETTINGS_TOOL_ID, 
								DeviceSettingsDialog.getToolShellFactory(
										machine, window.getButtonBar().getButtonBar(),
										provider.getTitle(), groups));

						return true;
					}
					return false;
				}
			});
		}

		
		final ControlListener resizeListener = new ControlListener() {

			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				Rectangle bounds = getParent().getClientArea();
				int height = bounds.height;
				Rectangle metrics = new Rectangle(bounds.x + (bounds.width - height), bounds.y,
						height, height);
				//System.out.println(metrics);
				setBounds(metrics);				
			}
			
		};
		getParent().addControlListener(resizeListener);

		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				getParent().removeControlListener(resizeListener);
			}
		});
	}

	public void dispose() {
		provider.getActiveProperty().removeListener(listener);
		
		super.dispose();
	}

	public void setOverlayBounds(Rectangle overlayBounds) {
		this.overlayBounds = overlayBounds; 
	}

	@Override
	protected void drawImage(PaintEvent e) {
		Rectangle drawRect = getBounds();
		Point po = parentDrawer.getPaintOffset();
		drawRect.x = po.x;
		drawRect.y = po.y;
		try {
			//System.out.println(bounds);
			imageProvider.drawImage(e.gc, 255, drawRect, bounds);
			if (overlayBounds != null && provider.getActiveProperty().getBoolean()) {
				imageProvider.drawImage(e.gc, 255, drawRect, overlayBounds);
			}
		} catch (IllegalArgumentException e2) {
			e2.printStackTrace();
		}
	}

	
}
