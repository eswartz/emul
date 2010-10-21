/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.target.ui.editors;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.internal.target.ui.Activator;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.tcf.target.core.ITargetListener;
import org.eclipse.tcf.target.core.TargetEvent;
import org.eclipse.tcf.target.core.TargetEvent.EventType;
import org.eclipse.tcf.target.ui.ITargetEditorInput;
import org.eclipse.tcf.target.ui.ITargetPage;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.ui.part.MultiPageEditorPart;

/**
 * An editor to show information about a target.
 * 
 * @author Doug Schaefer
 */
public class TargetEditor extends MultiPageEditorPart implements ITargetListener {

	public static final String EDITOR_ID = "org.eclipse.tcf.target.editors.TargetEditor";
	
	private List servicesList;
	
	private void createAttributesPage() {
		final Composite composite = new Composite(getContainer(), SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		layout.numColumns = 2;

		Protocol.invokeLater(new Runnable() {
			@Override
			public void run() {
				final ITarget target = ((ITargetEditorInput)getEditorInput()).getTarget();
				target.handleTargetRequest(new ITarget.ITargetRequest() {
					@Override
					public void execute(IChannel channel) {
						final Map<String, String> attributes = channel.getRemotePeer().getAttributes();
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								for (Entry<String, String> entry : attributes.entrySet())
									addAttribute(composite, entry.getKey(), entry.getValue());
								
								composite.layout(true);
							};
						});
					}
					
					@Override
					public void channelUnavailable(IStatus error) {
					}
				});
			}
		});
		
		int index = addPage(composite);
		setPageText(index, "Attributes");
	}
	
	private void addAttribute(Composite parent, String key, String value) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(key);
		label.setLayoutData(new GridData(SWT.TRAIL, SWT.CENTER, false, false));
		
		Text text = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		text.setText(value);
		text.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
	}
	
	private void createServicesPage() {
		Composite composite = new Composite(getContainer(), SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);

		servicesList = new List(composite, SWT.BORDER);
		servicesList.add("<Pending...>");
		servicesList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Protocol.invokeLater(new Runnable() {
			@Override
			public void run() {
				ITarget target = ((ITargetEditorInput)getEditorInput()).getTarget();
				target.handleTargetRequest(new ITarget.ITargetRequest() {
					@Override
					public void execute(IChannel channel) {
						final Collection<String> services = channel.getRemoteServices();
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								servicesList.removeAll();
								for (String service : services)
									servicesList.add(service);
							}
						});
					}
					
					@Override
					public void channelUnavailable(IStatus error) {
						Activator.log(error);
					}
				});
			}
		});
		
		int index = addPage(composite);
		setPageText(index, "Services");
	}

	protected void createPages() {
		createAttributesPage();
		createServicesPage();
		
		IExtensionRegistry extReg = Platform.getExtensionRegistry();
		IExtensionPoint extPoint = extReg.getExtensionPoint(Activator.PLUGIN_ID + ".targetPage");
		IExtension[] exts = extPoint.getExtensions();
		for (IExtension ext : exts) {
			IConfigurationElement[] elements = ext.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				IConfigurationElement[] children = element.getChildren();
				// Should only be one - enablement
				try {
					Expression expression = ExpressionConverter.getDefault().perform(children[0]);
					ITarget target = ((ITargetEditorInput)getEditorInput()).getTarget();
					EvaluationContext context = new EvaluationContext(null, target);
					if (expression.evaluate(context).equals(EvaluationResult.TRUE)) {
						ITargetPage page = (ITargetPage)element.createExecutableExtension("class");
						if (page != null) {
							int index = addPage(page.createPage(getContainer(), target));
							setPageText(index, page.getPageText());
						}
					}
				} catch (CoreException e) {
					Activator.log(e.getStatus());
				}
			}
		}
		
		ITarget target = ((ITargetEditorInput)getEditorInput()).getTarget();
		setPartName(target.getShortName());
		target.addTargetListener(this);
	}

	@Override
	public void handleEvent(TargetEvent event) {
		if (event.getEventType() == EventType.DELETED)
			// target is gone, close the editor
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					getEditorSite().getPage().closeEditor(TargetEditor.this, false);
				}
			});
	}
	
	/**
	 * Calculates the contents of page 2 when the it is activated.
	 */
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if (newPageIndex == 2) {
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// Nothing to save
	}
	
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	@Override
	public void doSaveAs() {
		// Nothing here either
	}
	
}
