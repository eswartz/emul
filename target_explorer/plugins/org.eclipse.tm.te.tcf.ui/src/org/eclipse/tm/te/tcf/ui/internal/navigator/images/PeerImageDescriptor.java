/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.navigator.images;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
import org.eclipse.tm.te.tcf.ui.internal.registries.InternalImageRegistry;
import org.eclipse.tm.te.ui.images.AbstractImageDescriptor;


/**
 * Target Explorer: Peer model node image descriptor implementation.
 */
public class PeerImageDescriptor extends AbstractImageDescriptor {
	// the base image to decorate with overlays
	private Image fBaseImage;
	// the image size
	private Point fImageSize;

	// Flags representing the object states to decorate
	private int fState;

	/**
	 * Constructor.
	 */
	public PeerImageDescriptor(final ImageRegistry registry, final Image baseImage, final IPeerModel node) {
		super(registry);

		fBaseImage = baseImage;
		fImageSize = new Point(fBaseImage.getImageData().width, fBaseImage.getImageData().height);

		// Determine the current object state to decorate
		if (Protocol.isDispatchThread()) {
			initialize(node);
		} else {
			Protocol.invokeAndWait(new Runnable() {
				public void run() {
					initialize(node);
				}
			});
		}

		// build up the key for the image registry
		defineKey(fBaseImage.hashCode());
	}

	/**
	 * Initialize the image descriptor from the peer model.
	 *
	 * @param node The peer model. Must not be <code>null</code>.
	 */
	protected void initialize(IPeerModel node) {
		assert Protocol.isDispatchThread() && node != null;

		fState = node.getIntProperty(IPeerModelProperties.PROP_STATE);
	}

	protected void defineKey(int hashCode) {
		String key = "PMID:" +  //$NON-NLS-1$
			hashCode + ":" + //$NON-NLS-1$
			fState;

		setKey(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int, int)
	 */
	@Override
	protected void drawCompositeImage(int width, int height) {
		drawCentered(fBaseImage, width, height);

		if (fState == IPeerModelProperties.STATE_UNKNOWN) { /* unknown */
			drawBottomRight(InternalImageRegistry.OVERLAY_Grey);
		}
		else if (fState == IPeerModelProperties.STATE_REACHABLE) { /* not connected, but reachable */
			drawBottomRight(InternalImageRegistry.OVERLAY_Gold);
		}
		else if (fState == IPeerModelProperties.STATE_CONNECTED) { /* connected */
			drawBottomRight(InternalImageRegistry.OVERLAY_Green);
		}
		else if (fState == IPeerModelProperties.STATE_NOT_REACHABLE) { /* not connected, not reachable */
			drawBottomRight(InternalImageRegistry.OVERLAY_Red);
		}
		else if (fState == IPeerModelProperties.STATE_ERROR) { /* not connected, error */
			drawBottomRight(InternalImageRegistry.OVERLAY_RedX);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
	 */
	@Override
	protected Point getSize() {
		return fImageSize;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.ide.util.ui.AbstractImageDescriptor#getBaseImage()
	 */
	@Override
	protected Image getBaseImage() {
		return fBaseImage;
	}
}
