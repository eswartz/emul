/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.forms;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Target Explorer: Custom form toolkit for using form elements within
 * dialog and wizard pages, or other containers.
 */
public class CustomFormToolkit extends PlatformObject {
	// The reference of the wrapped toolkit
	private final FormToolkit toolkit;

	/**
	 * Constructor.
	 *
	 * @param toolkit The {@link FormToolkit} instance to wrap. Must not be <code>null</code>.
	 */
	public CustomFormToolkit(FormToolkit toolkit) {
		super();
		Assert.isNotNull(toolkit);
		this.toolkit = toolkit;
	}

	/**
	 * Returns the wrapped {@link FormToolkit} instance.
	 *
	 * @return The wrapped {@link FormToolkit} instance.
	 */
	public final FormToolkit getFormToolkit() {
		return toolkit;
	}

	/**
	 * Dispose the form toolkit wrapper.
	 */
	public void dispose() {
		toolkit.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (FormToolkit.class.isAssignableFrom(adapter))
			return getFormToolkit();

		return super.getAdapter(adapter);
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
	protected int convertHeightInCharsToPixels(Control control, int chars) {
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
	 * Creates a new scrollable form container within the given parent. If
	 * <code>overwriteBackground</code> is set, the parent background color
	 * and background image is applied to the created scrollable form.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param title The form title or <code>null</code> if none.
	 * @param overwriteBackground If <code>true</code>, the parent background color and image are applied to the scrollable form.
	 *
	 * @return The scrollable form instance.
	 */
	public ScrolledForm createScrolledForm(Composite parent, String title, boolean overwriteBackground) {
		Assert.isNotNull(parent);

		// Create the scrolled form which is the scrollable container for the expandable composite
		final ScrolledForm scrollableForm = getFormToolkit().createScrolledForm(parent);

		// Overwrite background color and image if requested
		if (overwriteBackground) {
			scrollableForm.setBackground(parent.getBackground());
			scrollableForm.setBackgroundImage(parent.getBackgroundImage());
		}

		// If a title is given, set and decorate the header
		if (title != null && scrollableForm.getForm() != null) {
			scrollableForm.getForm().setText(title);
			getFormToolkit().decorateFormHeading(scrollableForm.getForm());
		}

		return scrollableForm;
	}

	/**
	 * Creates an expandable composite within the given parent scrollable form using the given title.
	 * If <code>overwriteBackground</code> is set, the parent background color and background image
	 * is applied to the created expandable composite.
	 *
	 * @param scrolledForm The parent scrolled form. Must not be <code>null</code>.
	 * @param title The expandable composite title. Must not be <code>null</code>.
	 * @param entriesToShow The number of entries to show within the expanded area. Must be greater than 0.
	 * @param expanded The initial expanded state of the expandable composite.
	 * @param overwriteBackground If <code>true</code>, the parent background color and image are applied to the expandable composite.
	 *
	 * @return The expandable composite.
	 */
	public final ExpandableComposite createExpandableComposite(final ScrolledForm scrolledForm,
	                                                           String title, final int entriesToShow,
	                                                           boolean expanded, boolean overwriteBackground) {
		Assert.isNotNull(scrolledForm);
		Assert.isNotNull(title);
		Assert.isTrue(entriesToShow > 0);

		// Create the expandable composite within the scrollable container
		final ExpandableComposite expandable = getFormToolkit().createExpandableComposite(scrolledForm.getBody(), ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
		expandable.setText(title);

		// Overwrite background color and image if requested
		if (overwriteBackground) {
			expandable.setBackground(scrolledForm.getBackground());
			expandable.setBackgroundImage(scrolledForm.getBackgroundImage());
		}

		expandable.setLayout(new GridLayout());
		expandable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create an associate an expansion listener to the expandable form
		expandable.addExpansionListener(new IExpansionListener() {
			boolean notExpanded = true;

			/* (non-Javadoc)
			 * @see org.eclipse.ui.forms.events.IExpansionListener#expansionStateChanged(org.eclipse.ui.forms.events.ExpansionEvent)
			 */
			public void expansionStateChanged(ExpansionEvent e) {
				// Always set the scrolled form to re-flow. Otherwise it wouldn't
				// re-arrange the controls following this expandable composite on
				// collapse.
				scrolledForm.reflow(true);

				// Get the shell from the scrolled form.
				Shell shell = scrolledForm.getShell();
				if (shell != null && !shell.isDisposed() && e.getState() && notExpanded) {
					// And recalculate the bounds on expand
					shell.setRedraw(false);
					Rectangle shellBounds = shell.getBounds();

					// Assume at minimum 4 controls within the expandable area.
					shellBounds.height += convertHeightInCharsToPixels(expandable, Math.max(4, entriesToShow)) + IDialogConstants.VERTICAL_SPACING;

					shell.setBounds(shellBounds);
					shell.setRedraw(true);
					notExpanded = false;
				}
			}

			/* (non-Javadoc)
			 * @see org.eclipse.ui.forms.events.IExpansionListener#expansionStateChanging(org.eclipse.ui.forms.events.ExpansionEvent)
			 */
			public void expansionStateChanging(ExpansionEvent e) {
			}
		});

		// Create the client area the caller can use as parent for the control
		Composite client = getFormToolkit().createComposite(expandable);
		client.setLayout(new GridLayout());

		// Overwrite background color and image if requested
		if (overwriteBackground) {
			client.setBackground(scrolledForm.getBackground());
			client.setBackgroundImage(scrolledForm.getBackgroundImage());
		}

		// Set the initial expansion state
		expandable.setExpanded(expanded);
		// And associated the client
		expandable.setClient(client);

		return expandable;
	}

	/**
	 * Creates an expandable section within the given parent scrollable form using the given title.
	 * If <code>overwriteBackground</code> is set, the parent background color and background image
	 * is applied to the created section.
	 *
	 * @param parent The parent scrolled form. Must not be <code>null</code>.
	 * @param title The expandable composite title. Must not be <code>null</code>.
	 * @param entriesToShow The number of entries to show within the expanded area. Must be greater than 0.
	 * @param expanded The initial expanded state of the section.
	 * @param overwriteBackground If <code>true</code>, the parent background color and image are applied to the section.
	 *
	 * @return The section.
	 */
	public final Section createSection(final ScrolledForm scrolledForm,
	                                   String title, final int entriesToShow,
	                                   boolean expanded, boolean overwriteBackground) {
		Assert.isNotNull(scrolledForm);
		Assert.isNotNull(title);
		Assert.isTrue(entriesToShow > 0);

		// Create the section within the scrollable container
		final Section section = getFormToolkit().createSection(scrolledForm.getBody(), ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
		section.setText(title);

		// Overwrite background color and image if requested
		if (overwriteBackground) {
			section.setBackground(scrolledForm.getBackground());
			section.setBackgroundImage(scrolledForm.getBackgroundImage());
		}

		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create an associate an expansion listener to the expandable form
		section.addExpansionListener(new IExpansionListener() {
			boolean notExpanded = true;

			/* (non-Javadoc)
			 * @see org.eclipse.ui.forms.events.IExpansionListener#expansionStateChanged(org.eclipse.ui.forms.events.ExpansionEvent)
			 */
			public void expansionStateChanged(ExpansionEvent e) {
				// Always set the scrolled form to re-flow. Otherwise it wouldn't
				// re-arrange the controls following this expandable composite on
				// collapse.
				scrolledForm.reflow(true);

				// Get the shell from the scrolled form.
				Shell shell = scrolledForm.getShell();
				if (shell != null && !shell.isDisposed() && e.getState() && notExpanded) {
					// And recalculate the bounds on expand
					shell.setRedraw(false);
					Rectangle shellBounds = shell.getBounds();

					// Assume at minimum 4 controls within the expandable area.
					shellBounds.height += convertHeightInCharsToPixels(section, Math.max(4, entriesToShow)) + IDialogConstants.VERTICAL_SPACING;

					shell.setBounds(shellBounds);
					shell.setRedraw(true);
					notExpanded = false;
				}
			}

			/* (non-Javadoc)
			 * @see org.eclipse.ui.forms.events.IExpansionListener#expansionStateChanging(org.eclipse.ui.forms.events.ExpansionEvent)
			 */
			public void expansionStateChanging(ExpansionEvent e) {
			}
		});

		// Create the client area the caller can use as parent for the control
		Composite client = getFormToolkit().createComposite(section);
		client.setLayout(new GridLayout());

		// Overwrite background color and image if requested
		if (overwriteBackground) {
			client.setBackground(scrolledForm.getBackground());
			client.setBackgroundImage(scrolledForm.getBackgroundImage());
		}

		// Set the initial expansion state
		section.setExpanded(expanded);
		// And associated the client
		section.setClient(client);

		return section;
	}

	/**
	 * Creates an non-expandable section within the given parent scrollable form using the given title.
	 * If <code>overwriteBackground</code> is set, the parent background color and background image
	 * is applied to the created section.
	 *
	 * @param scrolledForm The parent scrolled form. Must not be <code>null</code>.
	 * @param title The expandable composite title. Must not be <code>null</code>.
	 * @param overwriteBackground If <code>true</code>, the parent background color and image are applied to the section.
	 *
	 * @return The section.
	 */
	public final Section createSection(final ScrolledForm scrolledForm, String title, boolean overwriteBackground) {
		Assert.isNotNull(scrolledForm);
		Assert.isNotNull(title);

		// Create the section within the scrollable container
		final Section section = getFormToolkit().createSection(scrolledForm.getBody(), ExpandableComposite.TITLE_BAR | ExpandableComposite.CLIENT_INDENT);
		section.setText(title);

		// Overwrite background color and image if requested
		if (overwriteBackground) {
			section.setBackground(scrolledForm.getBackground());
			section.setBackgroundImage(scrolledForm.getBackgroundImage());
		}

		// Configure the layout
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create the client area the caller can use as parent for the control
		Composite client = getFormToolkit().createComposite(section);
		client.setLayout(new GridLayout());

		// Overwrite background color and image if requested
		if (overwriteBackground) {
			client.setBackground(section.getBackground());
			client.setBackgroundImage(section.getBackgroundImage());
		}

		// And associated the client
		section.setClient(client);

		return section;
	}

	/**
	 * Creates an non-expandable section within the given parent composite using the given title.
	 * If <code>overwriteBackground</code> is set, the parent background color and background image
	 * is applied to the created section.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param title The expandable composite title. Must not be <code>null</code>.
	 * @param overwriteBackground If <code>true</code>, the parent background color and image are applied to the section.
	 *
	 * @return The section.
	 */
	public final Section createSection(final Composite parent, String title, boolean overwriteBackground) {
		Assert.isNotNull(parent);
		Assert.isNotNull(title);

		// Create the section within the scrollable container
		final Section section = getFormToolkit().createSection(parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.CLIENT_INDENT);
		section.setText(title);

		// Overwrite background color and image if requested
		if (overwriteBackground) {
			section.setBackground(parent.getBackground());
			section.setBackgroundImage(parent.getBackgroundImage());
		}

		// Configure the layout
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create the client area the caller can use as parent for the control
		Composite client = getFormToolkit().createComposite(section);
		client.setLayout(new GridLayout());

		// Overwrite background color and image if requested
		if (overwriteBackground) {
			client.setBackground(section.getBackground());
			client.setBackgroundImage(section.getBackgroundImage());
		}

		// And associated the client
		section.setClient(client);

		return section;
	}

    /**
     * Creates a composite with a highlighted note entry and a message text.
     * This is designed to take up the full width of the page.
     *
	 * @param parent The parent composite. Must not be <code>null</code>.
     * @param title The note title. Must not be <code>null</code>.
     * @param message The note message Must not be <code>null</code>.
     * @param widthHint The note message width hint in pixel or <code>SWT.DEFAULT</code>.
	 * @param overwriteBackground If <code>true</code>, the parent background color and image are applied to the note composite.
     *
     * @return The note composite.
     */
	public final Composite createNoteComposite(Composite parent, String title, String message, int widthHint, boolean overwriteBackground) {
		Assert.isNotNull(parent);
		Assert.isNotNull(title);
		Assert.isNotNull(message);

		Composite composite = getFormToolkit().createComposite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0; layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());

		Label noteLabel = getFormToolkit().createLabel(composite, title, SWT.BOLD);
		noteLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
		noteLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		Label messageLabel = getFormToolkit().createLabel(composite, message);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = widthHint;
		messageLabel.setLayoutData(layoutData);
		messageLabel.setFont(parent.getFont());

		// Overwrite background color and image if requested
		if (overwriteBackground) {
			composite.setBackground(parent.getBackground());
			composite.setBackgroundImage(parent.getBackgroundImage());

			noteLabel.setBackground(parent.getBackground());
			noteLabel.setBackgroundImage(parent.getBackgroundImage());

			messageLabel.setBackground(parent.getBackground());
			messageLabel.setBackgroundImage(parent.getBackgroundImage());
		}

		return composite;
	}
}
