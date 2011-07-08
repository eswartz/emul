/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.tables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
import org.eclipse.tm.te.tcf.ui.internal.nls.Messages;
import org.eclipse.tm.te.ui.tables.TableNode;
import org.eclipse.ui.forms.widgets.Section;


/**
 * Target Explorer: TCF node properties table content provider implementation.
 */
public class NodePropertiesContentProvider implements IStructuredContentProvider {

	/**
	 * The list of properties to filter out and not to show within the table.
	 */
	protected final static String[] FILTERED_PROPERTIES = new String[] {
		"name", "typeLabel", //$NON-NLS-1$ //$NON-NLS-2$
		"instance", "childrenQueried", //$NON-NLS-1$ //$NON-NLS-2$
		IPeerModelProperties.PROP_CHANNEL_REF_COUNTER,
		"hasTabularProperties" //$NON-NLS-1$
	};

	// Flag to control if the content provide may update the parent section title
	private final boolean updateParentSectionTitle;

	/**
	 * Constructor.
	 *
	 * @param updateParentSectionTitle Specify <code>true</code> to allow the content provider to update
	 *                                 the parent section title, <code>false</code> if no title update is desired.
	 */
	public NodePropertiesContentProvider(boolean updateParentSectionTitle) {
		this.updateParentSectionTitle = updateParentSectionTitle;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(final Object inputElement) {
		List<TableNode> nodes = new ArrayList<TableNode>();

		if (inputElement instanceof IPeerModel) {
			TableNode lastErrorNode = null;

			// Get all custom properties of the node
			final Map<String, Object> properties = new HashMap<String, Object>();
			// And get all native properties of the peer
			if (Protocol.isDispatchThread()) {
				properties.putAll(((IPeerModel)inputElement).getProperties());
				properties.putAll(((IPeerModel)inputElement).getPeer().getAttributes());
			} else {
				Protocol.invokeAndWait(new Runnable() {
					public void run() {
						properties.putAll(((IPeerModel)inputElement).getProperties());
						properties.putAll(((IPeerModel)inputElement).getPeer().getAttributes());
					}
				});
			}

			for (String name : properties.keySet()) {
				// Check if the property is filtered
				if (name.endsWith(".silent") || Arrays.asList(FILTERED_PROPERTIES).contains(name)) continue; //$NON-NLS-1$
				// Create the properties node, if not one of the services nodes
				if (!IPeerModelProperties.PROP_LOCAL_SERVICES.equals(name) && !IPeerModelProperties.PROP_REMOTE_SERVICES.equals(name)) {
					TableNode propertiesNode = new TableNode(name, properties.get(name) != null ? properties.get(name).toString() : ""); //$NON-NLS-1$
					if (!IPeerModelProperties.PROP_LAST_SCANNER_ERROR.equals(name)) nodes.add(propertiesNode);
					else lastErrorNode = propertiesNode;
				} else {
					// For the services nodes, additional nodes might be necessary to make
					// reading all the service names in the table easier
					String services = properties.get(name) != null ? properties.get(name).toString() : ""; //$NON-NLS-1$
					if (services.split(",").length > 6) { //$NON-NLS-1$
						// More than 6 services listed -> generate nodes with 6 service names each
						String[] serviceNames = services.split(","); //$NON-NLS-1$
						boolean withName = true;
						StringBuilder nodeValue = new StringBuilder();
						int counter = 1;
						for (String serviceName : serviceNames) {
							nodeValue.append(serviceName.trim());
							nodeValue.append(", "); //$NON-NLS-1$
							if (counter < 6) {
								counter++;
							} else {
								TableNode propertiesNode = new TableNode(withName ? name : "\t", nodeValue.toString()); //$NON-NLS-1$
								nodes.add(propertiesNode);
								if (withName) withName = false;
								counter = 1;
								nodeValue = new StringBuilder();
							}
						}
						// Anything left in the string builder?
						if (nodeValue.toString().trim().length() > 0) {
							String value = nodeValue.toString();
							if (value.endsWith(", ")) value = value.substring(0, value.length() - 2); //$NON-NLS-1$
							if (value.trim().length() > 0) {
								TableNode propertiesNode = new TableNode(withName ? name : "\t", value); //$NON-NLS-1$
								nodes.add(propertiesNode);
							}
						}

					} else {
						// Less than 6 service names listed -> generate a single node
						TableNode propertiesNode = new TableNode(name, services);
						nodes.add(propertiesNode);
					}
				}
			}

			if (lastErrorNode != null) {
				// Add an empty line before the error
				TableNode propertiesNode = new TableNode("", ""); //$NON-NLS-1$ //$NON-NLS-2$
				nodes.add(propertiesNode);
				nodes.add(lastErrorNode);
			}
		}

		return nodes.toArray(new TableNode[nodes.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Do nothing if we shall not update the section title
		if (!updateParentSectionTitle) return;

		String sectionTitle = null;
		Object element = null;

		// If the input is a tree selection, extract the element from the tree path
		if (newInput instanceof ITreeSelection && !((ITreeSelection)newInput).isEmpty()) {
			// Cast to the correct type
			ITreeSelection selection = (ITreeSelection)newInput;
			// Get the selected tree pathes
			TreePath[] pathes = selection.getPaths();
			// If there are more than one elements selected, we care only about the first path
			TreePath path = pathes.length > 0 ? pathes[0] : null;
			// Get the last element within the tree path
			element = path != null ? path.getLastSegment() : null;
		}

		// If the input is a peer model node, set it directly
		if (newInput instanceof IPeerModel) element = newInput;

		// Determine the section header text
		if (element instanceof IPeerModel) {
			sectionTitle = NLS.bind(org.eclipse.tm.te.ui.nls.Messages.NodePropertiesTableControl_section_title, Messages.NodePropertiesContentProvider_peerNode_sectionTitle);
		}

		// Set the standard (no selection) section title if none could be determined
		if (sectionTitle == null || "".equals(sectionTitle.trim())) sectionTitle = org.eclipse.tm.te.ui.nls.Messages.NodePropertiesTableControl_section_title_noSelection; //$NON-NLS-1$
		// Stretch to a length of 40 characters to make sure the title can be changed
		// to hold and show text up to this length
		while (sectionTitle.length() < 40) sectionTitle += " "; //$NON-NLS-1$

		// Find the parent section the node properties tables is embedded in
		Control control = viewer.getControl();
		while (control != null && !control.isDisposed()) {
			if (control instanceof Section) {
				Section section = (Section)control;
				// We cannot get access to the Label control used to set the text, so just catch the
				// probably SWTException
				try { section.setText(sectionTitle); } catch(SWTException e) { /* ignored on purpose */ }
				break;
			}
			control = control.getParent();
		}
	}
}
