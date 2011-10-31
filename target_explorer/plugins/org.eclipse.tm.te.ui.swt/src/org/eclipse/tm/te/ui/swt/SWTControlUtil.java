/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.swt;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

/**
 * Utility providing convenience methods for use with SWT controls.
 */
public final class SWTControlUtil {

	/**
	 * Returns the text from the specified control. The method will return <code>null</code>
	 * if the control is <code>null</code> or has been already disposed.
	 *
	 * @param control The control to get the text from.
	 * @param The text currently set to the control or <code>null</code>.
	 */
	public static final String getText(Control control) {
		if (control != null && !control.isDisposed()) {
			if (control instanceof Button) {
				return ((Button)control).getText().trim();
			}
			if (control instanceof Combo) {
				return ((Combo)control).getText().trim();
			}
			if (control instanceof Group) {
				return ((Group)control).getText().trim();
			}
			if (control instanceof Label) {
				return ((Label)control).getText().trim();
			}
			if (control instanceof Link) {
				return ((Link)control).getText().trim();
			}
			if (control instanceof Text) {
				return ((Text)control).getText().trim();
			}
			if (control instanceof Decorations) {
				return ((Decorations)control).getText();
			}
		}
		return null;
	}

	/**
	 * Sets the given text to the specified control. The text will be not applied if the
	 * control is <code>null</code> or has been already disposed or if the given text itself
	 * is <code>null</code>.
	 *
	 * @param control The control the given text should be applied to.
	 * @param value The text to apply to the given control.
	 */
	public static final void setText(Control control, String value) {
		if (control != null && !control.isDisposed() && value != null) {
			String trimmedValue = value.trim();

			// Avoid triggering attached listeners if the value has not changed.
			String oldValue = getText(control);
			if (!trimmedValue.equals(oldValue)) {
				if (control instanceof Button) {
					((Button)control).setText(trimmedValue);
				}
				if (control instanceof Combo) {
					((Combo)control).setText(trimmedValue);
				}
				if (control instanceof Group) {
					((Group)control).setText(trimmedValue);
				}
				if (control instanceof Label) {
					((Label)control).setText(trimmedValue);
				}
				if (control instanceof Link) {
					((Link)control).setText(trimmedValue);
				}
				if (control instanceof Text) {
					((Text)control).setText(trimmedValue);
				}
				if (control instanceof Decorations) {
					((Decorations)control).setText(trimmedValue);
				}
			}
		}
	}

	/**
	 * Sets the given text to the specified control as tooltip. The tooltip text will be not
	 * applied if the control is <code>null</code> or has been already disposed.
	 *
	 * @param control The control the given text should be applied to.
	 * @param value The text to apply to the given control or <code>null</code> to reset the tooltip.
	 */
	public static final void setToolTipText(Control control, String value) {
		if (control != null && !control.isDisposed()) {
			control.setToolTipText(value);
		}
	}

	/**
	 * Sets the text of the control (Text, Combo) as tooltip if the text is
	 * not fully visible.
	 * @param control The control to set the tooltip for.
	 */
	public static final void setValueToolTip(Scrollable control) {
		if (control != null && !control.isDisposed()) {
			String text = null;
			int resize = 0;
			if (control instanceof Text) {
				text = ((Text)control).getText().trim();
				resize = control.getBorderWidth() * 2;
			}
			if (control instanceof Combo) {
				text = ((Combo)control).getText().trim();
				resize = (int)(control.getSize().y * 1.5) + control.getBorderWidth() * 2;
			}
			if (text != null) {
				GC gc = new GC(control);
				int width = 0;
				for (int i = 0; i < text.length(); i++) {
					width += gc.getAdvanceWidth(text.charAt(i));
				}
				// Show the tooltip _only_ if the text itself exceeds
				// the length of the control (partially shown within the control).
				if (width > (control.getClientArea().width - resize)) {
					control.setToolTipText(text);
				} else {
					control.setToolTipText(null);
				}
				gc.dispose();
			}
		}
	}

	/**
	 * Adds the given text to the specified control. The given text will be added in case it's not
	 * <code>null</code> and not empty and if the given control itself is not <code>null</code>
	 * and not disposed. If the given text is already within the controls drop down list, the text
	 * will <i>not</i> be added again to the list.
	 *
	 * @param control The control to add the given text to.
	 * @param value The text to add to the control.
	 */
	public static final void add(Control control, String value) {
		add(control, value, false);
	}

	/**
	 * Adds the given text to the specified control. The given text will be added in case it's not
	 * <code>null</code> and not empty and if the given control itself is not <code>null</code>
	 * and not disposed. If the given text is already within the controls drop down list, the text
	 * will <i>not</i> be added again to the list.
	 *
	 * @param control The control to add the given text to.
	 * @param value The text to add to the control.
	 * @param allowEmpty If <code>true</code>, empty values will be added to the control. Otherwise,
	 *                   empty values will be filtered out and not applied to the control.
	 */
	public static final void add(Control control, String value, boolean allowEmpty) {
		if (control != null && !control.isDisposed() && value != null) {
			if (!allowEmpty  && value.trim().length() == 0) {
				return;
			}
			if (control instanceof Combo) {
				Combo combo = ((Combo)control);
				if (combo.indexOf(value) == -1) {
					combo.add(value);
				}
			}
			if (control instanceof List) {
				List list = ((List)control);
				if (list.indexOf(value) == -1) {
					list.add(value);
				}
			}
		}
	}

	/**
	 * Adds the given text to the specified control at the given index. If the given index is negative,
	 * the item index will be set to <code>0</code>. The given text will be added in case it's not
	 * <code>null</code> and not empty and if the given control itself is not <code>null</code>
	 * and not disposed. If the given text is already within the controls drop down list, the text will
	 * <i>not</i> be added again to the list.
	 *
	 * @param control The control to add the given text to.
	 * @param value The text to add to the control.
	 * @param index The index of the item to add to the control.
	 */
	public static final void add(Control control, String value, int index) {
		add(control, value, index, false);
	}

	/**
	 * Adds the given text to the specified control at the given index. If the given index is negative,
	 * the item index will be set to <code>0</code>. The given text will be added in case it's not
	 * <code>null</code> and not empty and if the given control itself is not <code>null</code>
	 * and not disposed. If the given text is already within the controls drop down list, the text will
	 * <i>not</i> be added again to the list.
	 *
	 * @param control The control to add the given text to.
	 * @param value The text to add to the control.
	 * @param index The index of the item to add to the control.
	 * @param allowEmpty If <code>true</code>, empty values will be added to the control. Otherwise,
	 *                   empty values will be filtered out and not applied to the control.
	 */
	public static final void add(Control control, String value, int index, boolean allowEmpty) {
		if (control != null && !control.isDisposed() && value != null) {
			if (!allowEmpty  && value.trim().length() == 0) {
				return;
			}
			if (control instanceof Combo) {
				Combo combo = ((Combo)control);
				if (combo.indexOf(value) == -1) {
					if (index < 0) {
						index = 0;
					}
					combo.add(value, index);
				}
			}
			if (control instanceof List) {
				List list = ((List)control);
				if (list.indexOf(value) == -1) {
					if (index < 0) {
						index = 0;
					}
					list.add(value, index);
				}
			}
		}
	}

	/**
	 * Sets the enabled state of the given control to the given state. The state will be
	 * applied in case the given control is not <code>null</code> and not disposed.
	 *
	 * @param control The control to set the enabled state for.
	 * @param enabled <code>true</code> to enable the control, <code>false</code> otherwise.
	 */
	public static final void setEnabled(Control control, boolean enabled) {
		if (control != null && !control.isDisposed()) {
			control.setEnabled(enabled);
		}
	}

	/**
	 * Returns the enabled state of the given control. The method returns always <code>true</code>
	 * in case the given control is <code>null</code> or disposed.
	 *
	 * @param control The control to get the enabled state for.
	 * @return <code>true</code> if the control is enabled, <code>false</code> otherwise.
	 */
	public static final boolean isEnabled(Control control) {
		if (control != null && !control.isDisposed()) {
			return control.isEnabled();
		}
		return true;
	}

	/**
	 * Sets the visible state of the given control. The state will be applied in
	 * case the given control is not <code>null</code> and not disposed.
	 *
	 * @param control The control to set the visible state for.
	 * @param visible <code>True</code> to set the control visible, <code>false</code> otherwise.
	 */
	public static final void setVisible(Control control, boolean visible) {
		if (control != null && !control.isDisposed()) {
			control.setVisible(visible);
		}
	}

	/**
	 * Returns the visible state of the given control. The method returns always
	 * <code>true</code> in case the given control is <code>null</code> or disposed.
	 *
	 * @param control The control to set the visible state for.
	 * @param visible <code>True</code> to set the control visible, <code>false</code> otherwise.
	 */
	public static final boolean isVisible(Control control) {
		if (control != null && !control.isDisposed()) {
			return control.getVisible();
		}
		return true;
	}

	/**
	 * Returns the item count of the specified control. The method will return <code>0</code>
	 * in case the control is <code>null</code> or has been already disposed or does not
	 * support items.
	 *
	 * @param control The control to get the item count for.
	 * @return The number of items within the control or <code>-1</code>.
	 */
	public static final int getItemCount(Control control) {
		if (control != null && !control.isDisposed()) {
			if (control instanceof Combo) {
				return ((Combo)control).getItemCount();
			}
			if (control instanceof List) {
				return ((List)control).getItemCount();
			}
		}
		return -1;
	}

	/**
	 * Sets the specified items to the specified control.
	 *
	 * @param control The control to set the items to.
	 * @param items The array of items to set.
	 */
	public static final void setItems(Control control, String[] items) {
		if (control != null && !control.isDisposed() && items != null) {
			if (control instanceof Combo) {
				((Combo)control).setItems(items);
			}
			else if (control instanceof List) {
				((List)control).setItems(items);
			}
		}
	}

	/**
	 * Returns the items of the specified control. The method will return an empty array
	 * in case the control is <code>null</code> or has been already disposed or does not
	 * support items.
	 *
	 * @param control The control to get the items from.
	 * @return The array items or and empty array.
	 */
	public static final String[] getItems(Control control) {
		if (control != null && !control.isDisposed()) {
			if (control instanceof Combo) {
				return ((Combo)control).getItems();
			}
			if (control instanceof List) {
				return ((List)control).getItems();
			}
		}
		return new String[0];
	}

	/**
	 * Sets the text of the specified item of the specified control.
	 *
	 * @param control The control to set the item text.
	 * @param index The index of the item to change.
	 * @param value The new item text to apply.
	 */
	public static final void setItem(Control control, int index, String value) {
		if (control != null && !control.isDisposed() && value != null) {
			// The index must be within valid range
			if (index >= 0 && index < getItemCount(control)) {
				String trimmedValue = value.trim();

				// Avoid triggering attached listeners if the value has not changed.
				String oldValue = getItem(control, index);
				if (!trimmedValue.equals(oldValue)) {
					if (control instanceof Combo) {
						((Combo)control).setItem(index, trimmedValue);
					}
					if (control instanceof List) {
						((List)control).setItem(index, trimmedValue);
					}
				}
			}
		}
	}

	/**
	 * Returns the text of the item at the specified index of the specified control.
	 *
	 * @param control The control to query the item text from.
	 * @param index The index of the item to query.
	 * @return The item text or <code>null</code>.
	 */
	public static final String getItem(Control control, int index) {
		if (control != null && !control.isDisposed()) {
			// The index must be within valid range
			if (index >= 0 && index < getItemCount(control)) {
				if (control instanceof Combo) {
					return ((Combo)control).getItem(index).trim();
				}
				if (control instanceof List) {
					return ((List)control).getItem(index).trim();
				}
			}
		}
		return null;
	}

	/**
	 * Returns the selected item index of the specified control. The method will return <code>-1</code> in case
	 * the control is <code>null</code> or has been already disposed or does not support selections.
	 *
	 * @param control The control to get the selected item index for.
	 * @return The index of the selected item within the control or <code>-1</code>.
	 */
	public static final int getSelectionIndex(Control control) {
		if (control != null && !control.isDisposed()) {
			if (control instanceof Combo) {
				return ((Combo)control).getSelectionIndex();
			}
			if (control instanceof List) {
				return ((List)control).getSelectionIndex();
			}
			if (control instanceof Table) {
				return ((Table)control).getSelectionIndex();
			}
		}
		return -1;
	}

	/**
	 * Returns the selected item count of the specified control. The method will return <code>-1</code> in case
	 * the control is <code>null</code> or has been already disposed or does not support selections.
	 *
	 * @param control The control to get the selected item count for.
	 * @return The number of selected items within the control or <code>-1</code>.
	 */
	public static final int getSelectionCount(Control control) {
		if (control != null && !control.isDisposed()) {
			if (control instanceof List) {
				return ((List)control).getSelectionCount();
			}
			if (control instanceof Table) {
				return ((Table)control).getSelectionCount();
			}
			if (control instanceof Tree) {
				return ((Tree)control).getSelectionCount();
			}
		}
		return -1;
	}

	/**
	 * Removes all items of the specified control.
	 *
	 * @param control The control to remove all items from.
	 */
	public static final void removeAll(Control control) {
		if (control != null && !control.isDisposed()) {
			if (control instanceof Combo) {
				((Combo)control).removeAll();
			}
			if (control instanceof List) {
				((List)control).removeAll();
			}
			if (control instanceof Table) {
				((Table)control).removeAll();
			}
			if (control instanceof Tree) {
				((Tree)control).removeAll();
			}
		}
	}

	/**
	 * Returns the index of the given item for the specified control. The method will return
	 * <code>-1</code> in case the control is <code>null</code> or has been already disposed
	 * or does not support items.
	 *
	 * @param control The control to lookup the item index.
	 * @param item The item to lookup the index for.
	 * @return The item index if found or <code>-1</code>.
	 */
	public static final int indexOf(Control control, String item) {
		if (control != null && !control.isDisposed() && item != null) {
			if (control instanceof Combo) {
				return ((Combo)control).indexOf(item);
			}
			if (control instanceof List) {
				return ((List)control).indexOf(item);
			}
		}
		return -1;
	}

	/**
	 * Selects the item with the given item index for the specified control. The method will
	 * return immediately in case the control is <code>null</code> or has been already
	 * disposed or does not support items.
	 *
	 * @param control The control to select the given item.
	 * @param item The item to select.
	 */
	public static final void select(Control control, int index) {
		if (control != null && !control.isDisposed()) {
			if (index >= 0 && index < getItemCount(control)) {
				if (control instanceof Combo) {
					((Combo)control).select(index);
				}
				if (control instanceof List) {
					((List)control).select(index);
				}
			}
		}
		return;
	}

	/**
	 * Returns the buttons selection state.
	 *
	 * @param button The button to query the selection state for.
	 * @return <code>true</code> if the button specified is not <code>null</code> nor disposed and selected, <code>false</code> otherwise.
	 */
	public static final boolean getSelection(Button button) {
		if (button != null && !button.isDisposed()) {
			return button.getSelection();
		}
		return false;
	}

	/**
	 * Sets the buttons selection state to the given state.
	 *
	 * @param button The button to set the selection state for.
	 * @param selected The button selection state to set.
	 */
	public static final void setSelection(Button button, boolean selected) {
		if (button != null && !button.isDisposed()) {
			button.setSelection(selected);
		}
	}

	/**
	 * Returns the number of pixels corresponding to the height of the given
	 * number of characters.
	 * <p>
	 * This methods uses the static {@link Dialog#convertHeightInCharsToPixels(org.eclipse.swt.graphics.FontMetrics, int)}
	 * method for calculation.
	 * <p>
	 * @param chars The number of characters
	 * @return The corresponding height in pixels
	 */
	public static int convertHeightInCharsToPixels(Control control, int chars) {
		int height = 0;
		if (control != null && !control.isDisposed()) {
			GC gc = new GC(control);
			gc.setFont(JFaceResources.getDialogFont());
			height = Dialog.convertHeightInCharsToPixels(gc.getFontMetrics(), chars);
			gc.dispose();
		}

		return height;
	}

	/**
	 * Returns the number of pixels corresponding to the width of the given
	 * number of characters.
	 * <p>
	 * This methods uses the static {@link Dialog#convertWidthInCharsToPixels(org.eclipse.swt.graphics.FontMetrics, int)}
	 * method for calculation.
	 * <p>
	 * @param chars The number of characters
	 * @return The corresponding width in pixels
	 */
	public static int convertWidthInCharsToPixels(Control control, int chars) {
		int width = 0;
		if (control != null && !control.isDisposed()) {
			GC gc = new GC(control);
			gc.setFont(JFaceResources.getDialogFont());
			width = Dialog.convertWidthInCharsToPixels(gc.getFontMetrics(), chars);
			gc.dispose();
		}

		return width;
	}

	/**
	 * Sets the focus to the given control.
	 *
	 * @param control The control to set the focus to.
	 * @return <code>True</code> if the control got the focus, <code>false</code> otherwise.
	 */
	public static boolean setFocus(Control control) {
		if (control != null && !control.isDisposed()) {
			return control.setFocus();
		}
		return false;
	}

	/**
	 * Sets the given color as control foreground.
	 *
	 * @param control The control.
	 * @param color The color.
	 */
	public static void setForeground(Control control, Color color) {
		if (control != null && !control.isDisposed() && color != null) {
			control.setForeground(color);
		}
	}

	/**
	 * Sets the given color as control background.
	 *
	 * @param control The control.
	 * @param color The color.
	 */
	public static void setBackground(Control control, Color color) {
		if (control != null && !control.isDisposed() && color != null) {
			control.setBackground(color);
		}
	}

	/**
	 * Sets the given image as control background image.
	 *
	 * @param control The control.
	 * @param image The image.
	 */
	public static void setBackgroundImage(Control control, Image image) {
		if (control != null && !control.isDisposed() && image != null) {
			control.setBackgroundImage(image);
		}
	}

	/* --------------------------------------------------------------------
	 *
	 * Generate system specific checkbox images.
	 *
	 * -------------------------------------------------------------------- */

	private static final String CHECKED = "CHECKED"; //$NON-NLS-1$
	private static final String UNCHECKED = "UNCHECKED"; //$NON-NLS-1$
	private static final String GRAYED = "GRAYED"; //$NON-NLS-1$
	private static final String ENABLED = "ENABLED"; //$NON-NLS-1$
	private static final String DISABLED = "DISABLED"; //$NON-NLS-1$

	private static Image makeShot(Shell shell, boolean checked, boolean grayed, boolean enabled) {
		shell = new Shell(shell, SWT.NO_FOCUS | SWT.NO_TRIM);

		Color greenScreen = new Color(shell.getDisplay(), 222, 223, 224);
		shell.setBackground(greenScreen);

		Button button = new Button(shell, SWT.CHECK);
		button.setBackground(greenScreen);
		button.setSelection(grayed || checked);
		button.setEnabled(enabled);
		button.setGrayed(grayed);

		button.setLocation(0, 0);
		Point bsize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		button.setSize(bsize);
		shell.setSize(bsize);

		shell.open();
		GC gc = new GC(shell);
		Image image = new Image(shell.getDisplay(), bsize.x, bsize.y);
		gc.copyArea(image, 0, 0);
		gc.dispose();

		ImageData imageData = image.getImageData();
		imageData.transparentPixel = imageData.palette.getPixel(greenScreen
			.getRGB());
		image = new Image(shell.getDisplay(), imageData);
		shell.close();

		return image;
	}

	public static synchronized Image getCheckBoxImage(boolean checked, boolean grayed, boolean enabled) {
		String key = (SWTControlUtil.class.getName() + "_" + //$NON-NLS-1$
			((checked || grayed) ? CHECKED : UNCHECKED) + "_" + //$NON-NLS-1$
			(grayed ? GRAYED + "_" : "") + //$NON-NLS-1$ //$NON-NLS-2$
			(enabled ? ENABLED : DISABLED));

		Image image = JFaceResources.getImageRegistry().get(key);
		try {
			if (image == null || image.getImageData().data == null) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				image = makeShot(shell, checked||grayed, grayed, enabled);
				if (image != null && image.getImageData().data != null) {
					JFaceResources.getImageRegistry().put(key, image);
				}
			}
		}
		catch (Exception e) {
		}

		return image;
	}
}
