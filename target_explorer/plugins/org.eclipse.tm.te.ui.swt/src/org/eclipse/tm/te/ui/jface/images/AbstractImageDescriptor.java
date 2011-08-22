/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.jface.images;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * Target Explorer: Extended composite image descriptor.
 * <p>
 * The image descriptor implementation adds method for easily drawing overlay
 * images on different positions on top of a base image.
 */
public abstract class AbstractImageDescriptor extends CompositeImageDescriptor {
	// The parent image registry providing the images for drawing
	private final ImageRegistry parentImageRegistry;

	// The image descriptor key
	private String descriptorKey = null;

	/**
	 * Constructor.
	 *
	 * @param parent The parent image registry. Must not be <code>null</code>.
	 */
	public AbstractImageDescriptor(ImageRegistry parent) {
		super();

		Assert.isNotNull(parent);
		parentImageRegistry = parent;
	}

	/**
	 * Returns the parent image registry.
	 *
	 * @return The parent image registry instance.
	 */
	protected final ImageRegistry getParentImageRegistry() {
		return parentImageRegistry;
	}

	/**
	 * Set the image descriptor key.
	 *
	 * @param key The image descriptor key. Must not be <code>null</code>.
	 */
	protected final void setDecriptorKey(String key) {
		Assert.isNotNull(key);
		descriptorKey = key;
	}

	/**
	 * Returns the image descriptor key.
	 *
	 * @return The image descriptor key, or <code>null</code> if not set.
	 */
	public final String getDecriptorKey() {
		return descriptorKey;
	}

	/**
	 * Draw the image, found under the specified key, centered within the
	 * rectangle given by width x height.
	 *
	 * @param key The image key. Must not be <code>null</code>.
	 * @param width The width of the rectangle to center the image in.
	 * @param height The height of the rectangle to center the image in.
	 */
	protected void drawCentered(String key, int width, int height) {
		Assert.isNotNull(key);
		drawCentered(parentImageRegistry.get(key), width, height);
	}

	/**
	 * Draw the given image centered within the rectangle
	 * defined by the specified width x height.
	 *
	 * @param image The image. Must not be <code>null</code>.
	 * @param width The width of the rectangle to center the image in.
	 * @param height The height of the rectangle to center the image in.
	 */
	protected void drawCentered(Image image, int width, int height) {
		if (image != null) {
			ImageData imageData = image.getImageData();
			if (imageData != null) {
				int x = StrictMath.max(0, (width - imageData.width + 1) / 2);
				int y = StrictMath.max(0, (height - imageData.height + 1) / 2);
				drawImage(imageData, x, y);
			}
		}
	}

	/**
	 * Draw the overlay image, found under the specified key in the parent
	 * image registry, centered right on top of the base image.
	 *
	 * @param key The overlay image key. Must not be <code>null</code>.
	 * @param width The width of the overlay image.
	 * @param height The height of the overlay image.
	 */
	protected void drawCenterRight(String key, int width, int height) {
		Image baseImage = parentImageRegistry.get(key);
		if (baseImage != null) {
			ImageData imageData = baseImage.getImageData();
			if (imageData != null) {
				int x = StrictMath.max(0, width - imageData.width);
				int y = StrictMath.max(0, (height - imageData.height + 1) / 2);
				drawImage(imageData, x, y);
			}
		}
	}

	/**
	 * Draw the overlay image, found under the specified key in the parent
	 * image registry, top left on top of the base image.
	 *
	 * @param key The overlay image key. Must not be <code>null</code>.
	 */
	protected void drawTopLeft(String key) {
		Image baseImage = parentImageRegistry.get(key);
		if (baseImage != null) {
			ImageData imageData = baseImage.getImageData();
			if (imageData != null) {
				drawImage(imageData, 0, 0);
			}
		}
	}

	/**
	 * Draw the overlay image, found under the specified key in the parent
	 * image registry, top right on top of the base image.
	 *
	 * @param key The overlay image key. Must not be <code>null</code>.
	 * @param width The width of the overlay image.
	 * @param height The height of the overlay image.
	 */
	protected void drawTopRight(String key, int width, int height) {
		Image baseImage = parentImageRegistry.get(key);
		if (baseImage != null) {
			ImageData imageData = baseImage.getImageData();
			if (imageData != null) {
				int x = StrictMath.max(0, width - imageData.width);
				drawImage(imageData, x, 0);
			}
		}
	}

	/**
	 * Draw the overlay image, found under the specified key in the parent
	 * image registry, bottom center on top of the base image.
	 *
	 * @param key The overlay image key. Must not be <code>null</code>.
	 * @param width The width of the overlay image.
	 * @param height The height of the overlay image.
	 */
	protected void drawBottomCenter(String key, int width, int height) {
		Image image = parentImageRegistry.get(key);
		if (image != null) {
			ImageData imageData = image.getImageData();
			if (imageData != null) {
				int x = StrictMath.max(0, (width - imageData.width + 1) / 2);
				int y = StrictMath.max(0, height - imageData.height);
				drawImage(imageData, x, y);
			}
		}
	}

	/**
	 * Draw the overlay image, found under the specified key in the parent
	 * image registry, bottom left on top of the base image.
	 *
	 * @param key The overlay image key. Must not be <code>null</code>.
	 * @param width The width of the overlay image.
	 * @param height The height of the overlay image.
	 */
	protected void drawBottomLeft(String key, int width, int height) {
		Image image = parentImageRegistry.get(key);
		if (image != null) {
			ImageData imageData = image.getImageData();
			if (imageData != null) {
				int y = StrictMath.max(0, height - imageData.height);
				drawImage(imageData, 0, y);
			}
		}
	}

	/**
	 * Draw the overlay image, found under the specified key in the parent
	 * image registry, center left on top of the base image.
	 *
	 * @param key The overlay image key. Must not be <code>null</code>.
	 * @param width The width of the overlay image.
	 * @param height The height of the overlay image.
	 */
	protected void drawCenterLeft(String key, int width, int height) {
		Image image = parentImageRegistry.get(key);
		if (image != null) {
			ImageData imageData = image.getImageData();
			if (imageData != null) {
				int y = StrictMath.max(0, (height - imageData.height) / 2);
				drawImage(imageData, 0, y);
			}
		}
	}

	/**
	 * Draw the overlay image, found under the specified key in the parent
	 * image registry, bottom right on top of the base image.
	 *
	 * @param key The overlay image key. Must not be <code>null</code>.
	 */
	protected void drawBottomRight(String key) {
		if (getSize() != null) {
			Point size = getSize();
			drawBottomRight(key, size.x, size.y);
		} else {
			// the default eclipse style guide recommendation is 16x16
			drawBottomRight(key, 16, 16);
		}
	}

	/**
	 * Draw the overlay image, found under the specified key in the parent
	 * image registry, bottom right on top of the base image.
	 *
	 * @param key The overlay image key. Must not be <code>null</code>.
	 * @param width The width of the overlay image.
	 * @param height The height of the overlay image.
	 */
	protected void drawBottomRight(String key, int width, int height) {
		Image image = parentImageRegistry.get(key);
		if (image != null) {
			ImageData imageData = image.getImageData();
			if (imageData != null) {
				int x = StrictMath.max(0, width - imageData.width);
				int y = StrictMath.max(0, height - imageData.height);
				drawImage(imageData, x, y);
			}
		}
	}

	/**
	 * Returns the base image used for the combined image description. This
	 * method is called from <code>getTransparentPixel()</code> to query the
	 * transparent color of the palette.
	 *
	 * @return The base image or <code>null</code> if none.
	 */
	protected abstract Image getBaseImage();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#getTransparentPixel()
	 */
	@Override
	protected int getTransparentPixel() {
		Image baseImage = getBaseImage();
		if (baseImage != null && baseImage.getImageData() != null) {
			return baseImage.getImageData().transparentPixel;
		}
		return super.getTransparentPixel();
	}
}
