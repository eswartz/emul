/**
 * 
 */
package org.ejs.gui.images;

import java.util.ArrayList;
import java.util.Arrays;
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
		private int[] rgb;
		private int count;
		
		public ColorLeaf(int[] prgb) {
			this.rgb = Arrays.copyOf(prgb, 3);
		}

		public ColorLeaf(int[] prgb, int count) {
			this.rgb = Arrays.copyOf(prgb, 3);
			this.count = count;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(rgb);
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
			if (!Arrays.equals(rgb, other.rgb))
				return false;
			return true;
		}

		

		@Override
		public String toString() {
			return "ColorLeaf [rgb=" + Arrays.toString(rgb) + ", count="
					+ count + "]";
		}

		@Override
		public int[] reprRGB() {
			return rgb;
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
	public void addColor(int[] prgb) {
		int pixel = ColorMapUtils.rgb8ToPixel(prgb);
		
		ColorLeaf leaf= map.get(pixel);
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
		System.out.println("total colors: " + pixels.size());
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
			System.out.println("merging " + (pixels.size() - maxLeafCount + 1) + " leaves");
			
			// sort by frequency and merge the last ones
			List<ColorLeaf> mergers = pixels.subList(maxLeafCount - 1, pixels.size());
			Collections.sort(mergers, new Comparator<ColorLeaf>() {
				@Override
				public int compare(ColorLeaf o1, ColorLeaf o2) {
					return o2.count - o1.count;
				}
			});
			
			int[] rgb = { 0, 0, 0 };
			int count = 0;
			
			int n = mergers.size();
			for (ColorLeaf leaf : mergers) {
				rgb[0] += leaf.rgb[0]; 
				rgb[1] += leaf.rgb[1]; 
				rgb[2] += leaf.rgb[2];
				count += leaf.count;
			}
			rgb[0] = (rgb[0] + n - 1) / n;
			rgb[1] = (rgb[1] + n - 1) / n;
			rgb[2] = (rgb[2] + n - 1) / n;
			
			mergers.set(0, new ColorLeaf(rgb, count));
			
			pixels.subList(maxLeafCount, pixels.size()).clear();
		}
	}

	static final Comparator<ColorLeaf> redCompare = new Comparator<ColorLeaf>() {
		@Override
		public int compare(ColorLeaf o1, ColorLeaf o2) {
			return o1.rgb[0] - o2.rgb[0];
		}
	};
	static final Comparator<ColorLeaf> greenCompare = new Comparator<ColorLeaf>() {
		@Override
		public int compare(ColorLeaf o1, ColorLeaf o2) {
			return o1.rgb[1] - o2.rgb[1];
		}
	};
	static final Comparator<ColorLeaf> blueCompare = new Comparator<ColorLeaf>() {
		@Override
		public int compare(ColorLeaf o1, ColorLeaf o2) {
			return o1.rgb[2] - o2.rgb[2];
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
			int[] rgb = { 0, 0, 0};
			int count = 0;
			int n = 0;
			for (ColorLeaf leaf : subList) {
				if (leaf != null) {
					rgb[0] += leaf.rgb[0];
					rgb[1] += leaf.rgb[1];
					rgb[2] += leaf.rgb[2];
					count += leaf.count;
					n++;
				}
			}
			for (int i = start; i < end; i++) {
				pixels.set(i, null);
			}
			if (n > 0) {
				rgb[0] = (rgb[0] + n - 1) / n;
				rgb[1] = (rgb[1] + n - 1) / n;
				rgb[2] = (rgb[2] + n - 1) / n;
				pixels.set(start, new ColorLeaf(rgb, count));
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
		for (ColorLeaf leaf : subList) {
			minR = Math.min(minR, leaf.rgb[0]);
			minG = Math.min(minG, leaf.rgb[1]);
			minB = Math.min(minB, leaf.rgb[2]);
			maxR = Math.max(maxR, leaf.rgb[0]);
			maxG = Math.max(maxG, leaf.rgb[1]);
			maxB = Math.max(maxB, leaf.rgb[2]);
			count += leaf.count;
			n++;
		}

		if (n == 0)
			return;
		
		// sort by maximum axis then find the median
		int mid;
		if (maxR - minR >= maxG - minG && maxR - minR >= maxB - minB) {
			Collections.sort(subList, redCompare);
		}
		else if (maxG - minG >= maxR - minR && maxG - minG >= maxB - minB) {
			Collections.sort(subList, greenCompare);
		}
		else {
			Collections.sort(subList, blueCompare);
		}
		
		int midCount = 0;
		for (mid = 0; mid < subList.size(); mid++) {
			ColorLeaf leaf = subList.get(mid); 
			midCount += leaf.count;
			if (midCount >= count / 2) {
				break;
			}
		}
		
		reduceColorDepth(depth - 1, start, mid + start);
		reduceColorDepth(depth - 1, mid + start, end);
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
