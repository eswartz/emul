/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.navigator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.ui.activator.UIPlugin;
import org.eclipse.tm.te.tcf.ui.internal.ImageConsts;
import org.eclipse.tm.te.tcf.ui.internal.navigator.images.PeerImageDescriptor;
import org.eclipse.tm.te.ui.images.AbstractImageDescriptor;


/**
 * Target Explorer: Label provider delegate implementation.
 */
public class LabelProviderDelegate extends LabelProvider implements ILabelDecorator {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IPeerModel) {
			String label = null;

			final IPeer peer = ((IPeerModel)element).getPeer();
			final String[] peerName = new String[1];
			if (Protocol.isDispatchThread()) {
				peerName[0] = peer.getName();
			} else {
				Protocol.invokeAndWait(new Runnable() {
					public void run() {
						peerName[0] = peer.getName();
					}
				});
			}
			label = peerName[0];

			if (label != null && !"".equals(label.trim())) { //$NON-NLS-1$
				return label;
			}
		}

		return ""; //$NON-NLS-1$
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof IPeerModel) {
			return UIPlugin.getImage(ImageConsts.IMAGE_TARGET);
		}

		return super.getImage(element);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
	 */
	public Image decorateImage(Image image, Object element) {
		Image decoratedImage = null;

		if (image != null && element instanceof IPeerModel) {
			AbstractImageDescriptor descriptor = new PeerImageDescriptor(UIPlugin.getDefault().getImageRegistry(),
			                                                             image,
			                                                             (IPeerModel)element);
			decoratedImage = UIPlugin.getSharedImage(descriptor);
		}

		return decoratedImage;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
	 */
	public String decorateText(String text, Object element) {
		if (element instanceof IPeerModel) {
			String label = text;

			final IPeer peer = ((IPeerModel)element).getPeer();
			final StringBuilder builder = new StringBuilder(label != null && !"".equals(label.trim()) ? label.trim() : "<noname>"); //$NON-NLS-1$ //$NON-NLS-2$
			if (Protocol.isDispatchThread()) {
				doDecorateText(builder, peer);
			} else {
				Protocol.invokeAndWait(new Runnable() {
					public void run() {
						doDecorateText(builder, peer);
					}
				});
			}
			label = builder.toString();

			if (label != null && !"".equals(label.trim()) && !"<noname>".equals(label.trim())) { //$NON-NLS-1$ //$NON-NLS-2$
				return label;
			}
		}
		return null;
	}

	/**
	 * Decorate the text with some peer attributes.
	 * <p>
	 * <b>Note:</b> Must be called with the TCF event dispatch thread.
	 *
	 * @param builder The string builder to decorate. Must not be <code>null</code>.
	 * @param peer The peer. Must not be <code>null</code>.
	 */
	/* default */ void doDecorateText(StringBuilder builder, IPeer peer) {
		Assert.isNotNull(builder);
		Assert.isNotNull(peer);
		Assert.isTrue(Protocol.isDispatchThread());

		String osName = peer.getOSName();

		if (osName != null && !"".equals(osName.trim())) { //$NON-NLS-1$
			builder.append(" [" + osName.trim() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		String ip = peer.getAttributes().get(IPeer.ATTR_IP_HOST);
		String port = peer.getAttributes().get(IPeer.ATTR_IP_PORT);

		if (ip != null && !"".equals(ip.trim())) { //$NON-NLS-1$
			builder.append(" @ "); //$NON-NLS-1$
			builder.append(ip.trim());

			if (port != null && !"".equals(port.trim()) && !"1534".equals(port.trim())) { //$NON-NLS-1$ //$NON-NLS-2$
				builder.append(":"); //$NON-NLS-1$
				builder.append(port.trim());
			}
		}

	}
}
