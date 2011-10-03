/**
 * AbstractCellPaintListener.java
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Abstract cell paint listener implementation
 */
public abstract class AbstractCellPaintListener implements Listener {

	private final int[] columns;

	/**
	 * Constructor.
	 * @param colums The valid columns.
	 * @param widget The widget.
	 */
	public AbstractCellPaintListener(Widget widget, int... columns) {
		this.columns = columns;
		register(widget);
	}

	/**
	 * Add this listener to the widget.
	 * @param widget The widget.
	 */
	protected void register(Widget widget) {
		if (widget != null && !widget.isDisposed()) {
			widget.addListener(SWT.MeasureItem, this);
			widget.addListener(SWT.PaintItem, this);
			widget.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					deregister(e.widget);
				}
			});
		}
	}

	/**
	 * Remove this listener from the widget.
	 * @param widget The widget.
	 */
	protected void deregister(Widget widget) {
		if (widget != null) {
			widget.removeListener(SWT.MeasureItem, this);
			widget.removeListener(SWT.PaintItem, this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void handleEvent(Event event) {
		// Check if the event type is handled.
		if (!isHandledEventType(event.type)) {
			return;
		}

		// We can paint something only if the item is available
		if (event.item instanceof Item) {
			// Check if the table item is valid
			if (isValid(event)) {
				// Forward to the event type specific handler methods
				switch (event.type) {
					case SWT.MeasureItem:
						doHandleMeasureItemEvent(event);
						break;
					case SWT.PaintItem:
						doHandlePaintItemEvent(event);
						break;
				}
			}
		}
	}

	/**
	 * Returns if or if not the given event type is handled by this handler.
	 * <p>
	 * The default implementation accepts {@link SWT#MeasureItem} and {@link SWT#PaintItem}.
	 *
	 * @param type The event type.
	 * @return <code>True</code> if the event type is handled by this handler, <code>false</code> otherwise.
	 */
	protected boolean isHandledEventType(int type) {
		return SWT.MeasureItem == type || SWT.PaintItem == type;
	}

	/**
	 * Handle the measure item event.
	 * @param event The event.
	 */
	protected void doHandleMeasureItemEvent(Event event) {
		Assert.isNotNull(event);

		// Get the image to draw.
		Image image = getImageToDraw((Item)event.item, event.index);
		// We have to measure anything only if the returned image is not null.
		if (image != null) {
			// Width must be minimum image width + 1 pixels at each side
			event.width = Math.max(event.width, image.getImageData().width + 2);
			// Height must be minimum image width + 1 pixels at each side
			event.height = Math.max(event.height, image.getImageData().height + 2);
		}
	}

	/**
	 * Handle the paint item event.
	 * @param event The event.
	 */
	protected void doHandlePaintItemEvent(Event event) {
		Assert.isNotNull(event);

		// Get the image to draw.
		Image image = getImageToDraw((Item)event.item, event.index);
		// We have to draw anything only if the returned image is not null.
		if (image != null) {
			Point point = getPaintOrigin(event, image);
			// Paint the image
			event.gc.drawImage(image, point.x, point.y);
		}
	}

	/**
	 * Get the origin where the image should be painted.
	 * @param event The event.
	 * @param image The image.
	 * @return The origin.
	 */
	protected abstract Point getPaintOrigin(Event event, Image image);

	/**
	 * Get the width of the widget where the image to draw into.
	 * @param event The event.
	 * @return The width.
	 */
	protected int getWidgetWidth(Event event) {
		if (event.widget instanceof Table) {
			TableColumn column = ((Table)event.widget).getColumn(event.index);
			return column.getWidth();
		}
		else if (event.widget instanceof Tree) {
			TreeColumn column = ((Tree)event.widget).getColumn(event.index);
			return column.getWidth();
		}
		return event.width;
	}

	/**
	 * Get the image to draw
	 * @param item The item to draw the image for.
	 * @param columnIndex The column index.
	 * @return The image or <code>null</code>.
	 */
	protected abstract Image getImageToDraw(Item item, int columnIndex);

	/**
	 * Returns if or if not the given events data is valid.
	 * Subclass implementers may check if the associated data object
	 * is matching and expected data type.
	 *
	 * @param event The event. Must not be <code>null</code>.
	 * @return <code>True</code> if the events data is valid, <code>false</code> otherwise.
	 */
	protected boolean isValid(Event event) {
		return (event.item != null && isPaintImageInColumn(event.index) &&
			((event.widget instanceof Table && event.item instanceof TableItem) ||
				(event.widget instanceof Tree && event.item instanceof TreeItem)));
	}

	/**
	 * Returns if or if not the managed image shall be painted to the
	 * given column.
	 *
	 * @param columnIndex The column index of the current column.
	 * @return <code>True</code> if the image shall be painted, <code>false</code> otherwise.
	 */
	protected boolean isPaintImageInColumn(int columnIndex) {
		for (int col : columns) {
			if (col == columnIndex) {
				return true;
			}
		}
		return false;
	}
}
