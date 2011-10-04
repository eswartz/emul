/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.navigator.images;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
import org.eclipse.tm.te.tcf.ui.internal.ImageConsts;
import org.eclipse.tm.te.ui.jface.images.AbstractImageDescriptor;


/**
 * Target Explorer: Peer model node image descriptor implementation.
 */
public class PeerImageDescriptor extends AbstractImageDescriptor {
	// the base image to decorate with overlays
	private Image baseImage;
	// the image size
	private Point imageSize;

	// Flags representing the object states to decorate
	private int state;

	/**
	 * Constructor.
	 */
	public PeerImageDescriptor(final ImageRegistry registry, final Image baseImage, final IPeerModel node) {
		super(registry);

		this.baseImage = baseImage;
		imageSize = new Point(baseImage.getImageData().width, baseImage.getImageData().height);

		// Determine the current object state to decorate
		if (Protocol.isDispatchThread()) {
			initialize(node);
		} else {
			Protocol.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					initialize(node);
				}
			});
		}

		// build up the key for the image registry
		defineKey(baseImage.hashCode());
	}

	/**
	 * Initialize the image descriptor from the peer model.
	 *
	 * @param node The peer model. Must not be <code>null</code>.
	 */
	protected void initialize(IPeerModel node) {
		Assert.isNotNull(node);
		Assert.isTrue(Protocol.isDispatchThread());

		state = node.getIntProperty(IPeerModelProperties.PROP_STATE);
	}

	protected void defineKey(int hashCode) {
		String key = "PMID:" +  //$NON-NLS-1$
			hashCode + ":" + //$NON-NLS-1$
			state;

		setDecriptorKey(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int, int)
	 */
	@Override
	protected void drawCompositeImage(int width, int height) {
		drawCentered(baseImage, width, height);

		if (state == IPeerModelProperties.STATE_UNKNOWN) { /* unknown */
			drawBottomRight(ImageConsts.GREY_OVR);
		}
		else if (state == IPeerModelProperties.STATE_REACHABLE) { /* not connected, but reachable */
			drawBottomRight(ImageConsts.GOLD_OVR);
		}
		else if (state == IPeerModelProperties.STATE_CONNECTED) { /* connected */
			drawBottomRight(ImageConsts.GREEN_OVR);
		}
		else if (state == IPeerModelProperties.STATE_NOT_REACHABLE) { /* not connected, not reachable */
			drawBottomRight(ImageConsts.RED_OVR);
		}
		else if (state == IPeerModelProperties.STATE_ERROR) { /* not connected, error */
			drawBottomRight(ImageConsts.RED_X_OVR);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
	 */
	@Override
	protected Point getSize() {
		return imageSize;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.ide.util.ui.AbstractImageDescriptor#getBaseImage()
	 */
	@Override
	protected Image getBaseImage() {
		return baseImage;
	}
}
