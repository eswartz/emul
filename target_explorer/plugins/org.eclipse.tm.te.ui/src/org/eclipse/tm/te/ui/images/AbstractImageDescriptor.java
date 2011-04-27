/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.images;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * Target Explorer: Image descriptor for creating overlays.
 */
public abstract class AbstractImageDescriptor extends CompositeImageDescriptor {

	private String fKey;
	private ImageRegistry fRegistry;

	public AbstractImageDescriptor(ImageRegistry reg) {
		fRegistry = reg;
	}

	protected void setKey(String key) {
		fKey = key;
	}

	public String getKey() {
		return fKey;
	}

	protected ImageRegistry getRegistry() {
		return fRegistry;
	}

	protected void drawCentered(String key, int width, int height) {
		drawCentered(fRegistry.get(key), width, height);
	}

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

	protected void drawCenterRight(String key, int width, int height) {
		Image baseImage = fRegistry.get(key);
		if (baseImage != null) {
			ImageData imageData = baseImage.getImageData();
			if (imageData != null) {
				int x = StrictMath.max(0, width - imageData.width);
				int y = StrictMath.max(0, (height - imageData.height + 1) / 2);
				drawImage(imageData, x, y);
			}
		}
	}

	protected void drawTopLeft(String key) {
		Image baseImage = fRegistry.get(key);
		if (baseImage != null) {
			ImageData imageData = baseImage.getImageData();
			if (imageData != null) {
				drawImage(imageData, 0, 0);
			}
		}
	}

	protected void drawTopRight(String key, int width, int height) {
		Image baseImage = fRegistry.get(key);
		if (baseImage != null) {
			ImageData imageData = baseImage.getImageData();
			if (imageData != null) {
				int x = StrictMath.max(0, width - imageData.width);
				drawImage(imageData, x, 0);
			}
		}
	}

	protected void drawBottomCenter(String key, int width, int height) {
		Image image = fRegistry.get(key);
		if (image != null) {
			ImageData imageData = image.getImageData();
			if (imageData != null) {
				int x = StrictMath.max(0, (width - imageData.width + 1) / 2);
				int y = StrictMath.max(0, height - imageData.height);
				drawImage(imageData, x, y);
			}
		}
	}

	protected void drawBottomLeft(String key) {
		if (getSize() != null) {
			Point size = getSize();
			drawBottomLeft(key, size.x, size.y);
		} else {
			// the default eclipse style guide recommendation is 16x16
			drawBottomLeft(key, 16, 16);
		}
	}

	protected void drawBottomLeft(String key, int width, int height) {
		Image image = fRegistry.get(key);
		if (image != null) {
			ImageData imageData = image.getImageData();
			if (imageData != null) {
				int y = StrictMath.max(0, height - imageData.height);
				drawImage(imageData, 0, y);
			}
		}
	}

	protected void drawCenterLeft(String key, int width, int height) {
		Image image = fRegistry.get(key);
		if (image != null) {
			ImageData imageData = image.getImageData();
			if (imageData != null) {
				int y = StrictMath.max(0, (height - imageData.height) / 2);
				drawImage(imageData, 0, y);
			}
		}
	}

	protected void drawBottomRight(String key) {
		if (getSize() != null) {
			Point size = getSize();
			drawBottomRight(key, size.x, size.y);
		} else {
			// the default eclipse style guide recommendation is 16x16
			drawBottomRight(key, 16, 16);
		}
	}

	protected void drawBottomRight(String key, int width, int height) {
		Image image = fRegistry.get(key);
		if (image != null) {
			ImageData imageData = image.getImageData();
			if (imageData != null) {
				int x = StrictMath.max(0, width - imageData.width);
				int y = StrictMath.max(0, height - imageData.height);
				drawImage(imageData, x, y);
			}
		}
	}

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

	/**
	 * Returns the base image used for the combined image description. This
	 * method is called from <code>getTransparentPixel()</code> to query the
	 * transparent color of the palette.
	 *
	 * @return The base image or <code>null</code> if none.
	 */
	protected abstract Image getBaseImage();
}
