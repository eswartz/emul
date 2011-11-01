/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.controls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.te.ui.controls.nls.Messages;
import org.eclipse.tm.te.ui.controls.validator.Validator;
import org.eclipse.tm.te.ui.swt.SWTControlUtil;
import org.eclipse.tm.te.ui.wizards.interfaces.IValidatableWizardPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Base implementation of a common UI control providing an
 * editable field or combo box to the user with the additional capability
 * of browsing for the field value.
 */
public class BaseEditBrowseTextControl extends BaseDialogPageControl implements SelectionListener, ModifyListener {
	private boolean isGroup = true;
	private boolean hasHistroy = true;
	private boolean isReadOnly = false;
	private boolean labelIsButton = false;
	private int labelButtonStyle = SWT.RADIO;
	private boolean parentControlIsInnerPanel = false;
	private boolean hideBrowseButton = false;
	private boolean hideEditFieldControl = false;
	private boolean hideEditFieldControlDecoration = false;
	private boolean hideLabelControl = false;
	private boolean adjustBackgroundColor = false;
	boolean isInitializing = true;

	private String groupLabel = ""; //$NON-NLS-1$
	private String editFieldLabelTooltip = null;
	private String editFieldLabel = ""; //$NON-NLS-1$
	private String buttonLabel = Messages.BaseEditBrowseTextControl_button_label;

	private Composite innerPanel;
	private Control labelControl;
	private Control editFieldControl;
	private Button buttonControl;
	private ControlDecoration controlDecoration;

	private String dialogSettingsSlotId;

	private Validator editFieldValidator;

	/**
	 * Constructor.
	 *
	 * @param parentPage The parent dialog page this control is embedded in.
	 *                   Might be <code>null</code> if the control is not associated with a page.
	 */
	public BaseEditBrowseTextControl(IDialogPage parentPage) {
		super(parentPage);
	}

	/**
	 * Set if or if not the control should be enclosed in an group control.
	 *
	 * @param isGroup Specify <code>true</code> to enclose the control into a group control, <code>false</code> otherwise.
	 */
	public final void setIsGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	/**
	 * Returns if or if not the control is enclosed in an group control.
	 *
	 * @return <code>true</code> if the control is enclosed into a group control, <code>false</code> otherwise.
	 */
	public final boolean isGroup() {
		return isGroup;
	}

	/**
	 * Set if or if not this control should have a history or not.
	 *
	 * @param hasHistory Specify <code>true</code> if the control should have an history, <code>false</code> otherwise.
	 */
	public void setHasHistory(boolean hasHistory) {
		this.hasHistroy = hasHistory;
	}

	/**
	 * Returns if or if not this control has a history or not.
	 *
	 * @return <code>true</code> if the control should has an history, <code>false</code> otherwise.
	 */
	public final boolean hasHistory() {
		return hasHistroy;
	}

	/**
	 * Set if or if not this control can be edited by the user.
	 *
	 * @param readOnly Specify <code>true</code> if the control should be not editable by the user, <code>false</code> otherwise.
	 */
	public final void setReadOnly(boolean readOnly) {
		this.isReadOnly = readOnly;
	}

	/**
	 * Returns if or if not this control can be edited by the user.
	 *
	 * @return <code>true</code> if the control is editable by the user, <code>false</code> otherwise.
	 */
	public final boolean isReadOnly() {
		return isReadOnly;
	}

	/**
	 * Sets if of if not the label control should be hidden.
	 *
	 * @param hide <code>True</code> if to hide the label control, <code>false</code> otherwise.
	 */
	public final void setHideLabelControl(boolean hide) {
		this.hideLabelControl = hide;
	}

	/**
	 * Returns if or if not the label control is hidden.
	 *
	 * @return <code>True</code> if the label control is hidden, <code>false</code> otherwise.
	 */
	public final boolean isHideLabelControl() {
		return hideLabelControl;
	}

	/**
	 * Sets if of if not the edit field control should be hidden.
	 * <p>
	 * If set to <code>true</code>, the button control and the edit
	 * field control decoration are set to hidden automatically.
	 *
	 * @param hide <code>True</code> if to hide the edit field control, <code>false</code> otherwise.
	 */
	public final void setHideEditFieldControl(boolean hide) {
		this.hideEditFieldControl = hide;
		if (hide) {
			setHideBrowseButton(hide);
			setHideEditFieldControlDecoration(hide);
		}
	}

	/**
	 * Returns if or if not the edit field control is hidden.
	 *
	 * @return <code>True</code> if the edit field control is hidden, <code>false</code> otherwise.
	 */
	public final boolean isHideEditFieldControl() {
		return hideEditFieldControl;
	}


	/**
	 * Sets if of if not the edit field control decoration should be hidden.
	 *
	 * @param hide <code>True</code> if to hide the edit field control, <code>false</code> otherwise.
	 */
	public final void setHideEditFieldControlDecoration(boolean hide) {
		this.hideEditFieldControlDecoration = hide;
	}

	/**
	 * Returns if or if not the edit field control decoration is hidden.
	 *
	 * @return <code>True</code> if the edit field control is hidden, <code>false</code> otherwise.
	 */
	public final boolean isHideEditFieldControlDecoration() {
		return hideEditFieldControlDecoration;
	}

	/**
	 * Set if or if not the button behind the edit field control should be hidden.
	 *
	 * @param hide <code>true</code> to hide the button behind the edit field control, <code>false</code> otherwise.
	 */
	public final void setHideBrowseButton(boolean hide) {
		this.hideBrowseButton = hide;
	}

	/**
	 * Returns if or if not the button behind the edit field control is hidden or not.
	 *
	 * @return <code>true</code> if the button behind the edit field control is hidden, <code>false</code> otherwise.
	 */
	public final boolean isHideBrowseButton() {
		return hideBrowseButton;
	}

	/**
	 * Sets if to adjusts the background color of the created UI elements.
	 *
	 * @param adjust <code>True</code> to adjust the background color, <code>false</code> otherwise.
	 */
	public final void setAdjustBackgroundColor(boolean adjust) {
		this.adjustBackgroundColor = adjust;
	}

	/**
	 * Returns if to adjust the background color of the created UI elements.
	 *
	 * @return <code>True</code> to adjust the background color, <code>false</code> otherwise.
	 */
	public final boolean isAdjustBackgroundColor() {
		return adjustBackgroundColor;
	}

	/**
	 * Enables or disables all UI elements belonging to this control.
	 *
	 * @param enabled <code>true</code> to enable the UI elements, <code>false</code> otherwise.
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		SWTControlUtil.setEnabled(getLabelControl(), enabled);
		SWTControlUtil.setEnabled(getEditFieldControl(), enabled && (isLabelIsButton() ? isLabelControlSelected() : true));
		SWTControlUtil.setEnabled(getButtonControl(), enabled && (isLabelIsButton() ? isLabelControlSelected() : true));

		// Hide or show the control decoration if one is available
		if (getEditFieldControlDecoration() != null) {
			if (enabled && getEditFieldControlDecoration().getDescriptionText() != null) {
				getEditFieldControlDecoration().show();
			} else {
				getEditFieldControlDecoration().hide();
			}
		}
	}

	/**
	 * Sets all UI elements belonging to this control visible or not.
	 *
	 * @param visible <code>True</code> to set all UI controls visible, <code>false</code> otherwise.
	 */
	public void setVisible(boolean visible) {
		SWTControlUtil.setEnabled(getLabelControl(), visible);
		SWTControlUtil.setEnabled(getEditFieldControl(), visible);
		SWTControlUtil.setEnabled(getButtonControl(), visible);
	}

	/**
	 * Sets if or if not the parent control, passed by <code>setupPanel(parentControl)</code> should
	 * be used directly as the controls inner panel. The associated parent controls layout <b>must</b>
	 * be a <code>org.eclipse.swt.layout.GridLayout</code>. The assumed number of columns is <code>3</code>.
	 * If the panel contains more than <code>3</code> columns, the edit field controls horizontal span value
	 * should be adjusted using <code>setEditFieldControlLayoutHorizontalSpan</code>. In all other cases,
	 * a new independent inner panel will create even if <code>isParentControlIsInnerPanel()</code> returns <code>true</code>!
	 *
	 * @param parentIsInnerPanel <code>true</code> if the passed parent control is used directly as the inner panel, <code>false</code> otherwise.
	 */
	public final void setParentControlIsInnerPanel(boolean parentIsInnerPanel) {
		this.parentControlIsInnerPanel = parentIsInnerPanel;
	}

	/**
	 * Returns if or if not the parent control, passed by <code>setupPanel(parentControl)</code> is
	 * used directly as the controls inner panel.
	 *
	 * @param parentIsInnerPanel <code>true</code> if the passed parent control is used directly as the inner panel, <code>false</code> otherwise.
	 */
	public final boolean isParentControlIsInnerPanel() {
		return parentControlIsInnerPanel;
	}

	/**
	 * Sets the label to use for the enclosing group. If <code>null</code>, the
	 * group label is set to an empty string.
	 *
	 * @param groupLabel The group label to use for the enclosing group or <code>null</code>.
	 */
	public final void setGroupLabel(String groupLabel) {
		if (groupLabel != null) {
			this.groupLabel = groupLabel;
		} else {
			this.groupLabel = ""; //$NON-NLS-1$
		}
	}

	/**
	 * Returns the label used for the enclosing group. The method is called
	 * only if <code>isGroup()</code> returns <code>true</code>.
	 *
	 * @return The label used for the enclosing group. If not set, a empty string will be returned.
	 */
	public final String getGroupLabel() {
		return groupLabel;
	}

	/**
	 * Sets the tool tip to appear if the user hovers the mouse over the label control.
	 *
	 * @param tooltip The tool tip or <code>null</code> if none.
	 */
	public final void setEditFieldLabelTooltip(String tooltip) {
		this.editFieldLabelTooltip = tooltip;
		// Apply directly to the label control if created already.
		SWTControlUtil.setToolTipText(getLabelControl(), editFieldLabelTooltip);
	}

	/**
	 * Returns the tool tip to appear if the user hovers the mouse over the label control.
	 *
	 * @return The tool tip or <code>null</code> if none.
	 */
	public final String getEditFieldLabelTooltip() {
		return editFieldLabelTooltip;
	}

	/**
	 * Sets the label to use for the edit field. If <code>null</code>, the
	 * edit field label is set to an empty string.
	 *
	 * @param label The edit field label to use or <code>null</code>.
	 */
	public final void setEditFieldLabel(String label) {
		if (label != null) {
			this.editFieldLabel = label;
		} else {
			this.editFieldLabel = ""; //$NON-NLS-1$
		}
		// Update the control as well if already created.
		setLabelControlText(label);
	}

	/**
	 * Returns the label used for the edit field.
	 *
	 * @return The label used for the edit field. If not set, a empty string will be returned.
	 */
	public final String getEditFieldLabel() {
		return editFieldLabel;
	}

	/**
	 * Sets the label to use for the button. If <code>null</code>, the
	 * button label is set to an empty string.
	 *
	 * @param label The button label to use or <code>null</code>.
	 */
	public final void setButtonLabel(String label) {
		if (label != null) {
			this.buttonLabel = label;
		} else {
			this.buttonLabel = ""; //$NON-NLS-1$
		}
	}

	/**
	 * Returns the label used for the button.
	 *
	 * @return The label used for the button. If not set, a empty string will be returned.
	 */
	public final String getButtonLabel() {
		return buttonLabel;
	}

	/**
	 * Sets if or if not the label control should be an radio button control or not.
	 * If <code>true</code>, <code>configureLabelControl()</code> will automatically register
	 * a selection listener to the radio button control to enable/disable the edit field and
	 * button controls depending on the selection state of the radio button control.
	 *
	 * @param isRadioButton <code>true</code> if the label should be an radio button control, <code>false</code> otherwise.
	 */
	public final void setLabelIsButton(boolean isRadioButton) {
		this.labelIsButton = isRadioButton;
	}

	/**
	 * Returns if or if not the label control is an radio button control.
	 *
	 * @return <code>true</code> if the label control is an radio button control, <code>false</code> otherwise.
	 */
	public final boolean isLabelIsButton() {
		return labelIsButton;
	}

	/**
	 * Sets the button style to be used for the button in front of the label in case
	 * <code>isLabelIsButton()</code> returns <code>true</code>. The style to set is
	 * typically either <code>SWT.RADIO</code> or <code>SWT.CHECK</code>. The default
	 * is set to <code>SWT.RADIO</code>.
	 *
	 * @param style The button style to use. @see the <code>SWT</code> constants for details.
	 */
	public final void setLabelButtonStyle(int style) {
		this.labelButtonStyle = style;
	}

	/**
	 * Returns the button style used for the button in front of the label in case <code>
	 * isLabelIsButton()</code> returns <code>true</code>.
	 *
	 * @return The button style used. @see the <code>SWT</code> constants for details.
	 */
	public int getLabelButtonStyle() {
		return labelButtonStyle;
	}

	/**
	 * Returns the controls inner panel composite.
	 *
	 * @return The controls inner panel composite or <code>null</code> if the composite has not been created yet.
	 */
	public final Composite getInnerPanelComposite() {
		return innerPanel;
	}

	/**
	 * The method is called to create the controls inner panel composite during setup of
	 * the controls UI elements. Subclasses may override this method to create their own
	 * inner panel composite.
	 *
	 * @param parent The parent control for the inner panel composite to create. Must not be <code>null</code>!
	 * @return The created inner panel composite.
	 */
	protected Composite doCreateInnerPanelComposite(Composite parent) {
		Assert.isNotNull(parent);

		Composite innerPanel = null;
		FormToolkit toolkit = getFormToolkit();

		if (isGroup()) {
			innerPanel = new Group(parent, SWT.NONE);
			if (toolkit != null) toolkit.adapt(innerPanel);
			((Group)innerPanel).setText(getGroupLabel());
		} else {
			innerPanel = toolkit != null ? toolkit.createComposite(parent) : new Composite(parent, SWT.NONE);
		}

		return innerPanel;
	}

	/**
	 * Configure the given controls inner panel composite before the control is set visible.
	 * Subclasses may use this hook to configure the controls inner panel composite for their
	 * specific needs.
	 *
	 * @param innerPanel The inner panel composite to configure. Must not be <code>null</code>!
	 */
	protected void configureInnerPanelComposite(Composite innerPanel) {
		Assert.isNotNull(innerPanel);

		if (isAdjustBackgroundColor()) {
			SWTControlUtil.setBackground(innerPanel, innerPanel.getParent().getBackground());
		}

		// Calculate the number of columns within the grid
		int numColumns = 3;
		if (isHideLabelControl()) {
			numColumns--;
		}
		if (isHideEditFieldControl()) {
			numColumns--;
		}
		if (isHideBrowseButton()) {
			numColumns--;
		}

		GridLayout layout = new GridLayout(numColumns, false);
		// if the inner panel is not a group (group is a composite as well, so we cannot test for
		// the composite directly), set the layouts margins to 0.
		if (!(innerPanel instanceof Group)) {
			// We assume a plain composite here and set back the layout margins to 0
			layout.marginHeight = 0; layout.marginWidth = 0;
		}

		innerPanel.setLayout(layout);
		innerPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	/**
	 * Adjust the inner panel layout if necessary. The method is called
	 * before the sub controls are added to the inner panel and only if the
	 * inner panel is using a {@link GridLayout}.
	 *
	 * @param layout The grid layout. Must not be <code>null</code>
	 */
	protected void doAdjustInnerPanelLayout(GridLayout layout) {
		Assert.isNotNull(layout);
	}

	/**
	 * Returns the label control. This control might be a label, a simple text, a radio button
	 * or any other SWT control.
	 *
	 * @return The label control or <code>null</code> if the control has not been created yet.
	 */
	public Control getLabelControl() {
		return labelControl;
	}

	/**
	 * The method is called to create the label control during setup of the
	 * controls UI elements. Subclasses may override this method to create their
	 * own SWT control to be used as label.
	 *
	 * @param parent The parent control for the label control to create. Must not be <code>null</code>!
	 * @return The created label control.
	 */
	protected Control doCreateLabelControl(Composite parent) {
		Assert.isNotNull(parent);

		Control labelControl = null;
		FormToolkit toolkit = getFormToolkit();

		if (!isLabelIsButton()) {
			labelControl = toolkit != null ? toolkit.createLabel(parent, null) : new Label(parent, SWT.NONE);
		} else {
			labelControl = toolkit != null ? toolkit.createButton(parent, null, getLabelButtonStyle() | SWT.NO_FOCUS) : new Button(parent, getLabelButtonStyle() | SWT.NO_FOCUS);
			SWTControlUtil.setSelection((Button)labelControl, false);
		}
		SWTControlUtil.setText(labelControl, getEditFieldLabel());

		return labelControl;
	}

	/**
	 * Configure the given label control before the control is set visible. Subclasses may use
	 * this hook to configure the label control for their specific needs and to register any
	 * required listener to the control.
	 *
	 * @param button The label control to configure. Must not be <code>null</code>!
	 */
	protected void configureLabelControl(final Control label) {
		Assert.isNotNull(label);
		if (isAdjustBackgroundColor()) {
			SWTControlUtil.setBackground(label, label.getParent().getBackground());
		}
		if (isLabelIsButton() && label instanceof Button) {
			((Button)labelControl).addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					onLabelControlSelectedChanged();
				}
			});
		}
		SWTControlUtil.setToolTipText(label, getEditFieldLabelTooltip());
	}

	/**
	 * This method is called from {@link #configureEditFieldControl(Control)} after the
	 * edit field has been configured. Reconfigure the label control layout data if
	 * necessary based on the just created edit field control settings.
	 * <p>
	 * The default implementation is aligning the label control on top of the cell in
	 * case the edit field control has the {@link SWT#MULTI} attribute set.
	 */
	protected void doAdjustLabelControlLayoutData() {
		// Get the edit field control style bits
		int style = getEditFieldControl().getStyle();
		// If SWT.MULTI is set, we have to align the control label on top of the cell.
		if ((style & SWT.MULTI) != 0) {
			Object data = getLabelControl().getLayoutData();
			if (data == null || data instanceof GridData) {
				GridData layoutData = data != null ? (GridData)data : new GridData();
				layoutData.verticalAlignment = SWT.TOP;
				getLabelControl().setLayoutData(layoutData);
			}
		}
	}

	/**
	 * This method is called either from <code>setLabelControlSelection(...)</code> or
	 * from the registered controls selection listener. The method enables/disables the
	 * edit field and button control depending on the selection state if <code>isLabelIsRadioButton()</code>
	 * returns <code>true</code> and the label control is an button. In all other cases,
	 * the method will do nothing.
	 */
	protected void onLabelControlSelectedChanged() {
		if (isLabelIsButton() && labelControl instanceof Button) {
			if (((Button)labelControl).getSelection()) {
				SWTControlUtil.setEnabled(getEditFieldControl(), true);
				SWTControlUtil.setEnabled(getButtonControl(), true);
			} else {
				SWTControlUtil.setEnabled(getEditFieldControl(), false);
				SWTControlUtil.setEnabled(getButtonControl(), false);
			}

			// validate the page
			IValidatableWizardPage validatable = getValidatableWizardPage();
			if (validatable != null) validatable.validatePage();
		}
	}

	/**
	 * In case the label control is an button and <code>isLabelIsRadioButton()</code>
	 * returns <code>true</code>, the selection state of the radio button is changed
	 * to the given state. In all other cases, the method will do nothing.
	 *
	 * @param selected The selection state of the radio button to set.
	 */
	public void setLabelControlSelection(boolean selected) {
		if (isLabelIsButton() && labelControl instanceof Button) {
			SWTControlUtil.setSelection((Button)labelControl, selected);
			onLabelControlSelectedChanged();
		}
	}

	/**
	 * Returns <code>true</code> if <code>isLabelIsRadioButton()</code> returns
	 * <code>true</code> and the label control is an button and the control is
	 * selected. In all other cases, the method will return <code>true</code>,
	 * because a label always indicates a "selected" edit field.
	 *
	 * @return <code>true</code> if the label control is a label or a selected radio button, <code>false</code> otherwise.
	 */
	public boolean isLabelControlSelected() {
		if (isLabelIsButton() && labelControl instanceof Button) {
			return SWTControlUtil.getSelection((Button)labelControl);
		}
		return true;
	}

	/**
	 * Returns the current set text from the label control.<br>
	 * Override if using custom label controls.
	 *
	 * @return The label controls text or an empty string.
	 */
	public String getLabelControlText() {
		String value = SWTControlUtil.getText(labelControl);
		if (value == null) value = ""; //$NON-NLS-1$
		return value;
	}

	/**
	 * Sets the text to show within the label control. This method can handle
	 * <code>Label</code>, <code>Text</code>, <code>Button</code> and <code>Combo</code>
	 * SWT controls only by default. If subclasses use different edit field controls than
	 * these, the subclass must override this method in order to apply the given text
	 * correctly to the label control.
	 *
	 * @param text The text to set within the label control. Must not be <code>null</code>.
	 */
	public void setLabelControlText(String text) {
		SWTControlUtil.setText(labelControl, text);
	}

	/**
	 * Returns the edit field control. This control might be an text or combobox
	 * or any other SWT control.
	 *
	 * @return The edit field control or <code>null</code> if the control has not been created yet.
	 */
	public Control getEditFieldControl() {
		return editFieldControl;
	}

	/**
	 * The method is called from default implementation of <code>doCreateEditFieldControl</code>
	 * in order to allow overrides to adjust the edit field control creation style bits finally.
	 * Whatever this method will return is used to create the edit field control. The default
	 * implementation returns the passed in style bits completely unmodified.
	 *
	 * @param style The default style bits to apply to create the edit field control.
	 * @return The possibly modified style bits to apply to create the edit field control.
	 */
	protected int doAdjustEditFieldControlStyles(int style) {
		return style;
	}

	/**
	 * The method is called from default implementation of {@link #doCreateEditFieldControl(Composite)}
	 * in order to allow overrider to adjust the edit field control layout data bits finally.
	 * Whatever this method sets to the passed in layout data, will be associated with the
	 * the edit field control.
	 * <p>
	 * If {@link #isAdjustEditFieldControlWidthHint()} returns <code>true</code>, the default implementation
	 * calculates a width hint for the edit field control as following:
	 * <ul>
	 * <li>Set default width hint to the width of approximately 50 characters in the current dialog font.</li>
	 * <li>If a parent control is associated, recalculate the width hint to be 85% of parent controls horizontal size.</li>
	 * </ul>
	 *
	 * @param layoutData The layout data to apply to the edit field control. Must not be <code>null</code>.
	 */
	protected void doAdjustEditFieldControlLayoutData(GridData layoutData) {
		Assert.isNotNull(layoutData);

		// adjust the control indentation
		if (getEditFieldControlDecoration() != null) {
			layoutData.horizontalIndent = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		}

		// adjust the horizontal span.
		layoutData.horizontalSpan = calculateEditFieldControlHorizontalSpan();

		// adjust the controls width hint within the given layout data
		if (isAdjustEditFieldControlWidthHint()) {
			layoutData.widthHint = SWTControlUtil.convertWidthInCharsToPixels(getEditFieldControl(), 50);

			Composite parent = getParentControl();
			if (parent != null) {
				// Calculate the size of the parent. We are interested to get the width of the parent control.
				int wHint = parent.getLayoutData() instanceof GridData ? ((GridData)parent.getLayoutData()).widthHint : SWT.DEFAULT;
				Point parentSize = parent.computeSize(wHint, SWT.DEFAULT, false);
				if (parentSize != null) {
					// Calculate the child widthHint to be 85% of the parent
					layoutData.widthHint = (85 * parentSize.x) / 100;
					// Update the parent layout width hint if calculated once
					if (parent.getLayoutData() instanceof GridData) {
						((GridData)parent.getLayoutData()).widthHint = parentSize.x;
					}
				}
			}
		}
	}

	/**
	 * Controls whether {@link #doAdjustEditFieldControlLayoutData(GridData)} is adjusting
	 * the layout data width hint for the edit field control or not.
	 * <p>
	 * The default implementation returns <code>false</code>.
	 *
	 * @return <code>True</code> to adjust the edit field controls layout data width hint attributed, <code>false</code> for not to adjust.
	 */
	protected boolean isAdjustEditFieldControlWidthHint() {
		return false;
	}

	/**
	 * The method is called to create the edit field control during setup of the
	 * controls UI elements. Subclasses may override this method to create their
	 * own SWT control to be used as edit field.
	 *
	 * @param parent The parent control for the edit field control to create. Must not be <code>null</code>!
	 * @return The created edit field control.
	 */
	protected Control doCreateEditFieldControl(Composite parent) {
		Assert.isNotNull(parent);

		final Scrollable editField;
		FormToolkit toolkit = getFormToolkit();

		if (hasHistory()) {
			// if the control should have an history, the edit field control is an combobox
			int style = SWT.DROP_DOWN;
			if (isReadOnly()) {
				style |= SWT.READ_ONLY;
			}
			editField = new Combo(parent, doAdjustEditFieldControlStyles(style));
			if (toolkit != null) toolkit.adapt((Combo)editField);
			((Combo)editField).addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (!isInitializing) { // do not call this unless the boundaries of the control are calculated yet
						SWTControlUtil.setValueToolTip(editField);
					}
				}
			});
			((Combo)editField).addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!isInitializing) { // do not call this unless the boundaries of the control are calculated yet
						SWTControlUtil.setValueToolTip(editField);
					}
				}
			});
			// make sure that after resizing a control, the necessity of showing the tool tip is recalculated
			((Combo)editField).addPaintListener(new PaintListener() {
				@Override
				public void paintControl(PaintEvent e) {
					if (!isInitializing) { // do not call this unless the boundaries of the control are calculated yet
						SWTControlUtil.setValueToolTip(editField);
					}
				}});
		} else {
			int style = SWT.SINGLE;
			if (isReadOnly()) {
				style |= SWT.READ_ONLY;
			}
			editField = toolkit != null ? toolkit.createText(parent, null, doAdjustEditFieldControlStyles(SWT.BORDER | style)) : new Text(parent, doAdjustEditFieldControlStyles(SWT.BORDER | style));
			((Text)editField).addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (!isInitializing) { // do not call this unless the boundaries of the control are calculated yet
						SWTControlUtil.setValueToolTip(editField);
					}
				}
			});
			// make sure that after resizing a control, the necessity of showing the tool tip is recalculated
			((Text)editField).addPaintListener(new PaintListener() {
				@Override
				public void paintControl(PaintEvent e) {
					if (!isInitializing) { // do not call this unless the boundaries of the control are calculated yet
						SWTControlUtil.setValueToolTip(editField);
					}
				}});
		}

		return editField;
	}

	/**
	 * Configure the given edit field control before the control is set visible. Subclasses may use
	 * this hook to configure the edit field control for their specific needs and to register any
	 * required listener to the control.
	 *
	 * @param control The edit field control to configure. Must not be <code>null</code>!
	 */
	protected void configureEditFieldControl(Control control) {
		Assert.isNotNull(control);

		// the edit field control expands within the inner composite
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		doAdjustEditFieldControlLayoutData(layoutData);
		control.setLayoutData(layoutData);

		// The edit field can influence the layout data of the label control (SWT.MULTI).
		// Give the label control the chance to reconfigure after the edit field control
		// got created and the control style bits can be queried.
		doAdjustLabelControlLayoutData();

		// register the modification and selection listener if they are available.
		ModifyListener modifyListener = doGetEditFieldControlModifyListener();
		VerifyListener verifyListener = doGetEditFieldControlVerifyListener();
		SelectionListener selectionListener = doGetEditFieldControlSelectionListener();

		if (control instanceof Text) {
			if (modifyListener != null) {
				((Text)control).addModifyListener(modifyListener);
			}
			if (verifyListener != null) {
				((Text)control).addVerifyListener(verifyListener);
			}
		}
		if (control instanceof Combo) {
			if (modifyListener != null) {
				((Combo)control).addModifyListener(modifyListener);
			}
			if (verifyListener != null) {
				((Combo)control).addVerifyListener(verifyListener);
			}
			if (selectionListener != null) {
				((Combo)control).addSelectionListener(selectionListener);
			}
		}
		// if the label control is an button control, trigger an initial onLabelControlSelectedChanged to
		// enable/disable the edit field control correctly initially.
		if (isLabelIsButton()) {
			onLabelControlSelectedChanged();
		}
	}

	/**
	 * Returns the horizontal span value which is set to the edit field controls grid
	 * layout data.
	 */
	public int calculateEditFieldControlHorizontalSpan() {
		// Default horizontal span is always 1.
		int span = 1;

		if (getEditFieldControl() != null && getEditFieldControl().getParent() != null) {
			// Get the parent control of the edit field
			Composite parent = getEditFieldControl().getParent();
			// Determine the number of columns within the parent
			int numColumns = parent.getLayout() instanceof GridLayout ? ((GridLayout)parent.getLayout()).numColumns : 1;
			// Calculate the number of columns consumed
			int consumed = 0;
			if (!isHideLabelControl()) {
				consumed++; // The label
			}
			if (!isHideEditFieldControl()) {
				consumed++; // The edit field control
			}
			if (!isHideBrowseButton()) {
				consumed++; // The browse button
			}
			// In case there are more columns available than consumed,
			// make the edit field control spanning over all the remaining columns.
			if (numColumns > consumed) {
				span = numColumns - consumed + 1;
			}
		}

		return span;
	}

	/**
	 * Creates a new instance of a {@link ControlDecoration} object associated with
	 * the given edit field control. The method is called after the edit field control
	 * has been created.
	 *
	 * @param control The edit field control. Must not be <code>null</code>.
	 * @return The control decoration object instance.
	 */
	protected ControlDecoration doCreateEditFieldControlDecoration(Control control) {
		Assert.isNotNull(control);
		return new ControlDecoration(control, doGetEditFieldControlDecorationPosition());
	}

	/**
	 * Returns the edit field control decoration position. The default is
	 * {@link SWT#TOP} | {@link SWT#LEFT}.
	 *
	 * @return The edit field control position.
	 */
	protected int doGetEditFieldControlDecorationPosition() {
		return SWT.TOP | SWT.LEFT;
	}

	/**
	 * Configure the given edit field control decoration.
	 *
	 * @param decoration The edit field control decoration. Must not be <code>null</code>.
	 */
	protected void configureEditFieldControlDecoration(ControlDecoration decoration) {
		Assert.isNotNull(decoration);
		decoration.setShowOnlyOnFocus(false);
	}

	/**
	 * Updates the given edit field control decoration to represent the given
	 * message and message type.
	 *
	 * @param decoration The control decoration. Must not be <code>null</code>.
	 * @param message The message. Must not be <code>null</code>.
	 * @param messageType The message type.
	 */
	protected void updateEditFieldControlDecorationForMessage(ControlDecoration decoration, String message, int messageType) {
		Assert.isNotNull(decoration);
		Assert.isNotNull(message);

		// The description is the same as the message
		decoration.setDescriptionText(message);

		// The icon depends on the message type
		FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();

		// Determine the id of the decoration to show
		String decorationId = FieldDecorationRegistry.DEC_INFORMATION;
		if (messageType == IMessageProvider.ERROR) {
			decorationId = FieldDecorationRegistry.DEC_ERROR;
		} else if (messageType == IMessageProvider.WARNING) {
			decorationId = FieldDecorationRegistry.DEC_WARNING;
		}

		// Get the field decoration
		FieldDecoration fieldDeco = registry.getFieldDecoration(decorationId);
		if (fieldDeco != null) {
			decoration.setImage(fieldDeco.getImage());
		}
	}

	/**
	 * Returns the edit field control decoration.
	 *
	 * @return The edit field control decoration instance or <code>null</code> if not yet created.
	 */
	public final ControlDecoration getEditFieldControlDecoration() {
		return controlDecoration;
	}

	/**
	 * Returns the modification listener instance to be registered for the edit field
	 * control if not <code>null</code>. The default implementation returns always <code>
	 * null</code>. Subclasses may override this method to provide a suitable modification
	 * listener for the edit field control.
	 *
	 * @return The modification listener to register to the edit field control or <code>null</code>.
	 */
	protected ModifyListener doGetEditFieldControlModifyListener() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	@Override
	public void modifyText(ModifyEvent e) {
		// validate the page
		IValidatableWizardPage validatable = getValidatableWizardPage();
		if (validatable != null) validatable.validatePage();
	}

	/**
	 * Returns the verify listener instance to be registered for the edit field
	 * control if not <code>null</code>. The default implementation returns always <code>
	 * null</code>. Subclasses may override this method to provide a suitable verify
	 * listener for the edit field control.
	 *
	 * @return The verify listener to register to the edit field control or <code>null</code>.
	 */
	protected VerifyListener doGetEditFieldControlVerifyListener() {
		return null;
	}

	/**
	 * Returns the selection listener instance to be registered for the edit field
	 * control if not <code>null</code>. The default implementation returns always <code>
	 * null</code>. Subclasses may override this method to provide a suitable selection
	 * listener for the edit field control.
	 *
	 * @return The modification listener to register to the edit field control or <code>null</code>.
	 */
	protected SelectionListener doGetEditFieldControlSelectionListener() {
		if (getEditFieldControl() instanceof Combo) {
			return this;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		// validate the page
		IValidatableWizardPage validatable = getValidatableWizardPage();
		if (validatable != null) validatable.validatePage();
	}

	/**
	 * Returns the current set text from the edit field control.
	 *
	 * @return The edit field controls text or an empty string, but never <code>null</code>.
	 */
	public String getEditFieldControlText() {
		String value = SWTControlUtil.getText(editFieldControl);
		if (value == null) value = ""; //$NON-NLS-1$
		return value;
	}

	/**
	 * The content of the edit field control might require preparation before it
	 * can be validated at all. By default, this method returns the same value as
	 * a call to <code>getEditFieldControlText()</code>.
	 *
	 * @return The edit field control text to validate.
	 */
	public String getEditFieldControlTextForValidation() {
		return getEditFieldControlText();
	}

	/**
	 * Sets the text to show within the edit field control. This method can handle
	 * <code>Text</code> and <code>Combo</code> SWT controls only by default. If subclasses
	 * use different edit field controls than these two, the subclass must override
	 * this method in order to apply the given text correctly to the edit field control.
	 *
	 * @param text The text to set within the edit field control. Must not be <code>null</code>.
	 */
	public void setEditFieldControlText(String text) {
		if (text == null) {
			text = ""; //$NON-NLS-1$
		}
		String oldText = SWTControlUtil.getText(editFieldControl);
		if (!text.equals(oldText)) {
			SWTControlUtil.setText(editFieldControl, text);
			// If the edit field control is not a combobox, next statement will do nothing
			SWTControlUtil.add(editFieldControl, text);
		}
	}

	/**
	 * Set the edit field control history to the given history entries if <code>
	 * hasHistory()</code> returns <code>true</code> and the edit field control
	 * is an SWT <code>Combo</code> control. If subclasses use different edit field
	 * controls, the subclass must override this method if the used control supports
	 * history lists. Duplicated history entries, empty history entries or <code>null</code>
	 * values are not applied to the history.
	 *
	 * @param historyEntries The history entries to set. Must not be <code>null</code>!
	 */
	public void setEditFieldControlHistory(String[] historyEntries) {
		Assert.isNotNull(historyEntries);
		if (hasHistory() && getEditFieldControl() instanceof Combo) {
			Combo combo = (Combo)getEditFieldControl();
			List<String> oldItems = new ArrayList<String>(Arrays.asList(SWTControlUtil.getItems(combo)));
			List<String> newItems = new ArrayList<String>();

			String oldSelectedItem = getEditFieldControlText();

			// we add the entries one by one to filter out duplicates, empty strings and null values.
			for (String entry : historyEntries) {
				if (entry == null || entry.trim().length() == 0 || newItems.contains(entry)) {
					continue;
				}
				newItems.add(entry);
			}

			// Create the array of new items to apply before sorting the
			// new items list. Otherwise we will loose the order of the
			// items in which the clients wants to set them to the control
			final String[] newItemsArray = newItems.toArray(new String[newItems.size()]);

			// The two lists must be in the same order to compare them with equals
			Collections.sort(oldItems);
			Collections.sort(newItems);

			if (!newItems.equals(oldItems)) SWTControlUtil.setItems(combo, newItemsArray);

			// Restore the previously selected item if still available
			if (newItems.contains(oldSelectedItem)) setEditFieldControlText(oldSelectedItem);
		}
	}

	/**
	 * Adds the given string to the edit field control history if <code>
	 * hasHistory()</code> returns <code>true</code> and the edit field control
	 * is an SWT <code>Combo</code> control. If subclasses use different edit field
	 * controls, the subclass must override this method if the used control supports
	 * history lists. Duplicated history entries, empty history entries or <code>null</code>
	 * values are not applied to the history.
	 *
	 * @param entry
	 */
	public void addToEditFieldControlHistory(String entry) {
		if (hasHistory() && getEditFieldControl() instanceof Combo) {
			Combo combo = (Combo)getEditFieldControl();
			if (entry != null && entry.trim().length() > 0 && combo.indexOf(entry) == -1) {
				combo.add(entry);
			}
		}
	}

	/**
	 * The method is called to create an edit field validator during setup.
	 * Subclasses have to override this method to create the right validator.
	 * The default validator is <code>null</code> and so it isn't used.
	 *
	 * @return The new created edit field validator.
	 */
	protected Validator doCreateEditFieldValidator() {
		return null;
	}

	/**
	 * Configure the edit field validator.
	 * Subclasses should override this method to configure the validator.
	 *
	 * @param validator The validator to be configured.
	 */
	protected void configureEditFieldValidator(Validator validator) {
		// do nothing
	}

	/**
	 * Returns the button control.
	 *
	 * @return The button control or <code>null</code> if the control has not been created yet.
	 */
	public Button getButtonControl() {
		return buttonControl;
	}

	/**
	 * The method is called to create the button control during setup of the controls
	 * UI elements. Subclasses may override this method to create their own button control.
	 *
	 * @param parent The parent control for the button control to create. Must not be <code>null</code>!
	 * @return The created button control.
	 */
	protected Button doCreateButtonControl(Composite parent) {
		Assert.isNotNull(parent);

		FormToolkit toolkit = getFormToolkit();

		Button button = toolkit != null ? toolkit.createButton(parent, null, SWT.PUSH) : new Button(parent, SWT.PUSH);
		// add a whitespace at the beginning and at the end of the button text to make the
		// button visibly broader than the label itself.
		button.setText(" " + getButtonLabel().trim() + " "); //$NON-NLS-1$ //$NON-NLS-2$

		return button;
	}

	/**
	 * Configure the given button control before the control is set visible. Subclasses may use
	 * this hook to configure the button control for their specific needs and to register any
	 * required listener to the control.
	 *
	 * @param button The button control to configure. Must not be <code>null</code>!
	 */
	protected void configureButtonControl(Button button) {
		Assert.isNotNull(button);
		// add the selection listener to open the file dialog if the user pressed the button
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onButtonControlSelected();
			}
		});

		// If not yet set, assure that the buttons fill in the available space
		if (button.getLayoutData() == null) {
			button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		}
	}

	/**
	 * Called if the user pressed the button control. Subclasses may override
	 * this method to plugin the desired functionality.
	 */
	protected void onButtonControlSelected() {
	}

	/**
	 * Returns the layout data to be used for the top most controls composite. Because this
	 * top most composite is directly embedded into the parent control, it cannot be predicted
	 * which layout data object class must be associated to the top most controls composite. Subclasses
	 * must override this method if the layout of the parent object is not a <code>org.eclipse.swt.layout.GridLayout</code>!
	 *
	 * @param parentLayout The associated layout of the parent composite of the top most controls composite. Might be <code>null</code>!
	 * @return The layout data object to be associated to the top most controls composite. Must be never <code>null</code>!
	 */
	protected Object getTopMostCompositeLayoutData(Layout parentLayout) {
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		if (parentLayout instanceof GridLayout) {
			layoutData.horizontalSpan = ((GridLayout)parentLayout).numColumns;
		}
		return layoutData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseControl#setupPanel(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void setupPanel(Composite parent) {
		isInitializing = true;
		super.setupPanel(parent);

		FormToolkit toolkit = getFormToolkit();

		// do we need a group or a plain composite
		if (!isParentControlIsInnerPanel() || !(parent.getLayout() instanceof GridLayout)) {
			// create the control most enclosing composite
			Composite composite = toolkit != null ? toolkit.createComposite(parent) : new Composite(parent, SWT.NONE);
			if (isAdjustBackgroundColor()) {
				SWTControlUtil.setBackground(composite, parent.getBackground());
			}
			composite.setLayoutData(getTopMostCompositeLayoutData(parent.getLayout()));

			// within the top most controls composite, the layout management is
			// in our own hands again.
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0; layout.marginWidth = 0;
			composite.setLayout(layout);

			innerPanel = doCreateInnerPanelComposite(composite);
			// give the subclasses the chance to reconfigure the inner panel composite for specific needs.
			configureInnerPanelComposite(innerPanel);
		} else {
			innerPanel = parent;
		}

		// Adjust the inner panel layout data. This is the final point in time
		// to influence the inner panel layout.
		if (innerPanel.getLayout() instanceof GridLayout) {
			doAdjustInnerPanelLayout((GridLayout)innerPanel.getLayout());
		}

		// now, the label control for the edit field control comes first
		if (!isHideLabelControl()) {
			labelControl = doCreateLabelControl(innerPanel);
			// give the subclasses the chance to reconfigure the label control for specific needs
			configureLabelControl(labelControl);
		}

		// In case, the button is not hidden and the inner panel to use
		// has only 2 columns, we need an additional inner inner panel to
		// squeeze the edit field control and the button into such panel
		Composite innerInnerPanel = innerPanel;
		if (((GridLayout)innerInnerPanel.getLayout()).numColumns == 2 && !isHideBrowseButton() && !isHideEditFieldControl()) {
			innerInnerPanel = toolkit != null ? toolkit.createComposite(innerPanel) : new Composite(innerPanel, SWT.NONE);
			if (isAdjustBackgroundColor()) {
				SWTControlUtil.setBackground(innerInnerPanel, innerPanel.getBackground());
			}
			GridLayout layout = new GridLayout(2, false);
			layout.marginHeight = 0; layout.marginWidth = 0;
			innerInnerPanel.setLayout(layout);
			innerInnerPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}

		if (!isHideEditFieldControl()) {
			// Create the edit field control itself. The result of
			// doCreateEditFieldControl(...) must be always not null!
			editFieldControl = doCreateEditFieldControl(innerInnerPanel);
			Assert.isNotNull(editFieldControl);

			// Once the edit field got created, the control decoration must
			// be created and configured _before_ the edit field itself is
			// configured. Otherwise, the layout data for the edit field may
			// not be configured correctly.
			if (!isHideEditFieldControlDecoration()) {
				controlDecoration = doCreateEditFieldControlDecoration(editFieldControl);
				Assert.isNotNull(controlDecoration);
				configureEditFieldControlDecoration(controlDecoration);
			}

			// Configure the edit field control (including layout data)
			configureEditFieldControl(editFieldControl);

			// before validation, create the edit field validator
			setEditFieldValidator(doCreateEditFieldValidator());
			// now configure the edit field validator
			configureEditFieldValidator(getEditFieldValidator());
		}

		if (!isHideBrowseButton()) {
			// finally, the button most right end.
			buttonControl = doCreateButtonControl(innerInnerPanel);
			// give the subclasses the chance to reconfigure the button control for specific needs
			configureButtonControl(buttonControl);
		}

		// validate the control before setting the control visible
		isValid();
		isInitializing = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseControl#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();

		labelControl = null;
		editFieldControl = null;
		buttonControl = null;
	}

	/**
	 * Set the dialog settings slot id to use for saving and restoring the controls history
	 * to and from a given dialog settings instance. If the slot id is set to <code>null</code>,
	 * the edit field control label is used as slot id!
	 *
	 * @param settingsSlotId The dialog settings slot id to use or <code>null</code> to use the edit field control label as slot id.
	 */
	public void setDialogSettingsSlotId(String settingsSlotId) {
		dialogSettingsSlotId = settingsSlotId;
	}

	/**
	 * Returns the dialog settings slot id to use for saving and restoring the controls history
	 * to and from a given dialog settings instance. The returned dialog settings slot id is
	 * automatically prefixed if the given prefix is not <code>null</code> or empty.
	 *
	 * @param prefix The dialog settings slot id prefix or <code>null</code>.
	 * @return The dialog settings slot id to use. Must be never <code>null</code>!
	 */
	public String getDialogSettingsSlotId(String prefix) {
		String settingsSlotId = dialogSettingsSlotId;
		if (settingsSlotId == null) {
			settingsSlotId = getEditFieldLabel().replace(':', ' ').trim().replace(' ', '_');
		}
		return prefixDialogSettingsSlotId(settingsSlotId, prefix);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseControl#doRestoreWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
		String[] historyEntries = getHistory(settings, idPrefix);
		if (historyEntries.length > 0) {
			setEditFieldControlHistory(historyEntries);
			if ("".equals(getEditFieldControlText())) setEditFieldControlText(historyEntries[0]); //$NON-NLS-1$
		}
	}

	/**
	 * Get the history entries from the dialog setting.
	 * @param settings The dialog setting.
	 * @param idPrefix The prefix for the dialog setting slot id
	 * @return The history entries or an empty array. Will never return <code>null</code>!
	 */
	protected String[] getHistory(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);
		if (settings != null && getDialogSettingsSlotId(idPrefix) != null) {
			return DialogSettingsUtil.getSettingsArraySafe(settings, getDialogSettingsSlotId(idPrefix));
		}

		return new String[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseControl#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);
		if (settings != null && getDialogSettingsSlotId(idPrefix) != null) {
			String[] historyEntries = DialogSettingsUtil.getSettingsArraySafe(settings, getDialogSettingsSlotId(idPrefix));
			historyEntries = DialogSettingsUtil.addToHistory(historyEntries, getEditFieldControlText());
			settings.put(getDialogSettingsSlotId(idPrefix), historyEntries);
		}
	}

	/**
	 * Returns the validator for the edit field.
	 *
	 * @return The edit field validator.
	 */
	public final Validator getEditFieldValidator() {
		return editFieldValidator;
	}

	/**
	 * Set the validator for the edit field.
	 * This method should be overwritten to check whether the validator type is
	 * valid for the edit field.
	 *
	 * @param editFieldValidator The validator for the edit field.
	 */
	public void setEditFieldValidator(Validator editFieldValidator) {
		this.editFieldValidator = editFieldValidator;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseControl#isValid()
	 */
	@Override
	public boolean isValid() {
		if (isInitializing) {
			return true;
		}
		boolean valid = super.isValid();

		if (getEditFieldValidator() != null &&
			getEditFieldControl() != null &&
			!getEditFieldControl().isDisposed() &&
			SWTControlUtil.isEnabled(getEditFieldControl()) &&
			!isReadOnly() &&
			isLabelControlSelected()) {

			valid = getEditFieldValidator().isValid(getEditFieldControlTextForValidation());
			setMessage(getEditFieldValidator().getMessage(), getEditFieldValidator().getMessageType());
		}

		if (getEditFieldControlDecoration() != null) {
			// Setup and show the control decoration if necessary
			if (isEnabled() && (!valid || (getMessage() != null && getMessageType() != IMessageProvider.NONE))) {
				// Update the control decorator
				ControlDecoration decoration = getEditFieldControlDecoration();
				updateEditFieldControlDecorationForMessage(decoration, getMessage(), getMessageType());

				// And show the decoration
				decoration.show();
			} else {
				ControlDecoration decoration = getEditFieldControlDecoration();
				// Control is valid and no message is set -> hide the decoration
				decoration.hide();
				decoration.setDescriptionText(null);
			}
		}

		return valid;
	}
}
