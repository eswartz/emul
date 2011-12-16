/**
 * AbstractCheckBoxCellPaintListener.java
 * Created on Aug 21, 2011
 *
 * Copyright (c) 2011 Wind River Systems, Inc.
 *
 * The right to copy, distribute, modify, or otherwise make use
 * of this software may be licensed only pursuant to the terms
 * of an applicable Wind River license agreement.
 */
package org.eclipse.tm.te.ui.swt.listener;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.tm.te.ui.swt.SWTControlUtil;

/**
 * Abstract check box cell paint listener implementation.
 */
public abstract class AbstractCheckBoxCellPaintListener extends AbstractCellPaintListener {

	private static final int ENABLED = 1;
	private static final int CHECKED = 2;
	private static final int TRISTATE = 4;

	protected static final int STATE_NONE = -1;
	protected static final int STATE_ENABLED_CHECKED = ENABLED | CHECKED;
	protected static final int STATE_ENABLED_UNCHECKED = ENABLED;
	protected static final int STATE_ENABLED_TRISTATE = ENABLED | TRISTATE;
	protected static final int STATE_DISABLED_CHECKED = CHECKED;
	protected static final int STATE_DISABLED_UNCHECKED = 0;
	protected static final int STATE_DISABLED_TRISTATE = TRISTATE;

	/**
	 * Constructor.
	 *
	 * @param columns The valid columns.
	 * @param widget The widget.
	 */
	public AbstractCheckBoxCellPaintListener(Widget widget, int... columns) {
		super(widget, columns);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.swt.listener.AbstractCellPaintListener#getPaintOrigin(org.eclipse.swt.widgets.Event, org.eclipse.swt.graphics.Image)
	 */
	@Override
	protected Point getPaintOrigin(Event event, Image image) {
		// Determine host platform
		boolean isWindowsHost = System.getProperty("os.name","").toLowerCase().startsWith("windows"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// Center the image horizontally within the column
		int x = event.x + (getWidgetWidth(event)/2 - image.getImageData().width / 2);
		// Center the image vertically within the column
		int y = isWindowsHost ? event.y : event.y + event.height - (image.getImageData().height + 4);
		return new Point(x, y);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.swt.listener.AbstractCellPaintListener#getImageToDraw(org.eclipse.swt.widgets.Item, int)
	 */
	@Override
	protected Image getImageToDraw(Item item, int columnIndex) {
		Assert.isNotNull(item);

		// If the image shall be painted to the current column
		if (isPaintImageInColumn(columnIndex)) {
			// Check which image to paint
			int state = getCheckBoxState(item.getData(), columnIndex);
			if (state >= 0) {
				return SWTControlUtil.getCheckBoxImage((state & CHECKED) > 0, (state & TRISTATE) > 0, (state & ENABLED) > 0);
			}
		}

		return null;
	}

	/**
	 * Returns the state for the check box.
	 *
	 * @param data The associated event data or <code>null</code>:
	 * @param columnIndex The column index of the current column.
	 *
	 * @return The state for the check box.
	 */
	protected abstract int getCheckBoxState(Object data, int columnIndex);

}
