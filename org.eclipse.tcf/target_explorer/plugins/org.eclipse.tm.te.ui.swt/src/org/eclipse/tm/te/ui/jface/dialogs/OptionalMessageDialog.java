/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.jface.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.tm.te.ui.swt.activator.UIPlugin;
import org.eclipse.tm.te.ui.swt.nls.Messages;
import org.eclipse.ui.PlatformUI;

/**
 * Message dialog with "do not show again" and optional help button. The Dialog
 * stores the selected button result automatically, when "do not show again" was
 * selected. All stored values can be cleared in the Target Explorer preferences
 * root page.
 * <p>
 * Additional information (e.g. last opening time stamp for license warning) can
 * be stored using <code>set/getAdditionalDialogInfo()</code>, that should
 * also be cleared with the states.
 */
public class OptionalMessageDialog extends MessageDialogWithToggle {

	// section name for the dialog settings stored by this dialog
	private static final String DIALOG_ID = "OptionalMessageDialog"; //$NON-NLS-1$

	// context help id for the dialog
	private String contextHelpId;
	// the key where the result is stored within the dialog settings section
	private String key;

	/**
	 * Constructor. Message dialog with "do not show again" and optional help
	 * button. The dialog automatically stores the pressed button when "do not
	 * show again" was selected. The next time the dialogs <code>open()</code>
	 * method is called it returns the stored value without opening the dialog.
	 * When the cancel button was pressed, _NO_ value will be stored.
	 *
	 * @param parentShell
	 *            The shell.
	 * @param title
	 *            The title for the message dialog.
	 * @param image
	 *            The window icon or <code>null</code> if default icon should
	 *            be used.
	 * @param message
	 *            The dialog message text.
	 * @param imageType
	 *            The dialog image type (QUESTION, INFORMATION, WARNING, ERROR).
	 * @param buttonLabels
	 *            The labels for buttons.
	 * @param defaultIndex
	 *            The default button index.
	 * @param key
	 *            The unique key for the stored result value (e.g. "<PluginName>.<ActionOrDialogName>").
	 * @param contextHelpId
	 *            The optional help context id. If <code>null</code>, no help
	 *            button will be shown.
	 */
	public OptionalMessageDialog(Shell parentShell, String title, Image image, String message, int imageType, String[] buttonLabels, int defaultIndex, String key, String contextHelpId) {

		super(parentShell,
		      title,
		      image,
		      message,
		      imageType,
		      buttonLabels != null ? buttonLabels : new String [] { IDialogConstants.OK_LABEL },
		      defaultIndex,
		      Messages.getString(DIALOG_ID + (imageType == QUESTION ? "_rememberMyDecision_label" : "_doNotShowAgain_label")), //$NON-NLS-1$ //$NON-NLS-2$
		      false);

		this.contextHelpId = contextHelpId;
		this.key = key == null || key.trim().length() == 0 ? null : key.trim();

		if (contextHelpId != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(parentShell, contextHelpId);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	@Override
	protected boolean isResizable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IconAndMessageDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		if (parent.getLayout() instanceof GridLayout) {
			gd.horizontalSpan = ((GridLayout)parent.getLayout()).numColumns;
		}
		composite.setLayoutData(gd);
		composite.setFont(parent.getFont());

		// create help control if needed
		if (contextHelpId != null) {
			Control helpControl = createHelpControl(composite);
			((GridData)helpControl.getLayoutData()).horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		}

		Control buttonSection = super.createButtonBar(composite);
		((GridData)buttonSection.getLayoutData()).grabExcessHorizontalSpace = true;
		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.MessageDialog#createButton(org.eclipse.swt.widgets.Composite, int, java.lang.String, boolean)
	 */
	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		// Allow to re-adjust the button id's. Base implementation is matching
		// the button labels against the well known labels defined by IDialogConstants.
		// For labels not defined there, the implementation set id's starting with 256.
		return super.createButton(parent, adjustButtonIdForLabel(id, label), label, defaultButton);
	}

	/**
	 * Adjust the button id to use for the button with the given label.
	 * <p>
	 * <b>Note:</b>Base implementation is matching the button labels against the well known
	 * labels defined by {@link IDialogConstants}. For labels not defined there, the implementation
	 * set id's starting with 256.
	 * <p>
	 * The default implementation returns the button id unmodified.
	 *
	 * @param id The suggested button id.
	 * @param label The button label.
	 * @return The effective button id.
	 */
	protected int adjustButtonIdForLabel(int id, String label) {
		return id;
	}

	private Control createHelpControl(Composite parent) {
		Image helpImage = JFaceResources.getImage(DLG_IMG_HELP);
		if (helpImage != null) {
			return createHelpImageButton(parent, helpImage);
		}
		return createHelpLink(parent);
	}

	/*
	 * Creates a button with a help image. This is only used if there
	 * is an image available.
	 */
	private ToolBar createHelpImageButton(Composite parent, Image image) {
		ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.NO_FOCUS);
		((GridLayout)parent.getLayout()).numColumns++;
		toolBar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		final Cursor cursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		toolBar.setCursor(cursor);
		toolBar.addDisposeListener(new DisposeListener() {
			@Override
            public void widgetDisposed(DisposeEvent e) {
				cursor.dispose();
			}
		});
		ToolItem item = new ToolItem(toolBar, SWT.NONE);
		item.setImage(image);
		item.setToolTipText(JFaceResources.getString("helpToolTip")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				helpPressed();
			}
		});
		return toolBar;
	}

	/*
	 * Creates a help link. This is used when there is no help image
	 * available.
	 */
	private Link createHelpLink(Composite parent) {
		Link link = new Link(parent, SWT.WRAP | SWT.NO_FOCUS);
		((GridLayout)parent.getLayout()).numColumns++;
		link.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		link.setText("<a>" + IDialogConstants.HELP_LABEL + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
		link.setToolTipText(IDialogConstants.HELP_LABEL);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				helpPressed();
			}
		});
		return link;
	}

	/**
	 * Invoked if the help button is pressed.
	 */
	/* default */ void helpPressed() {
		if (getShell() != null) {
			Control c = getShell().getDisplay().getFocusControl();
			while (c != null) {
				if (c.isListening(SWT.Help)) {
					c.notifyListeners(SWT.Help, new Event());
					break;
				}
				c = c.getParent();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createCustomArea(Composite parent) {
		if (contextHelpId != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, contextHelpId);
		}
		Label label = new Label(parent, SWT.NULL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		return label;
	}

	/**
	 * Opens the dialog only, if no dialog result is stored and this dialog
	 * should be displayed. If a dialog result is stored, this state will be
	 * returned without opening the dialog. When the dialog is closed and "do
	 * not show again" was selected, the result will be stored.
	 *
	 * @see org.eclipse.jface.window.Window#open()
	 */
	@Override
	public int open() {
		int result = getDialogResult(key);
		if (result < 0) {
			result = super.open();
			if (getToggleState() && result >= 0 && result != IDialogConstants.CANCEL_ID) {
				setDialogResult(key, result);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.MessageDialogWithToggle#setToggleButton(org.eclipse.swt.widgets.Button)
	 */
	@Override
	protected void setToggleButton(Button button) {
		// if no key is given, no toggle button should be displayed
		if (button != null && key != null) {
			super.setToggleButton(button);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.MessageDialogWithToggle#createToggleButton(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Button createToggleButton(Composite parent) {
		// if no key is given, no toggle button should be created
		if (key != null) {
			return super.createToggleButton(parent);
		}
		return null;
	}
	/**
	 * Opens a question dialog with OK and CANCEL.
	 *
	 * @param parentShell
	 *            The shell.
	 * @param title
	 *            The title for the message dialog.
	 * @param message
	 *            The dialog message text.
	 * @param key
	 *            The unique key for the stored result value (e.g. "<PluginName>.<ActionOrDialogName>").
	 * @param contextHelpId
	 *            The optional help context id. If <code>null</code>, no help
	 *            button will be shown.
	 * @return The stored or selected result.
	 */
	public static int openOkCancelDialog(Shell parentShell, String title, String message, String key, String contextHelpId) {
		return openOkCancelDialog(parentShell, title, message, null, key, contextHelpId);
	}

	/**
	 * Opens a question dialog with OK and CANCEL.
	 *
	 * @param parentShell
	 *            The shell.
	 * @param title
	 *            The title for the message dialog.
	 * @param message
	 *            The dialog message text.
	 * @param buttonLabel
	 * 			  An string array listing the labels of the message dialog buttons. If <code>null</code>, the default
	 *            labeling, typically &quot;OK&quot; for a single button message dialog, will be applied.
	 * @param key
	 *            The unique key for the stored result value (e.g. "<PluginName>.<ActionOrDialogName>").
	 * @param contextHelpId
	 *            The optional help context id. If <code>null</code>, no help
	 *            button will be shown.
	 * @return The stored or selected result.
	 */
	public static int openOkCancelDialog(Shell parentShell, String title, String message, String[] buttonLabel, String key, String contextHelpId) {
		if (buttonLabel == null) buttonLabel = new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL };
		OptionalMessageDialog dialog = new OptionalMessageDialog(parentShell, title, null, message, QUESTION, buttonLabel, 0, key, contextHelpId);
		return dialog.open();
	}

	/**
	 * Opens a question dialog with YES, NO and CANCEL.
	 *
	 * @param parentShell
	 *            The shell.
	 * @param title
	 *            The title for the message dialog.
	 * @param message
	 *            The dialog message text.
	 * @param key
	 *            The unique key for the stored result value (e.g. "<PluginName>.<ActionOrDialogName>").
	 * @param contextHelpId
	 *            The optional help context id. If <code>null</code>, no help
	 *            button will be shown.
	 * @return The stored or selected result.
	 */
	public static int openYesNoCancelDialog(Shell parentShell, String title, String message, String key, String contextHelpId) {
		return openYesNoCancelDialog(parentShell, title, message, null, key, contextHelpId);
	}

	/**
	 * Opens a question dialog with YES, NO and CANCEL.
	 *
	 * @param parentShell
	 *            The shell.
	 * @param title
	 *            The title for the message dialog.
	 * @param message
	 *            The dialog message text.
	 * @param buttonLabel
	 * 			  An string array listing the labels of the message dialog buttons. If <code>null</code>, the default
	 *            labeling, typically &quot;OK&quot; for a single button message dialog, will be applied.
	 * @param key
	 *            The unique key for the stored result value (e.g. "<PluginName>.<ActionOrDialogName>").
	 * @param contextHelpId
	 *            The optional help context id. If <code>null</code>, no help
	 *            button will be shown.
	 * @return The stored or selected result.
	 */
	public static int openYesNoCancelDialog(Shell parentShell, String title, String message, String[] buttonLabel, String key, String contextHelpId) {
		if (buttonLabel == null) buttonLabel = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL };
		OptionalMessageDialog dialog = new OptionalMessageDialog(parentShell, title, null, message, QUESTION, buttonLabel, 0, key, contextHelpId);
		return dialog.open();
	}

	/**
	 * Opens a question dialog with YES and NO.
	 *
	 * @param parentShell
	 *            The shell.
	 * @param title
	 *            The title for the message dialog.
	 * @param message
	 *            The dialog message text.
	 * @param key
	 *            The unique key for the stored result value (e.g. "<PluginName>.<ActionOrDialogName>").
	 * @param contextHelpId
	 *            The optional help context id. If <code>null</code>, no help
	 *            button will be shown.
	 * @return The stored or selected result.
	 */
	public static int openYesNoDialog(Shell parentShell, String title, String message, String key, String contextHelpId) {
		return openYesNoDialog(parentShell, title, message, null, key, contextHelpId);
	}

	/**
	 * Opens a question dialog with YES and NO.
	 *
	 * @param parentShell
	 *            The shell.
	 * @param title
	 *            The title for the message dialog.
	 * @param message
	 *            The dialog message text.
	 * @param buttonLabel
	 * 			  An string array listing the labels of the message dialog buttons. If <code>null</code>, the default
	 *            labeling, typically &quot;OK&quot; for a single button message dialog, will be applied.
	 * @param key
	 *            The unique key for the stored result value (e.g. "<PluginName>.<ActionOrDialogName>").
	 * @param contextHelpId
	 *            The optional help context id. If <code>null</code>, no help
	 *            button will be shown.
	 * @return The stored or selected result.
	 */
	public static int openYesNoDialog(Shell parentShell, String title, String message, String[] buttonLabel, String key, String contextHelpId) {
		if (buttonLabel == null) buttonLabel = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL };
		OptionalMessageDialog dialog = new OptionalMessageDialog(parentShell, title, null, message, QUESTION, buttonLabel, 0, key, contextHelpId);
		return dialog.open();
	}

	/**
	 * Opens a info dialog with OK.
	 *
	 * @param parentShell
	 *            The shell.
	 * @param title
	 *            The title for the message dialog.
	 * @param message
	 *            The dialog message text.
	 * @param key
	 *            The unique key for the stored result value (e.g. "<PluginName>.<ActionOrDialogName>").
	 * @param contextHelpId
	 *            The optional help context id. If <code>null</code>, no help
	 *            button will be shown.
	 * @return The stored or selected result.
	 */
	public static int openInformationDialog(Shell parentShell, String title, String message, String key, String contextHelpId) {
		return openInformationDialog(parentShell, title, message, null, key, contextHelpId);
	}

	/**
	 * Opens a info dialog with OK.
	 *
	 * @param parentShell
	 *            The shell.
	 * @param title
	 *            The title for the message dialog.
	 * @param message
	 *            The dialog message text.
	 * @param buttonLabel
	 * 			  An string array listing the labels of the message dialog buttons. If <code>null</code>, the default
	 *            labeling, typically &quot;OK&quot; for a single button message dialog, will be applied.
	 * @param key
	 *            The unique key for the stored result value (e.g. "<PluginName>.<ActionOrDialogName>").
	 * @param contextHelpId
	 *            The optional help context id. If <code>null</code>, no help
	 *            button will be shown.
	 * @return The stored or selected result.
	 */
	public static int openInformationDialog(Shell parentShell, String title, String message, String[] buttonLabel, String key, String contextHelpId) {
		if (buttonLabel == null) buttonLabel = new String[] { IDialogConstants.OK_LABEL };
		OptionalMessageDialog dialog = new OptionalMessageDialog(parentShell, title, null, message, INFORMATION, buttonLabel, 0, key, contextHelpId);
		return dialog.open();
	}

	/**
	 * Opens a warning dialog with OK.
	 *
	 * @param parentShell
	 *            The shell.
	 * @param title
	 *            The title for the message dialog.
	 * @param message
	 *            The dialog message text.
	 * @param key
	 *            The unique key for the stored result value (e.g. "<PluginName>.<ActionOrDialogName>").
	 * @param contextHelpId
	 *            The optional help context id. If <code>null</code>, no help
	 *            button will be shown.
	 * @return The stored or selected result.
	 */
	public static int openWarningDialog(Shell parentShell, String title, String message, String key, String contextHelpId) {
		return openWarningDialog(parentShell, title, message, null, key, contextHelpId);
	}

	/**
	 * Opens a warning dialog with OK.
	 *
	 * @param parentShell
	 *            The shell.
	 * @param title
	 *            The title for the message dialog.
	 * @param message
	 *            The dialog message text.
	 * @param buttonLabel
	 * 			  An string array listing the labels of the message dialog buttons. If <code>null</code>, the default
	 *            labeling, typically &quot;OK&quot; for a single button message dialog, will be applied.
	 * @param key
	 *            The unique key for the stored result value (e.g. "<PluginName>.<ActionOrDialogName>").
	 * @param contextHelpId
	 *            The optional help context id. If <code>null</code>, no help
	 *            button will be shown.
	 * @return The stored or selected result.
	 */
	public static int openWarningDialog(Shell parentShell, String title, String message, String[] buttonLabel, String key, String contextHelpId) {
		if (buttonLabel == null) buttonLabel = new String[] { IDialogConstants.OK_LABEL };
		OptionalMessageDialog dialog = new OptionalMessageDialog(parentShell, title, null, message, WARNING, buttonLabel, 0, key, contextHelpId);
		return dialog.open();
	}

	/**
	 * Opens a error dialog with OK.
	 *
	 * @param parentShell
	 *            The shell.
	 * @param title
	 *            The title for the message dialog.
	 * @param message
	 *            The dialog message text.
	 * @param key
	 *            The unique key for the stored result value (e.g. "<PluginName>.<ActionOrDialogName>").
	 * @param contextHelpId
	 *            The optional help context id. If <code>null</code>, no help
	 *            button will be shown.
	 * @return The stored or selected result.
	 */
	public static int openErrorDialog(Shell parentShell, String title, String message, String key, String contextHelpId) {
		return openErrorDialog(parentShell, title, message, null, key, contextHelpId);
	}

	/**
	 * Opens a error dialog with OK.
	 *
	 * @param parentShell
	 *            The shell.
	 * @param title
	 *            The title for the message dialog.
	 * @param message
	 *            The dialog message text.
	 * @param buttonLabel
	 * 			  An string array listing the labels of the message dialog buttons. If <code>null</code>, the default
	 *            labeling, typically &quot;OK&quot; for a single button message dialog, will be applied.
	 * @param key
	 *            The unique key for the stored result value (e.g. "<PluginName>.<ActionOrDialogName>").
	 * @param contextHelpId
	 *            The optional help context id. If <code>null</code>, no help
	 *            button will be shown.
	 * @return The stored or selected result.
	 */
	public static int openErrorDialog(Shell parentShell, String title, String message, String[] buttonLabel, String key, String contextHelpId) {
		if (buttonLabel == null) buttonLabel = new String[] { IDialogConstants.OK_LABEL };
		OptionalMessageDialog dialog = new OptionalMessageDialog(parentShell, title, null, message, ERROR, buttonLabel, 0, key, contextHelpId);
		return dialog.open();
	}

	/*
	 * Get the dialog settings section or create it when it is not available.
	 */
	private static IDialogSettings getDialogSettings() {
		IDialogSettings settings = UIPlugin.getDefault().getDialogSettings();
		settings = settings.getSection(DIALOG_ID);
		if (settings == null)
			settings = UIPlugin.getDefault().getDialogSettings().addNewSection(DIALOG_ID);
		return settings;
	}

	/**
	 * Get the stored result for this key. If the dialog should be opened, -1
	 * will be returned.
	 *
	 * @param key
	 *            The key for the stored result.
	 * @return The stored result or -1 of the dialog should be opened.
	 */
	public static int getDialogResult(String key) {
		IDialogSettings settings = getDialogSettings();
		try {
			return settings.getInt(key + ".result"); //$NON-NLS-1$
		}
		catch (NumberFormatException e) {
		}
		return -1;
	}

	/**
	 * Get the stored toggle state for this key.
	 * If no state is stored, <code>false</code> will be returned.
	 *
	 * @param key
	 *            The key for the stored toggle state.
	 * @return The stored result or -1 of the dialog should be opened.
	 */
	public static boolean getDialogToggleState(String key) {
		IDialogSettings settings = getDialogSettings();
		return settings.getBoolean(key + ".toggleState"); //$NON-NLS-1$
	}

	/**
	 * Get the stored info for this key.
	 *
	 * @param key
	 *            The key for the stored info.
	 * @return The stored info or <code>null</code>.
	 */
	public static String getAdditionalDialogInfo(String key) {
		IDialogSettings settings = getDialogSettings();
		return settings.get(key + ".additionalInfo"); //$NON-NLS-1$
	}

	/**
	 * Set the dialog result for this key. If the result is < 0, the string
	 * "PROMPT" will be stored.
	 *
	 * @param key
	 *            The key to store the result.
	 * @param result
	 *            The result that should be stored.
	 */
	public static void setDialogResult(String key, int result) {
		IDialogSettings settings = getDialogSettings();
		if (result < 0) {
			settings.put(key + ".result", PROMPT); //$NON-NLS-1$
		}
		else {
			settings.put(key + ".result", result); //$NON-NLS-1$
		}
	}

	/**
	 * Set the dialog toggle state for this key.
	 *
	 * @param key
	 *            The key to store the toggle state.
	 * @param result
	 *            The toggle state that should be stored.
	 */
	public static void setDialogToggleState(String key, boolean state) {
		IDialogSettings settings = getDialogSettings();
		settings.put(key + ".toggleState", state); //$NON-NLS-1$
	}

	/**
	 * Set additional info for this key.
	 *
	 * @param key
	 *            The key to store the additional info.
	 * @param value
	 *            The additional info that should be stored.
	 */
	public static void setAdditionalDialogInfo(String key, String value) {
		IDialogSettings settings = getDialogSettings();
		settings.put(key + ".additionalInfo", value); //$NON-NLS-1$
	}

	/**
	 * Clears all stored information for this dialogs
	 */
	public static void clearAllRememberedStates() {
		IDialogSettings settings = UIPlugin.getDefault().getDialogSettings();
		settings.addNewSection(DIALOG_ID);
	}
}
