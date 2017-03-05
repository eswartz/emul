/*
  ColorMedianCut.java

  (c) 2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package org.ejs.gui.images;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Median cut algorithm for color quantization.
 * @author ejs
 *
 */
public class ColorMedianCut implements IColorQuantizer {

	static class ColorLeaf implements ILeaf {
		private int r, g, b;
		private int count;
		
		public ColorLeaf(int[] prgb) {
			this.r = prgb[0];
			this.g = prgb[1];
			this.b = prgb[2];
		}

		public ColorLeaf(int r, int g, int b, int count) {
			this.r = r;
			this.g = g;
			this.b = b;
			this.count = count;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + b;
			result = prime * result + g;
			result = prime * result + r;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ColorLeaf other = (ColorLeaf) obj;
			if (b != other.b)
				return false;
			if (g != other.g)
				return false;
			if (r != other.r)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ColorLeaf [rgb=" + r+","+g+","+b+ ", count="
					+ count + "]";
		}

		@Override
		public int[] reprRGB() {
			return new int[] { r, g, b };
		}

		@Override
		public int getPixelCount() {
			return count;
		}
		
	}
	
	private List<ColorLeaf> pixels = new ArrayList<ColorLeaf>(65536);
	private Map<Integer,ColorLeaf> map = new HashMap<Integer, ColorLeaf>();

	public ColorMedianCut() {
	}

	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IColorQuantizer#addColor(int[])
	 */
	@Override
	public void addColor(int pixel, int[] prgb) {
		// time/space tradeoff: reduce precision here to avoid 
		// wasting lots of time merging large lists of mostly similar pixels
		pixel &= 0xfcfcfc;
		prgb[0] = (prgb[0] & 0xfc) * 0xff / 0xfc;
		prgb[1] = (prgb[1] & 0xfc) * 0xff / 0xfc;
		prgb[2] = (prgb[2] & 0xfc) * 0xff / 0xfc;
		
		ColorLeaf leaf = map.get(pixel);
		if (leaf != null) {
			leaf.count++;
		} else {
			leaf = new ColorLeaf(prgb);
			leaf.count = 1;
			pixels.add(leaf);
			map.put(pixel, leaf);
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IColorQuantizer#getLeafCount()
	 */
	@Override
	public int getLeafCount() {
		return pixels.size();
	}

	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IColorQuantizer#reduceTree(int)
	 */
	@Override
	public void reduceColors(int maxLeafCount) {
//		System.out.println("total colors: " + pixels.size());
		if (pixels.size() > maxLeafCount) {
			int depth = 1;
			while ((1 << depth) < maxLeafCount)
				depth++;
			reduceColorDepth(depth, 0, pixels.size());
		}
		
		// remove all nulls
		for (Iterator<ColorLeaf> iter = pixels.iterator(); iter.hasNext();) {
			ColorLeaf leaf = iter.next();
			if (leaf == null)
				iter.remove();
		}
		
		if (pixels.size() > maxLeafCount) {
//			System.out.println("merging " + (pixels.size() - maxLeafCount + 1) + " leaves");
			
			// sort by frequency and merge the last ones
			List<ColorLeaf> mergers = pixels.subList(maxLeafCount - 1, pixels.size());
			Collections.sort(mergers, new Comparator<ColorLeaf>() {
				@Override
				public int compare(ColorLeaf o1, ColorLeaf o2) {
					return o2.count - o1.count;
				}
			});
			
			int r = 0, g = 0, b = 0;
			int count = 0;
			
			int n = mergers.size();
			for (int i = 0; i < n; i++) {
				ColorLeaf leaf = mergers.get(i);
				r += leaf.r; 
				g += leaf.g; 
				b += leaf.b;
				count += leaf.count;
			}
			r = (r + n - 1) / n;
			g = (g + n - 1) / n;
			b = (b + n - 1) / n;
			
			mergers.set(0, new ColorLeaf(r, g, b, count));
			
			// remove excess
			pixels.subList(maxLeafCount, pixels.size()).clear();
		}
	}

	static final Comparator<ColorLeaf> redCompare = new Comparator<ColorLeaf>() {
		@Override
		public int compare(ColorLeaf o1, ColorLeaf o2) {
			return o1.r - o2.r;
		}
	};
	static final Comparator<ColorLeaf> greenCompare = new Comparator<ColorLeaf>() {
		@Override
		public int compare(ColorLeaf o1, ColorLeaf o2) {
			return o1.g - o2.g;
		}
	};
	static final Comparator<ColorLeaf> blueCompare = new Comparator<ColorLeaf>() {
		@Override
		public int compare(ColorLeaf o1, ColorLeaf o2) {
			return o1.b - o2.b;
		}
	};
	
	/**
	 * Reduce colors at depth 
	 * @param depth depth, 2^depth = # colors
	 * @param start
	 * @param end
	 */
	private void reduceColorDepth(int depth, int start, int end) {
		List<ColorLeaf> subList = pixels.subList(start, end);
		
		if (depth == 0) {
			// no more entries desired; merge everything
//			System.out.println("merging " + (end - start) + " colors");
			int r = 0, g = 0, b = 0;
			int count = 0;
			int n = 0;
			int scale = 0;
			
			// slightly favor more populous colors
			for (int i = start; i < end; i++) {
				ColorLeaf leaf = pixels.get(i);
				int sc = (int) Math.sqrt(leaf.count);
				r += leaf.r * sc;
				g += leaf.g * sc;
				b += leaf.b * sc;
				count += leaf.count;
				scale += sc;
				n++;
			}
			for (int i = start; i < end; i++) {
				pixels.set(i, null);
			}
			if (n > 0) {
				r = (r + scale - 1) / scale;
				g = (g + scale - 1) / scale;
				b = (b + scale - 1) / scale;
				pixels.set(start, new ColorLeaf(r, g, b, count));
			}
			return;
		}
		
		int minR = Integer.MAX_VALUE; 
		int minG = Integer.MAX_VALUE; 
		int minB = Integer.MAX_VALUE; 
		int maxR = Integer.MIN_VALUE; 
		int maxG = Integer.MIN_VALUE; 
		int maxB = Integer.MIN_VALUE; 
		
		int n = 0;
		int count = 0;
		for (int i = start; i < end; i++) {
			ColorLeaf leaf = pixels.get(i);
			minR = Math.min(minR, leaf.r);
			minG = Math.min(minG, leaf.g);
			minB = Math.min(minB, leaf.b);
			maxR = Math.max(maxR, leaf.r);
			maxG = Math.max(maxG, leaf.g);
			maxB = Math.max(maxB, leaf.b);
			count += leaf.count;
			n++;
		}

		if (n == 0)
			return;
		
		// sort by maximum axis then find the median
		int mid;
		if (maxR - minR > maxG - minG && maxR - minR > maxB - minB) {
			Collections.sort(subList, redCompare);
		}
		else if (maxG - minG > maxR - minR && maxG - minG > maxB - minB) {
			Collections.sort(subList, greenCompare);
		}
		else {
			Collections.sort(subList, blueCompare);
		}
		
		int midCount = 0;
		for (mid = start; mid < end; mid++) {
			ColorLeaf leaf = pixels.get(mid); 
			midCount += leaf.count * 2;
			if (midCount >= count) {
				break;
			}
		}
		
		reduceColorDepth(depth - 1, start, mid);
		reduceColorDepth(depth - 1, mid, end);
	}

	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IColorQuantizer#gatherLeaves()
	 */
	@Override
	public List<ILeaf> gatherLeaves() {
		List<ILeaf> leaves = new ArrayList<IColorQuantizer.ILeaf>();
		for (ColorLeaf leaf : pixels) {
			if (leaf != null)
				leaves.add(leaf);
		}
		return leaves;
	}

}
