/*
  MultiImageSizeProvider.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.bars;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import v9t9.gui.client.swt.imageimport.ImageUtils;
import ejs.base.utils.Pair;



/**
 * Get an image which are available in multiple sizes
 * @author ejs
 *
 */
public class MultiImageSizeProvider implements IImageProvider {
	protected TreeMap<Integer, Image> iconMap;
	private Map<Integer, Image> scaleCache = new LinkedHashMap<Integer, Image>();
	
	/**
	 * 
	 */
	public MultiImageSizeProvider(TreeMap<Integer, Image> iconMap) {
		this.iconMap = iconMap;
	}
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.bars.IImageProvider#dispose()
	 */
	@Override
	public void dispose() {
		for (Image image : scaleCache.values()) {
			image.dispose();
		}
		scaleCache.clear();
	}
	/**
	 */
	public Pair<Double, Image> getImage(final int sx, final int sy) {
		int sz = Math.max(sx, sy);
		SortedMap<Integer, Image> tailMap = iconMap.tailMap(sz);
		Image icon;
		if (tailMap.isEmpty())
			if (!iconMap.isEmpty())
				icon = iconMap.lastEntry().getValue();
			else
				icon = null;
		else
			icon = tailMap.values().iterator().next();
		
		if (icon == null)
			return new Pair<Double, Image>(1.0, null);
		
		Rectangle bounds = icon.getBounds();
		int iconWidth = bounds.width;
		int iconHeight = bounds.height;
		if (iconWidth != sx) {
			Image scaled = scaleCache.get(sx);
			if (scaled == null) {
				int newHeight = iconWidth > iconHeight ? iconWidth * sx / iconHeight 
						: iconHeight * sx / iconWidth;
				scaled = ImageUtils.scaleImage(icon.getDevice(), 
						//iconMap.lastEntry().getValue(),
						icon,
						new Point(sx, newHeight), 
						true); 
				scaleCache.put(sx, scaled);
				if (scaleCache.size() > 16) {
					Iterator<Entry<Integer, Image>> iter = scaleCache.entrySet().iterator();
					Entry<Integer, Image> ent = iter.next();
					ent.getValue().dispose();
					iter.remove();
				}
			}
			icon = scaled;
			iconWidth = sx;
		}
		
		int min = iconMap.values().iterator().next().getBounds().width;
		double ratio = (double) iconWidth / min;
		return new Pair<Double, Image>(ratio, icon);
	}

	@Override
	public void drawImage(GC gc, int alpha, Rectangle drawRect, Rectangle imgRect) {
		double ratio;
		Pair<Double, Image> iconInfo = getImage(drawRect.width, drawRect.height);
		ratio = iconInfo.first;
		Image icon = iconInfo.second;
		if (drawRect.width > 0 && imgRect.width > 0 && ratio > 0 && imgRect.x >= 0 && imgRect.y >= 0) {
//			gc.setAntialias(SWT.ON);
//			Transform transform = new Transform(gc.getDevice());
//			transform.translate(-drawRect.x, -drawRect.y);
//			gc.setTransform(transform);
			
			if (alpha == 255 || ImageUtils.isAlphaBlendingSupported()) {
				int origAlpha = gc.getAlpha();
				if (alpha != 255) 
					gc.setAlpha(alpha);
				gc.drawImage(icon, 
						(int)(imgRect.x * ratio), (int)(imgRect.y * ratio),
					(int)(imgRect.width * ratio), (int) (imgRect.height * ratio), 
					drawRect.x, drawRect.y,
					drawRect.width, drawRect.height);

				if (alpha != origAlpha)
					gc.setAlpha(origAlpha);
			} else {
				// e.g. no GDI+
				Image alphaImg = ImageUtils.makeAlphaBlendedImage(icon, alpha);
				gc.drawImage(alphaImg, 
						(int)(imgRect.x * ratio), (int)(imgRect.y * ratio),
						(int)(imgRect.width * ratio), (int) (imgRect.height * ratio), 
						drawRect.x, drawRect.y,
						drawRect.width, drawRect.height);
				alphaImg.dispose();
			}
//			transform.dispose();
		}
	}

	/**
	 * @param i
	 * @return
	 */
	public Rectangle imageIndexToBounds(int iconIndex) {
		Rectangle bounds = iconMap.values().iterator().next().getBounds();
		int unit = bounds.width;
		return new Rectangle(0, unit * iconIndex, unit, unit); 
	}
}
