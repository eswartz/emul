/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.decorators.images;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.tm.te.core.model.interfaces.IConnectable;
import org.eclipse.tm.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tm.te.ui.interfaces.ImageConsts;
import org.eclipse.tm.te.ui.jface.images.AbstractImageDescriptor;


/**
 * Connectable node image descriptor implementation.
 */
public class ConnectableImageDescriptor extends AbstractImageDescriptor {
	// the base image to decorate with overlays
	private Image baseImage;
	// the image size
	private Point imageSize;

	// Flags representing the object states to decorate
	private int connectState;
	private int connectSubState;
	private boolean pending;

	/**
	 * Constructor.
	 *
	 * @param registry The image registry. Must not be <code>null</code>.
	 * @param baseImage The base image. Must not be <code>null</code>.
	 * @param node The connectable node. Must not be <code>null</code>.
	 */
	public ConnectableImageDescriptor(final ImageRegistry registry, final Image baseImage, final IConnectable node) {
		super(registry);

		Assert.isNotNull(baseImage);
		this.baseImage = baseImage;
		imageSize = new Point(baseImage.getImageData().width, baseImage.getImageData().height);

		// invoke initialize
		invokeInitialize(node);

		// build up the key for the image registry
		defineKey(baseImage.hashCode());
	}

	/**
	 * Invoke the initialize method.
	 * <p>
	 * Called from the constructor to initialize the image descriptor. Sub classes
	 * can overwrite this method to implement necessary thread-safety.
	 *
	 * @param node The connectable node. Must not be <code>null</code>.
	 */
	protected void invokeInitialize(IConnectable node) {
		Assert.isNotNull(node);
		initialize(node);
	}

	/**
	 * Initialize the image descriptor from the connectable node.
	 *
	 * @param node The target node. Must not be <code>null</code>.
	 */
	protected void initialize(IConnectable node) {
		Assert.isNotNull(node);

		connectState = node.getConnectState();
		connectSubState = node.getConnectSubState();
		pending = node instanceof IModelNode ? ((IModelNode)node).isPending() : false;
	}

	protected void defineKey(int hashCode) {
		String key = "CNID:" +  //$NON-NLS-1$
			hashCode + ":" + //$NON-NLS-1$
			connectState + ":" + //$NON-NLS-1$
			connectSubState + ":" + //$NON-NLS-1$
			pending;

		setDecriptorKey(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int, int)
	 */
	@Override
	protected void drawCompositeImage(int width, int height) {
		drawCentered(baseImage, width, height);

		if (pending || (connectState == IConnectable.STATE_UNREACHABLE && (connectSubState == IConnectable.SUB_STATE_REBOOT_MANUAL))) {
			drawTopLeft(ImageConsts.BUSY_OVR);
		}

		if (connectState == IConnectable.STATE_CONNECTING || connectState == IConnectable.STATE_DISCONNECTING) {
			drawBottomRight(ImageConsts.GOLD_OVR);
		} else if (connectState == IConnectable.STATE_UNREACHABLE) {
			drawBottomRight(ImageConsts.RED_OVR);
		} else if (connectState == IConnectable.STATE_CONNECTED) {
			drawBottomRight(ImageConsts.GREEN_OVR);
		} else if (connectState == IConnectable.STATE_UNCONNECTED) {
			drawBottomRight(ImageConsts.GREY_OVR);
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
	 * @see org.eclipse.tm.te.ui.jface.images.AbstractImageDescriptor#getBaseImage()
	 */
	@Override
	protected Image getBaseImage() {
		return baseImage;
	}
}
