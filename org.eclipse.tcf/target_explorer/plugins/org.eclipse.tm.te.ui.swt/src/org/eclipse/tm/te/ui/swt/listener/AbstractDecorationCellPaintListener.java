/**
 * AbstractDecorationCellPaintListener.java
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
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

/**
 * Abstract decoration cell paint listener implementation
 */
public abstract class AbstractDecorationCellPaintListener extends AbstractCellPaintListener {

	public static final int STATE_NONE = IMessageProvider.NONE;
	public static final int STATE_INFO = IMessageProvider.INFORMATION;
	public static final int STATE_WARNING = IMessageProvider.WARNING;
	public static final int STATE_ERROR = IMessageProvider.ERROR;

	/**
	 * Constructor.
	 * @param colums The valid columns.
	 * @param widget The widget.
	 */
	public AbstractDecorationCellPaintListener(Widget widget, int... columns) {
		super(widget, columns);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.swt.listener.AbstractCellPaintListener#getPaintOrigin(org.eclipse.swt.widgets.Event, org.eclipse.swt.graphics.Image)
	 */
	@Override
	protected Point getPaintOrigin(Event event, Image image) {
		return new Point(event.x, event.y);
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
			int state = getDecorationState(item.getData(), columnIndex);
			if (state >= 0) {
				String decorationId = null;
				switch (state) {
					case STATE_INFO:
						decorationId = FieldDecorationRegistry.DEC_INFORMATION;
						break;
					case STATE_WARNING:
						decorationId = FieldDecorationRegistry.DEC_WARNING;
						break;
					case STATE_ERROR:
						decorationId = FieldDecorationRegistry.DEC_ERROR;
						break;
				}
				if (decorationId != null) {
					// Get the field decoration
					FieldDecoration fieldDeco = FieldDecorationRegistry.getDefault().getFieldDecoration(decorationId);
					if (fieldDeco != null) {
						return fieldDeco.getImage();
					}
				}
			}
		}

		return null;
	}

	/**
	 * Returns the state for the decoration.
	 *
	 * @param data The associated event data or <code>null</code>:
	 * @param columnIndex The column index of the current column.
	 *
	 * @return The state for the decoration.
	 */
	protected abstract int getDecorationState(Object data, int columnIndex);

}
